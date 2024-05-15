package choijjyo.reco.Recognize

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import choijjyo.reco.databinding.ActivityChoiceBinding
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
            savePhoto(file) // 이미지 저장
            startRecognizeActivity(Uri.fromFile(file)) // RecognizeActivity로 URI 전달
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            startRecognizeActivity(it) // 갤러리에서 선택한 사진의 URI를 RecognizeActivity로 전달
        }
    }

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

    private fun savePhoto(file: File) {
        val fileName = "reco_${System.currentTimeMillis()}.jpg"
        val fos: FileOutputStream
        try {
            val fos = FileOutputStream(file)
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun startRecognizeActivity(uri: Uri) {
        val intent = Intent(this, RecognizeActivity::class.java)
        intent.putExtra("photoUri", uri.toString()) // RecognizeActivity로 URI 전달
        startActivity(intent)
    }

    // 퍼미션 설정
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
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE)
            .check()

        // 카메라 쓰기 권한 체크
        TedPermission.create()
            .setPermissionListener(cameraPermission)
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }
}
