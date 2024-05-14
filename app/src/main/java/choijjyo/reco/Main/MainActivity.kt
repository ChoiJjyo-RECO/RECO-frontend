package choijjyo.reco.Main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.FirestoreHelper
import choijjyo.reco.MyCloset.MyClosetActivity
import choijjyo.reco.R
import choijjyo.reco.databinding.ActivityMainBinding
import choijjyo.reco.Recognize.RecognizeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), RecentImageAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageList = mutableListOf<String>()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recentView)
        adapter = RecentImageAdapter(imageList, this)
        recyclerView.adapter = adapter

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
                        FirestoreHelper.loadImagesFromFirestoreForActivity(this, uid, recyclerView)
                    }
                }
                .addOnFailureListener { exception ->
                    // Firestore에서 사용자 이름을 가져오는 데 실패한 경우에 대한 처리
                }
        }

        binding.recognize.setOnClickListener {
            intent = Intent(this, RecognizeActivity::class.java)
            intent.putExtra("userUid", uid)
            startActivity(intent)
        }
        binding.closet.setOnClickListener {
            intent = Intent(this, MyClosetActivity::class.java)
            startActivity(intent)
        }
        binding.preference.setOnClickListener {
        }
    }

    override fun onResume() {
        super.onResume()
        FirestoreHelper.loadImagesFromFirestoreForActivity(this, uid, recyclerView)
    }

    override fun onItemClick(position: Int) {
        Log.d("MainActivity", "Item clicked at position: $position")
    }

}