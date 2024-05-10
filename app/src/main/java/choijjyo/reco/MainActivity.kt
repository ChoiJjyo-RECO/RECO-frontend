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
        loadImagesFromFirestore()

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
        loadImagesFromFirestore()
    }


    private fun loadImagesFromFirestore() {
        val imageList = mutableListOf<String>()
        val recyclerView: RecyclerView = findViewById(R.id.recentView)

        firestore.collection("users").document(uid).collection("closet")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrl = document.getString("imgURL")
                    if (imageUrl != null) {
                        imageList.add(imageUrl)
                    }
                }
                val adapter = ImageAdapter(imageList)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = GridLayoutManager(this, 3)
            }
            .addOnFailureListener { exception ->
                Log.e("LoadImages", "Error getting documents: ", exception)
            }
    }


}