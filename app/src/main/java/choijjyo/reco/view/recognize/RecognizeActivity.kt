package choijjyo.reco.view.recognize

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentTransaction
import choijjyo.reco.data.source.firestore.FirestoreHelper
import choijjyo.reco.R
import choijjyo.reco.data.entity.closet.ClosetData
import choijjyo.reco.databinding.ActivityRecognizeBinding
import choijjyo.reco.view.MainActivity
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class RecognizeActivity : AppCompatActivity() {

    companion object {
        init{
            System.loadLibrary("opencv_java4")
        }
    }

    private lateinit var binding: ActivityRecognizeBinding
//    private lateinit var progressBar: ProgressBar
    private lateinit var uid: String
    private lateinit var auth: FirebaseAuth
    private var uri: Uri? = null
    private lateinit var originalBitmap: Bitmap
    private val colorFilterHelper = ColorFilterHelper()
    private lateinit var getUri : String
    private lateinit var deuteranopiaButton: Button
    private lateinit var protanopiaButton: Button
    private lateinit var tritanopiaButton: Button
    private lateinit var originalImgButton: Button

    private var isDeuteranopiaButtonPressed = false
    private var isProtanopiaButtonPressed = false
    private var isTritanopiaButtonPressed = false
    private var isOriginalImgButtonPressed = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognizeBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
        }
        getUri = intent.getStringExtra("photoUri").toString()
        uri = Uri.parse(getUri)

        setContentView(binding.root)

        deuteranopiaButton = binding.deuteranopiaButton
        protanopiaButton = binding.protanopiaButton
        tritanopiaButton = binding.tritanopiaButton
        originalImgButton = binding.originalImgButton
