package choijjyo.reco.view.recognize

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import choijjyo.reco.R
import choijjyo.reco.databinding.ActivityChoiceBinding
import choijjyo.reco.view.MainActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ChoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChoiceBinding
    private lateinit var curPhotoPath: String

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val file = File(curPhotoPath)
            val bitmap = BitmapFactory.decodeFile(curPhotoPath)
            savePhoto(bitmap) // 이미지 저장
            startRecognizeActivity(Uri.fromFile(file)) // RecognizeActivity로 URI 전달
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            startRecognizeActivity(it) // 갤러리에서 선택한 사진의 URI를 RecognizeActivity로 전달
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setPermission()

        binding.galleryBtn.setOnClickListener {
            openGallery()
        }

        binding.cameraBtn.setOnClickListener {
            takeCapture()
        }

        val backButton = findViewById<Button>(R.id.backToMainButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit)
            finish() // Optional: Finish the current activity
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private fun takeCapture() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile() // 이미지 파일 생성
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

    private fun createImageFile(): File? {
        val timestamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timestamp}_",
            ".jpg",
            storageDir
        ).apply {
            curPhotoPath = absolutePath
        }
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

    }


    private fun startRecognizeActivity(uri: Uri) {
        val intent = Intent(this, RecognizeActivity::class.java)
        intent.putExtra("photoUri", uri.toString()) // RecognizeActivity로 URI 전달
        startActivity(intent)
    }

    // 퍼미션 설정
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setPermission() {
        val readPermission = object : PermissionListener {

            override fun onPermissionGranted() {
                // 읽기 권한 허용 시 갤러리 버튼 활성화
                binding.galleryBtn.setOnClickListener {
                    openGallery()
                }
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                // 읽기 권한 거부 시 갤러리 버튼 비활성화
                binding.galleryBtn.isEnabled = false
                Toast.makeText(this@ChoiceActivity, "갤러리를 사용하려면 읽기 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
            }
        }

        val cameraPermission = object : PermissionListener {
            override fun onPermissionGranted() {
                // 쓰기 권한 허용 시 카메라 버튼 활성화
                binding.cameraBtn.setOnClickListener {
                    takeCapture()
                }
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                // 쓰기 권한 거부 시 카메라 버튼 비활성화
                binding.cameraBtn.isEnabled = false
                Toast.makeText(this@ChoiceActivity, "카메라를 사용하려면 쓰기 권한을 허용해주세요.", Toast.LENGTH_LONG).show()
            }
        }

        // 갤러리 읽기 권한 체크
        TedPermission.create()
            .setPermissionListener(readPermission)
            .setPermissions(Manifest.permission.READ_MEDIA_IMAGES)
            .check()
        // 갤러리 읽기 권한 체크
        TedPermission.create()
            .setPermissionListener(readPermission)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()
        // 갤러리 쓰기 권한 체크
        TedPermission.create()
            .setPermissionListener(readPermission)
            .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()


        // 카메라 쓰기 권한 체크
        TedPermission.create()
            .setPermissionListener(cameraPermission)
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }



}
