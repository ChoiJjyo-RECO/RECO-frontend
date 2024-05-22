package choijjyo.reco.view.user

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import choijjyo.reco.data.source.firestore.FirestoreHelper
import choijjyo.reco.view.MainActivity
import choijjyo.reco.R
import choijjyo.reco.data.entity.user.UsersData
import choijjyo.reco.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in)

        binding.buttonSignIn.setOnClickListener {
            val identity = binding.editTextID.text.toString().trim()
            val password = binding.editTextPassWord.text.toString().trim()
            signIn(identity, password)
        }
        binding.buttonSignUp.setOnClickListener {
            intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.buttonAnonymousSignIn.setOnClickListener {
            signInAnonymously()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6 // 최소 6자리 이상의 비밀번호
    }

    private fun signIn(identity: String, password: String) {
        if (identity.isEmpty() && password.isEmpty()) {
            Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요.", Toast.LENGTH_LONG).show()
            binding.checkIDText.visibility = View.VISIBLE
            binding.checkPWText.visibility = View.VISIBLE
            binding.checkIDText.text = "이메일을 입력해주세요."
            binding.checkPWText.text = "비밀번호를 입력해주세요."
            return
        }
        if (identity.isEmpty()) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_LONG).show()
            binding.checkIDText.visibility = View.VISIBLE
            binding.checkPWText.visibility = View.GONE
            binding.checkIDText.text = "이메일을 입력해주세요."
            return
        }
        if (!isValidEmail(identity)) {
            Toast.makeText(this, "유효한 이메일 주소를 입력해주세요.", Toast.LENGTH_LONG).show()
            binding.checkIDText.visibility = View.VISIBLE
            binding.checkPWText.visibility = View.GONE
            binding.checkIDText.text = "유효한 이메일 주소를 입력해주세요."
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_LONG).show()
            binding.checkIDText.visibility = View.GONE
            binding.checkPWText.visibility = View.VISIBLE
            binding.checkPWText.text = "비밀번호를 입력해주세요."
            return
        }
        if (!isValidPassword(password)) {
            Toast.makeText(this, "비밀번호는 최소 6자리 이상이어야 합니다.", Toast.LENGTH_LONG).show()
            binding.checkIDText.visibility = View.GONE
            binding.checkPWText.visibility = View.VISIBLE
            binding.checkPWText.text = "비밀번호는 최소 6자리 이상이어야 합니다."
            return
        }
        auth.signInWithEmailAndPassword(identity, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "로그인에 성공했어요", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener(this) { exception ->
                when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        Toast.makeText(this, "이메일 주소나 비밀번호가 잘못되었습니다.\n다시 시도해주세요.", Toast.LENGTH_LONG).show()
                    }
                    is FirebaseAuthInvalidUserException -> {
                        Toast.makeText(this, "사용자가 존재하지 않습니다.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this, "로그인 중 오류가 발생했습니다: ${exception.message}", Toast.LENGTH_LONG).show()
                    }
                }
                Log.e("SignInActivity", "signInWithEmailAndPassword:failure", exception)
            }
    }
    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        Log.d("SignInActivity", "익명 로그인: success")
                        intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        FirestoreHelper.setDocument(
                            this, uid, UsersData(
                                name = "익명${getRandomNumber()}",
                                email = ""
                            )
                        )
                    } else {
                        Log.d("SignInActivity", "익명 사용자 ID를 가져오는 데 실패했습니다.")
                    }
                } else {
                    Log.d("SignInActivity", "익명 로그인: failed")
                }
            }
    }
    private fun getRandomNumber(): Int {
        return (1000..10000).random()
    }
}