//        progressBar = binding.progressBar
        setGallery(uri)

        binding.deuteranopiaButton.setOnClickListener {
            if (!isDeuteranopiaButtonPressed) {
                applyDeuteranopia()
                updateButtonBackground(deuteranopiaButton, true)
                resetButtonBackground(deuteranopiaButton)
                isDeuteranopiaButtonPressed = true
                isProtanopiaButtonPressed = false
                isTritanopiaButtonPressed = false
                isOriginalImgButtonPressed = false
            }
        }
        binding.protanopiaButton.setOnClickListener {
            if (!isProtanopiaButtonPressed) {
                applyProtanopia()
                updateButtonBackground(protanopiaButton, true)
                resetButtonBackground(protanopiaButton)
                isDeuteranopiaButtonPressed = false
                isProtanopiaButtonPressed = true
                isTritanopiaButtonPressed = false
                isOriginalImgButtonPressed = false
            }
        }
        binding.tritanopiaButton.setOnClickListener {
            if (!isTritanopiaButtonPressed) {
                applyTritanopia()
                updateButtonBackground(tritanopiaButton, true)
                resetButtonBackground(tritanopiaButton)
                isDeuteranopiaButtonPressed = false
                isProtanopiaButtonPressed = false
                isTritanopiaButtonPressed = true
                isOriginalImgButtonPressed = false
            }
        }
        binding.originalImgButton.setOnClickListener {
            if (!isOriginalImgButtonPressed) {
                showOriginal()
                updateButtonBackground(originalImgButton, true)
                resetButtonBackground(originalImgButton)
                isDeuteranopiaButtonPressed = false
                isProtanopiaButtonPressed = false
                isTritanopiaButtonPressed = false
                isOriginalImgButtonPressed = true
            }
        }

        binding.tabLayoutSearch.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                val tag = "FragmentTag$position"
                var fragment = supportFragmentManager.findFragmentByTag(tag)

                if (fragment == null) {
                    fragment = when (position) {
                        0 -> RecognizeRecommendClothesFragment()
                        1 -> RecognizeSimilarClothesFragment()
                        else -> null
                    }
                }

                // 선택된 프래그먼트를 표시
                fragment?.let {
                    val transaction = supportFragmentManager.beginTransaction()
                    if (!it.isAdded) {
                        transaction.add(R.id.searchRecognize_container, it, tag)
                    }
                    hideOtherFragments(transaction, tag)
                    transaction.show(it).commit()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.tabLayoutSearch.selectTab(binding.tabLayoutSearch.getTabAt(0))

        val backButton = findViewById<Button>(R.id.backToMainButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: Finish the current activity
        }

    }
    private fun hideOtherFragments(transaction: FragmentTransaction, exceptTag: String) {
        supportFragmentManager.fragments.forEach {
            if (it.tag != exceptTag) {
                transaction.hide(it)
            }
        }
    }

    fun setGallery(uri : Uri?) {
//        progressBar.visibility = View.VISIBLE
//        binding.progressText.visibility = View.VISIBLE
        binding.progressLayout.visibility = View.VISIBLE
        binding.cameraIV.setImageURI(uri)
        originalBitmap = (binding.cameraIV.drawable as BitmapDrawable).bitmap
        uri?.let { uploadImageToFirestore(it) }
    }



    // Firebase Storage에 이미지 업로드
    private fun uploadImageToFirestore(uri: Uri) {
        this.uri = uri
        val storageRef = FirebaseStorage.getInstance().reference
        val imageName = uri.lastPathSegment ?: "default_filename"
        val imagesRef = storageRef.child("users/${uid}/closet/$imageName")

        val uploadTask = imagesRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            Log.d("RecognizeActivity", "이미지가 업로드되었습니다.")

            // 자동으로 모델 실행
            GlobalScope.launch(Dispatchers.IO){
                request()
            }

            imagesRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val imageUrl = downloadUri.toString()
                FirestoreHelper.saveImageUrlToCloset(
                    this, uid, imageName, ClosetData(
                        closetColorRGB = emptyList(),
                        closetColorCategory = "",
                        clothes = "",
                        imgURL = imageUrl,
                        timestamp = Timestamp.now()
                    )
                )
            }.addOnFailureListener {
                Log.d("RecognizeActivity", "이미지 URL을 가져오는 데 실패했습니다.")
            }
        }.addOnFailureListener {
            //progressBar.visibility = View.GONE
            Log.d("RecognizeActivity", "이미지 업로드에 실패했습니다.")
        }
    }
    private fun request() {
        try {
            Log.d("uid", "uid: $uid")
            Log.d("uri", "uri: "+ uri?.lastPathSegment)
            val docId = uri?.lastPathSegment

            // 요청 URL에 쿼리 매개변수 추가
            val url = URL("https://8ed7-61-98-10-215.ngrok-free.app/detect_and_analyze?uid=$uid&doc_id=$docId")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.requestMethod = "GET"
            conn.doInput = true

            val resCode = conn.responseCode
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line).append("\n")
            }
            reader.close()
            conn.disconnect()

            // JSON 파싱
            val jsonResponse = JSONObject(response.toString())
            val objectClass = jsonResponse.getString("object_class")
            val closestColorCategory = jsonResponse.getString("closest_color_category")
            val imgURL = jsonResponse.getString("image_URL")

            // 결과 표시
            runOnUiThread {
                binding.resultLayout.visibility = View.VISIBLE
                binding.resultColor.text = "$closestColorCategory"  // 색상
                binding.resultClothType.text = "$objectClass"       // 종류
                val googleSearchKeyword = "$closestColorCategory $objectClass 제품 사진"
                val modelResult = "$closestColorCategory $objectClass"
                Log.d("googleSearch keyword",googleSearchKeyword)
                if (docId != null) {
                    lifecycleScope.launch {
                        sendToSimilarFragment(googleSearchKeyword, docId)
                        sendToRecommendFragment(modelResult, docId)
                    }
                }
            }


        } catch (ex: Exception) {
            println("예외 발생함: ${ex.toString()}")
            runOnUiThread {
                val docId = uri?.lastPathSegment
                Toast.makeText(this@RecognizeActivity, "의류를 인식할 수 없습니다. 다시 촬영해주세요.", Toast.LENGTH_SHORT).show()
                if (docId != null) {
                    FirestoreHelper.deleteClothesDocFromFirestore(this, uid, docId)
                }
            }
        } finally {
            runOnUiThread {
//                binding.progressBar.visibility = View.GONE
//                binding.progressText.visibility = View.GONE
                binding.progressLayout.visibility = View.GONE
            }
        }
    }
    fun applyDeuteranopia() {
        val correctedBitmap = colorFilterHelper.applyDeuteranopia(originalBitmap)
        binding.cameraIV.setImageBitmap(correctedBitmap)
    }

    fun applyProtanopia() {
        val correctedBitmap = colorFilterHelper.applyProtanopia(originalBitmap)
        binding.cameraIV.setImageBitmap(correctedBitmap)
    }

    fun applyTritanopia() {
        val correctedBitmap = colorFilterHelper.applyTritanopia(originalBitmap)
        binding.cameraIV.setImageBitmap(correctedBitmap)
    }
    fun showOriginal() {
        binding.cameraIV.setImageBitmap(originalBitmap)
    }
    private fun sendToSimilarFragment(googleSearchKeyword: String, docid: String) {
        val fragment = supportFragmentManager.findFragmentByTag("FragmentTag1") as? RecognizeSimilarClothesFragment
        if (fragment != null) {
            fragment.setSearchKeyword(googleSearchKeyword, docid)
            supportFragmentManager.beginTransaction().show(fragment).commit()
        } else {
            val similar_Fragment = RecognizeSimilarClothesFragment().apply {
                setSearchKeyword(googleSearchKeyword, docid)
            }
            supportFragmentManager.beginTransaction().apply {
                add(R.id.searchRecognize_container, similar_Fragment, "FragmentTag1")
                hideOtherFragments(this, "FragmentTag1") // 다른 프래그먼트는 숨기기
                commitNow()
            }
        }
    }

    private fun sendToRecommendFragment(modelResult: String, docid: String) {
        binding.tabLayoutSearch.getTabAt(0)?.select()
        val fragment = supportFragmentManager.findFragmentByTag("FragmentTag0") as? RecognizeRecommendClothesFragment
        if (fragment != null) {
            fragment.setSearchKeyword(modelResult, docid)
            supportFragmentManager.beginTransaction().show(fragment).commit()
        } else {
            val recommend_Fragment = RecognizeRecommendClothesFragment().apply {
                setSearchKeyword(modelResult, docid)
            }
            supportFragmentManager.beginTransaction().apply {
                add(R.id.searchRecognize_container, recommend_Fragment, "FragmentTag0")
                hideOtherFragments(this, "FragmentTag0") // 다른 프래그먼트는 숨기기
                commitNow()
            }
        }
    }
    private fun updateButtonBackground(button: Button, isPressed: Boolean) {
        if (isPressed) {
            button.setBackgroundResource(R.drawable.btn_selected_bg) // 눌린 상태 배경색
        } else {
            button.setBackgroundResource(R.drawable.btn_not_selected_bg) // 일반 상태 배경색
        }
    }

    private fun resetButtonBackground(clickedButton: Button) {
        if (clickedButton != binding.deuteranopiaButton) {
            updateButtonBackground(binding.deuteranopiaButton, false)
        }
        if (clickedButton != binding.protanopiaButton) {
            updateButtonBackground(binding.protanopiaButton, false)
        }
        if (clickedButton != binding.tritanopiaButton) {
            updateButtonBackground(binding.tritanopiaButton, false)
        }
        if (clickedButton != binding.originalImgButton) {
            updateButtonBackground(binding.originalImgButton, false)
        }
    }

}