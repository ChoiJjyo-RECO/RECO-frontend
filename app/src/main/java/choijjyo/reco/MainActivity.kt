package choijjyo.reco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recentView)

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
                        FirestoreHelper.loadImagesFromFirestore(this, uid, recyclerView)
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
            val closetIntent = Intent(this, MyClosetActivity::class.java)
            startActivity(closetIntent)


        }
        binding.preference.setOnClickListener {
        }
    }

    override fun onResume() {
        super.onResume()
        FirestoreHelper.loadImagesFromFirestore(this, "userUid", recyclerView)
    }





}