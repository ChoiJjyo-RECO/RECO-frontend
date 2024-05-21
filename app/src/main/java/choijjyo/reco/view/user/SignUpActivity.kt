package choijjyo.reco.view.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import choijjyo.reco.data.source.firestore.FirestoreHelper
import choijjyo.reco.view.MainActivity
import choijjyo.reco.R
import choijjyo.reco.data.entity.user.UsersData
import choijjyo.reco.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    private lateinit var inputName: EditText
    private lateinit var inputEmail: EditText
    private lateinit var inputPW: EditText
    private lateinit var checkPW: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        inputName = findViewById(R.id.inputName)
        inputEmail = findViewById(R.id.inputEmail)
        inputPW = findViewById(R.id.inputPW)
        checkPW = findViewById(R.id.checkPW)

        inputName.setText(intent.getStringExtra("inputName"))
        inputEmail.setText(intent.getStringExtra("inputEmail"))
        inputPW.setText(intent.getStringExtra("inputPW"))
        checkPW.setText(intent.getStringExtra("checkPW"))

        val agreed = intent.getBooleanExtra("AGREED", false)

        if (agreed) {
            binding.checkPolicyBtn.visibility = View.GONE
            binding.signUpBtn.visibility = View.VISIBLE
        } else {
            binding.checkPolicyBtn.visibility = View.VISIBLE
            binding.signUpBtn.visibility = View.GONE
        }

        binding.signUpBtn.setOnClickListener {
            joinAccount()
        }
        binding.checkPolicyBtn.setOnClickListener {
            val intent = Intent(this, CheckPolicyActivity::class.java)
            intent.putExtra("inputName", inputName.text.toString())
            intent.putExtra("inputEmail", inputEmail.text.toString())
            intent.putExtra("inputPW", inputPW.text.toString())
            intent.putExtra("checkPW", checkPW.text.toString())
            startActivity(intent)
        }
        binding.exitBtn.setOnClickListener {
            intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
    private fun joinAccount() {
        var name = binding.inputName.text.toString()
        var email = binding.inputEmail.text.toString()
        var password = binding.inputPW.text.toString()
        var passwordCheck = binding.checkPW.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(this, "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (email.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != passwordCheck) {
            binding.checkPWText.visibility = View.VISIBLE
            binding.checkPWText.text = "비밀번호가 일치하지 않습니다."
            binding.checkPWText.setTextColor(ContextCompat.getColor(this@SignUpActivity, R.color.red))
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    binding.checkIDText.visibility = View.GONE
                    Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                    val user = auth.currentUser
                    if (user != null) {
                        val uid = user.uid
                        FirestoreHelper.setDocument(
                            this, uid, UsersData(
                                name = name,
                                email = email
                            )
                        )
                    }
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = when (task.exception?.message) {
                        "The email address is badly formatted." -> {
                            binding.checkPWText.visibility = View.GONE
                            binding.checkIDText.visibility = View.VISIBLE
                            binding.checkIDText.text = "이메일 형식이 올바르지 않습니다."
                            "이메일 형식이 올바르지 않습니다."
                        }
                        "The email address is already in use by another account." -> {
                            binding.checkPWText.visibility = View.GONE
                            binding.checkIDText.visibility = View.VISIBLE
                            binding.checkIDText.text = "이미 사용 중인 이메일 주소입니다."
                            "이미 사용 중인 이메일 주소입니다."
                        }
                        "The given password is invalid. [ Password should be at least 6 characters ]" -> {
                            binding.checkIDText.visibility = View.GONE
                            binding.checkPWText.visibility = View.VISIBLE
                            binding.checkPWText.text = "비밀번호는 최소 6자 이상이어야 합니다."
                            binding.checkPWText.setTextColor(ContextCompat.getColor(this@SignUpActivity, R.color.red))
                            "비밀번호는 최소 6자 이상이어야 합니다."
                        }
                        else -> "회원가입 실패: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
}