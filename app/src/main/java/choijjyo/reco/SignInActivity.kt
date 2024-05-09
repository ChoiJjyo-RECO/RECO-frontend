package choijjyo.reco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import choijjyo.reco.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
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

    private fun signIn(identity: String, password: String) {
        auth.signInWithEmailAndPassword(identity, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this, "로그인에 성공했어요", Toast.LENGTH_LONG).show()
                    Toast.makeText(this, auth.currentUser?.uid.toString(), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "로그인에 실패했어요", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener(this) { exception ->
                Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                Log.e("SignInActivity", "signInWithEmailAndPassword:failure", exception)
            }
    }
    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        Toast.makeText(this, "익명 로그인: success", Toast.LENGTH_SHORT).show()
                        intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        FirestoreHelper.setDocument(this, uid, UsersData(
                            name = "익명${getRandomNumber()}",
                            email = ""
                        ))
                    } else {
                        Toast.makeText(this, "익명 사용자 ID를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "익명 로그인: failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun getRandomNumber(): Int {
        return (1..1000).random()
    }
}