package choijjyo.reco.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.data.source.firestore.FirestoreHelper
import choijjyo.reco.view.preference.PreferenceActivity
import choijjyo.reco.view.myCloset.closet.ClosetActivity
import choijjyo.reco.R
import choijjyo.reco.view.recognize.ChoiceActivity
import choijjyo.reco.databinding.ActivityMainBinding
import choijjyo.reco.adapter.recent.RecentItemImageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity(), RecentItemImageAdapter.OnItemClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentItemImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val imageList = mutableListOf<String>()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        recyclerView = findViewById(R.id.recentView)
        adapter = RecentItemImageAdapter(imageList, this)
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
            intent = Intent(this, ChoiceActivity::class.java)
            intent.putExtra("userUid", uid)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit)
        }
        binding.closet.setOnClickListener {
            intent = Intent(this, ClosetActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit)
        }
        binding.preference.setOnClickListener {
            val likeIntent = Intent(this, PreferenceActivity::class.java)
            startActivity(likeIntent)
            overridePendingTransition(R.anim.slide_right_enter, R.anim.slide_right_exit)
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