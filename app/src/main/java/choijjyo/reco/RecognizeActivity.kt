package choijjyo.reco

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import choijjyo.reco.databinding.ActivityRecognizeBinding
//import com.google.firebase.storage.FirebaseStorage
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

class RecognizeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecognizeBinding
    private var galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
            //uri -> setGallery(uri)
    }

    val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

    lateinit var curPhotoPath : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecognizeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setPermission()

//        val uid = intent.getStringExtra("userUid")

        binding.cameraBtn.setOnClickListener {
            takeCapture()
        }

        binding.galleryBtn.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.runModel.setOnClickListener {
            //통신시작
            GlobalScope.launch(Dispatchers.IO){
                request()
            }
        }
    }


//    fun setGallery(uri : Uri?) {
//        binding.cameraIV.setImageURI(uri)
//        uri?.let { uploadImageToFirebase(it) }
//    }

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
        //uploadImageToFirebase(Uri.fromFile(File(curPhotoPath)))
    }



    // Firebase Storage에 이미지 업로드
//    private fun uploadImageToFirebase(uri: Uri) {
//        val storageRef = FirebaseStorage.getInstance().reference
//        val uid = intent.getStringExtra("userUid")
//        val imagesRef = storageRef.child("users/${uid}/closet/${uri.lastPathSegment}")
//
//        val uploadTask = imagesRef.putFile(uri)
//
//        uploadTask.addOnSuccessListener {
//            Toast.makeText(this@RecognizeActivity, "이미지가 업로드되었습니다.", Toast.LENGTH_SHORT).show()
//
//            imagesRef.downloadUrl.addOnSuccessListener { downloadUri ->
//                val imageUrl = downloadUri.toString()
//            }.addOnFailureListener { exception ->
//                Toast.makeText(this@RecognizeActivity, "이미지 URL을 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
//            }
//        }.addOnFailureListener { exception ->
//            Toast.makeText(this@RecognizeActivity, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
//        }
//    }
    private fun request() {
        try {
            val uid = intent.getStringExtra("userUid")
            Log.d("uid", "uid"+uid)
            val docId = "20240510"

            // 요청 URL에 쿼리 매개변수 추가
            val url = URL("https://b4f3-121-166-22-33.ngrok-free.app/detect_and_analyze?uid=$uid&doc_id=$docId")
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
                binding.resultText.text = "종류: $objectClass\n색깔: $closestColorCategory"
//                Glide.with(this@ClosetActivity)
//                    .load(imgURL)
//                    .into(clotheImg)
            }
        } catch (ex: Exception) {
            println("예외 발생함: ${ex.toString()}")
        }
    }
}