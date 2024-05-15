package choijjyo.reco.Recognize

import android.Manifest
import androidx.lifecycle.lifecycleScope
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentTransaction
import choijjyo.reco.FirestoreHelper
import choijjyo.reco.R
import choijjyo.reco.databinding.ActivityRecognizeBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date

class RecognizeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecognizeBinding
    private lateinit var curPhotoPath: String
    private lateinit var progressBar: ProgressBar
    private lateinit var uid: String
    private var uri: Uri? = null
    private lateinit var originalBitmap: Bitmap
    private val colorFilterHelper = ColorFilterHelper()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognizeBinding.inflate(layoutInflater)
        uid = intent.getStringExtra("userUid") ?: ""
        setContentView(binding.root)

        progressBar = findViewById(R.id.progressBar)

        binding.deuteranopiaButton.setOnClickListener {
            applyDeuteranopia()
        }
        binding.protanopiaButton.setOnClickListener {
            applyProtanopia()
        }
        binding.tritanopiaButton.setOnClickListener {
            applyTritanopia()
        }
        binding.originalImgButton.setOnClickListener {
            showOriginal()
        }

        binding.tabLayoutSearch.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                val tag = "FragmentTag$position"
                var fragment = supportFragmentManager.findFragmentByTag(tag)

                if (fragment == null) {
                    fragment = when (position) {
                        0 -> Fragment_RecommendClothes()
                        1 -> Fragment_SimilarClothes()
                        else -> null
                    }
                }

                // 선택된 프래그먼트를 표시
                fragment?.let {
                    val transaction = supportFragmentManager.beginTransaction()
                    if (!it.isAdded) {
                        transaction.add(R.id.searchfragment_container, it, tag)
                    }
                    hideOtherFragments(transaction, tag)
                    transaction.show(it).commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        binding.tabLayoutSearch.selectTab(binding.tabLayoutSearch.getTabAt(0))

        // 갤러리에서 이미지 선택하기
        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                startRecognizeActivity(it)
            }
        }

        binding.cameraIV.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

    }

    // ChoiceActivity로부터 받아온 URI를 RecognizeActivity로 전달
    private fun startRecognizeActivity(photoUri: Uri) {
        val intent = Intent(this, RecognizeActivity::class.java)
        intent.putExtra("photoUri", photoUri.toString())
        startActivity(intent)

        // 이미지를 Firebase에 업로드하고 모델 실행
        savePhoto(getBitmapFromUri(photoUri))
    }

    // Uri로부터 Bitmap 가져오기
    private fun getBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
    }

    // 이미지 저장 및 업로드
    private fun savePhoto(bitmap: Bitmap) {
        val folderPath = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/"
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val fileName = "${timestamp}.jpeg"
        val folder = File(folderPath)
        if (!folder.isDirectory) {
            folder.mkdirs()
        }

        val out = FileOutputStream(folderPath + fileName)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_LONG).show()

        // 이미지를 Firebase에 업로드하고 모델 실행
        uploadImageToFirestore(Uri.fromFile(File(curPhotoPath)))
        // 이미지를 저장하고 나서 originalBitmap을 초기화
        originalBitmap = bitmap
        progressBar.visibility = View.VISIBLE
    }

    // ChoiceActivity에서 받아온 URI를 사용하여 사진을 Firebase Storage에 업로드하는 함수
    private fun uploadImageToFirestore(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageName = uri.lastPathSegment ?: "default_filename"
        val imagesRef = storageRef.child("users/${uid}/closet/$imageName")

        val uploadTask = imagesRef.putFile(uri)

        uploadTask.addOnSuccessListener { _ ->
            Log.d("RecognizeActivity", "이미지가 업로드되었습니다.")

            // 이미지가 업로드되면 모델 실행
            GlobalScope.launch(Dispatchers.IO) {
                request()
            }

            // 이미지 다운로드 URL 가져오기
            imagesRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val imageUrl = downloadUri.toString()
                // Firestore에 이미지 URL 저장
                FirestoreHelper.saveImageUrlToCloset(
                    this@RecognizeActivity, uid, imageName, ClosetData(
                        closetColorRGB = emptyList(),
                        closetColorCategory = "",
                        clothes = "",
                        imgURL = imageUrl,
                        timestamp = Timestamp.now()
                    )
                )
                Log.d("RecognizeActivity", "이미지 다운로드 URL: $imageUrl")
            }.addOnFailureListener {
                Log.d("RecognizeActivity", "이미지 URL을 가져오는 데 실패했습니다.")
            }
        }.addOnFailureListener { exception ->
            progressBar.visibility = View.GONE
            Log.d("RecognizeActivity", "이미지 업로드에 실패했습니다.", exception)
        }
    }




    // 모델 실행 요청
    private fun request() {
        // 모델 실행 및 결과 처리
    }

    private fun applyDeuteranopia() {
        val correctedBitmap = colorFilterHelper.applyDeuteranopia(originalBitmap)
        binding.cameraIV.setImageBitmap(correctedBitmap)
    }

    private fun applyProtanopia() {
        val correctedBitmap = colorFilterHelper.applyProtanopia(originalBitmap)
        binding.cameraIV.setImageBitmap(correctedBitmap)
    }

    private fun applyTritanopia() {
        val correctedBitmap = colorFilterHelper.applyTritanopia(originalBitmap)
        binding.cameraIV.setImageBitmap(correctedBitmap)
    }

    private fun showOriginal() {
        binding.cameraIV.setImageBitmap(originalBitmap)
    }


    private fun hideOtherFragments(transaction: FragmentTransaction, exceptTag: String) {
        supportFragmentManager.fragments.forEach {
            if (it.tag != exceptTag) {
                transaction.hide(it)

    private fun sendToSimilarFragment(googleSearchKeyword: String, docid: String) {
        val fragment = supportFragmentManager.findFragmentByTag("FragmentTag1") as? Fragment_SimilarClothes
        if (fragment != null) {
            fragment.setSearchKeyword(googleSearchKeyword, docid)
            supportFragmentManager.beginTransaction().show(fragment).commit()
        } else {
            val similar_Fragment = Fragment_SimilarClothes().apply {
                setSearchKeyword(googleSearchKeyword, docid)
            }
            supportFragmentManager.beginTransaction().apply {
                add(R.id.searchfragment_container, similar_Fragment, "FragmentTag1")
                hideOtherFragments(this, "FragmentTag1") // 다른 프래그먼트는 숨기기
                commitNow()
            }
        }
    }

    private fun sendToRecommendFragment(modelResult: String, docid: String) {
        val fragment = supportFragmentManager.findFragmentByTag("FragmentTag0") as? Fragment_RecommendClothes
        if (fragment != null) {
            fragment.setSearchKeyword(modelResult, docid)
            supportFragmentManager.beginTransaction().show(fragment).commit()
        } else {
            val recommend_Fragment = Fragment_RecommendClothes().apply {
                setSearchKeyword(modelResult, docid)
            }
            supportFragmentManager.beginTransaction().apply {
                add(R.id.searchfragment_container, recommend_Fragment, "FragmentTag0")
                hideOtherFragments(this, "FragmentTag0") // 다른 프래그먼트는 숨기기
                commitNow()

            }
        }
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
    }
}
