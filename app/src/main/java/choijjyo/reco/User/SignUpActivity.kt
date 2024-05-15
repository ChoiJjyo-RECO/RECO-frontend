package choijjyo.reco.User

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import choijjyo.reco.FirestoreHelper
import choijjyo.reco.Main.MainActivity
import choijjyo.reco.R
import choijjyo.reco.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)

        binding.joinBtn.setOnClickListener {
            joinAccount()
        }
        binding.exitBtn.setOnClickListener {
            intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
    }
    private fun joinAccount() {
        var name = binding.inputName.text.toString()
        var email = binding.inputID.text.toString()
        var password = binding.inputPW.text.toString()
        var passwordCheck = binding.checkPW.text.toString()

        // TODO: 유효성 검사 추가


        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}