package choijjyo.reco

import android.Manifest
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.databinding.ActivityRecognizeBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleCustomSearchAPI {
    @GET("customsearch/v1")
    fun searchImages(
        @Query("key") apiKey: String,
        @Query("cx") searchEngineId: String,
        @Query("q") query: String,
        @Query("searchType") searchType: String = "image",
        @Query("num") num: Int = 6
    ): Call<SearchResponse>
}

data class SearchResponse(
    val items: List<SearchItem>
)

data class SearchItem(
    val link: String, // 이미지 URL
    val image: Image
)

data class Image(
    val contextLink: String // 클릭 시 이동할 URL
)
data class SearchResultItem(
    val imageUrl: String,
    val clickUrl: String
)



class RecognizeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecommendImageAdapter

    companion object {
        init{
            System.loadLibrary("opencv_java4")
        }
    }

    private lateinit var binding: ActivityRecognizeBinding
    private lateinit var curPhotoPath : String
    private lateinit var progressBar: ProgressBar
    private lateinit var uid: String
    private var uri: Uri? = null
    private var galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            uri -> setGallery(uri)
        progressBar.visibility = View.VISIBLE
    }
    private val tabTextList = listOf<String>("Recommend Cloth", "Similar Cloth")
    private lateinit var originalBitmap: Bitmap
    private val colorFilterHelper = ColorFilterHelper()
    private val matrixDeuteranopia = arrayOf(
        doubleArrayOf(0.625, 0.375, 0.0),
        doubleArrayOf(0.7, 0.3, 0.0),
        doubleArrayOf(0.0, 0.3, 0.7)
    )

    private val matrixProtanopia = arrayOf(
        doubleArrayOf(0.567, 0.433, 0.0),
        doubleArrayOf(0.558, 0.442, 0.0),
        doubleArrayOf(0.0, 0.242, 0.758)
    )

    private val matrixTritanopia = arrayOf(
        doubleArrayOf(0.95, 0.05, 0.0),
        doubleArrayOf(0.0, 0.433, 0.567),
        doubleArrayOf(0.0, 0.475, 0.525)
    )

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bitmap: Bitmap
            val file = File(curPhotoPath)
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, Uri.fromFile(file))
                binding.cameraIV.setImageBitmap(bitmap)
            } else {
                val decode = ImageDecoder.createSource(
                    this.contentResolver,
                    Uri.fromFile(file)
                )
                bitmap = ImageDecoder.decodeBitmap(decode)
                binding.cameraIV.setImageBitmap(bitmap)
            }
            savePhoto(bitmap)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognizeBinding.inflate(layoutInflater)
        uid = intent.getStringExtra("userUid") ?: ""
        setContentView(binding.root)

        progressBar = findViewById(R.id.progressBar)

        setPermission()

        binding.cameraBtn.setOnClickListener {
            takeCapture()
        }

        binding.galleryBtn.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

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

        recyclerView = findViewById(R.id.recommedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)



    }

    fun setGallery(uri : Uri?) {
        binding.cameraIV.setImageURI(uri)
        originalBitmap = (binding.cameraIV.drawable as BitmapDrawable).bitmap
        binding.resultText.text = ""
        uri?.let { uploadImageToFirestore(it) }
    }

    // 테드 퍼미션 설정
    private fun setPermission() {
        val readPermission = object : PermissionListener {
            override fun onPermissionGranted() {
                setGalleryButton()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                binding.galleryBtn.isEnabled = false
                Toast.makeText(this@RecognizeActivity, "갤러리를 사용하려면 읽기 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
            }
        }

        val cameraPermission = object : PermissionListener {
            override fun onPermissionGranted() {
                setCameraButton()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                binding.cameraBtn.isEnabled = false
                Toast.makeText(this@RecognizeActivity, "카메라를 사용하려면 쓰기 권한을 허용해주세요.2", Toast.LENGTH_LONG).show()
            }
        }

        TedPermission.create()
            .setPermissionListener(readPermission)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()

        TedPermission.create()
            .setPermissionListener(cameraPermission)
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }
    private fun setGalleryButton() {
        binding.galleryBtn.setOnClickListener {
            if (hasReadStoragePermission()) {
                galleryLauncher.launch("image/*")
            } else {
                Toast.makeText(this, "갤러리를 사용하려면 읽기 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setCameraButton() {
        binding.cameraBtn.setOnClickListener {
            if (hasWriteStoragePermission()) {
                takeCapture()
            } else {
                Toast.makeText(this, "카메라를 사용하려면 쓰기 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hasReadStoragePermission(): Boolean {
        return (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun hasWriteStoragePermission(): Boolean {
        return (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    // 카메라 촬영
    private fun takeCapture() {
        // 기본 카메라 앱 실행
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    null
                }

                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "choijjyo.reco.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    cameraLauncher.launch(takePictureIntent)
                }
            }
        }
    }

    // 이미지 파일 생성
    private fun createImageFile(): File? {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
            .apply {  curPhotoPath = absolutePath}
    }

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
        // 이미지를 Firebase에 업로드
        uploadImageToFirestore(Uri.fromFile(File(curPhotoPath)))
        // 이미지를 저장하고 나서 originalBitmap을 초기화
        binding.cameraIV.setImageBitmap(bitmap)
        originalBitmap = bitmap
        progressBar.visibility = View.VISIBLE
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
                FirestoreHelper.saveImageUrlToCloset(this, uid, imageName, ClosetData(
                    closetColorRGB = emptyList(),
                    closetColorCategory = "",
                    clothes = "",
                    imgURL = imageUrl,
                    timestamp = Timestamp.now()
                ))
            }.addOnFailureListener {
                Log.d("RecognizeActivity", "이미지 URL을 가져오는 데 실패했습니다.")
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Log.d("RecognizeActivity", "이미지 업로드에 실패했습니다.")
        }
    }
    private fun request() {
        try {
            Log.d("uid", "uid: $uid")
            Log.d("uri", "uri: "+ uri?.lastPathSegment)
            val docId = uri?.lastPathSegment

            // 요청 URL에 쿼리 매개변수 추가
            val url = URL("https://21fa-61-98-10-215.ngrok-free.app/detect_and_analyze?uid=$uid&doc_id=$docId")
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
                binding.resultText.text = "색상: $closestColorCategory\n종류: $objectClass"
            }
            val googleSearchKeyword = "$closestColorCategory $objectClass 제품 사진"
            Log.d("googleSearch keyword",googleSearchKeyword)
            showRecommend(googleSearchKeyword)

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
                progressBar.visibility = View.GONE
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
    private fun showRecommend(query: String){
        // Retrofit 객체 생성 및 ApiService 인터페이스 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GoogleCustomSearchAPI::class.java)

        // API 호출
        val call = apiService.searchImages(query = query, apiKey = BuildConfig.GOOGLESEARCH_APIKEY, searchEngineId = BuildConfig.GOOGLESEARCH_SEARCHID)
        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    // API 응답으로부터 이미지 목록을 초기화
                    val imageList: List<SearchResultItem> = response.body()?.items?.map { apiItem ->
                        SearchResultItem(
                            imageUrl = apiItem.link,
                            clickUrl = apiItem.image.contextLink
                        )
                    } ?: listOf()

                    // 이미지 목록을 RecyclerView 어댑터에 전달
                    adapter = RecommendImageAdapter(imageList)
                    recyclerView.adapter = adapter
                } else {
                    // 에러 처리
                    Log.e("API Error", "Response Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                // 네트워크 에러 처리
                Log.e("API Error", "Network Error: ${t.message}")
            }
        })
    }

}