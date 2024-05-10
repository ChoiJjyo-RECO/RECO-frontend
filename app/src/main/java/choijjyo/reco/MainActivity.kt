package choijjyo.reco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import choijjyo.reco.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var uid:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name")
                        if (name != null) {
                            binding.username.text = name
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    // Firestore에서 사용자 이름을 가져오는 데 실패한 경우에 대한 처리
                }
        }

        binding.recognize.setOnClickListener {
            val mainintent = Intent(this, RecognizeActivity::class.java)
            mainintent.putExtra("userUid", uid)
            startActivity(mainintent)
        }
        binding.closet.setOnClickListener {
        }
        binding.preference.setOnClickListener {
        }
    }
}