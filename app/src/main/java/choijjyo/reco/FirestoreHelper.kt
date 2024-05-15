package choijjyo.reco

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.Main.Constants
import choijjyo.reco.Main.RecentImageAdapter
import choijjyo.reco.MyCloset.ClosetImageAdapter
import choijjyo.reco.MyCloset.ClothesActivity
import choijjyo.reco.Recognize.ClosetData
import choijjyo.reco.User.UsersData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object FirestoreHelper {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun setDocument(activity: AppCompatActivity, userId: String, data: UsersData) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .set(data)
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "Document set successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Error setting document", e)
            }
    }
    fun saveImageUrlToCloset(activity: AppCompatActivity, userId: String, imageName:String, imageData: ClosetData) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("closet")
            .document(imageName)
            .set(imageData)
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "Image URL saved to closet successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Error saving image URL to closet", e)
            }
    }

    fun deleteClothesDocFromFirestore(activity: AppCompatActivity, userId: String, imageName:String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("closet")
            .document(imageName)
            .delete()
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "DocumentSnapshot successfully deleted!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Error deleting document", e)
            }
    }

    fun loadImagesFromFirestoreForActivity(activity: AppCompatActivity, userId: String, recyclerView: RecyclerView) {
        val imageList = mutableListOf<String>()
        val colorCategoryList = mutableListOf<String>()
        val clothesList = mutableListOf<String>()

        firestore.collection("users").document(userId).collection("closet")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrl = document.getString("imgURL")
                    val colorCategory = document.getString("closetColorCategory")
                    val clothes = document.getString("clothes")
                    if (imageUrl != null && colorCategory != null && clothes != null) {
                        imageList.add(imageUrl)
                        colorCategoryList.add(colorCategory)
                        clothesList.add(clothes)
                    }
                }
                if (imageList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    val noImagesTextView = activity.findViewById<TextView>(R.id.noImagesTextView)
                    noImagesTextView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    val adapter = RecentImageAdapter(imageList, object : RecentImageAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val imageUrl = imageList[position]
                            val colorCategory = colorCategoryList[position]
                            val clothes = clothesList[position]
                            Log.d("FirestoreHelper", position.toString())
                            val intent = Intent(activity, ClothesActivity::class.java).apply {
                                putExtra("imageUrl", imageUrl)
                                putExtra("colorCategory", colorCategory)
                                putExtra("clothes", clothes)
                            }
                            activity.startActivity(intent)
                        }
                    })

                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = GridLayoutManager(activity, Constants.SPAN_COUNT)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("LoadImages", "Error getting documents: ", exception)
            }
    }

    fun loadImagesFromFirestoreForFragment(fragment: Fragment, userId: String, recyclerView: RecyclerView, categories: List<String>) {
        val imageList = mutableListOf<String>()
        val colorCategoryList = mutableListOf<String>()
        val clothesList = mutableListOf<String>()

        firestore.collection("users").document(userId).collection("closet")
            .whereIn("clothes", categories)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrl = document.getString("imgURL")
                    val colorCategory = document.getString("closetColorCategory")
                    val clothes = document.getString("clothes")

                    if (imageUrl != null && colorCategory != null && clothes != null) {
                        imageList.add(imageUrl)
                        colorCategoryList.add(colorCategory)
                        clothesList.add(clothes)
                    }
                }
                val adapter = ClosetImageAdapter(imageList, colorCategoryList, clothesList, object : ClosetImageAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val imageUrl = imageList[position]
                        val colorCategory = colorCategoryList[position]
                        val clothes = clothesList[position]
                        Log.d("FirestoreHelper", position.toString())
                        val intent = Intent(fragment.requireContext(), ClothesActivity::class.java).apply {
                            putExtra("imageUrl", imageUrl)
                            putExtra("colorCategory", colorCategory)
                            putExtra("clothes", clothes)
                        }
                        fragment.startActivity(intent)
                    }
                })
                recyclerView.adapter = adapter
                recyclerView.layoutManager = GridLayoutManager(fragment.requireContext(), 3)
            }
            .addOnFailureListener { exception ->
                Log.e("LoadImages", "Error getting documents: ", exception)
            }
    }
    fun saveSimilarUrlToCloset(activity: FragmentActivity?, userId: String, docId:String, imageData: HashMap<String, Any>) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("closet")
            .document(docId)
            .set(imageData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "Image URL saved to closet successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Error saving image URL to closet", e)
            }
    }

    fun saveUserColors(
        likeSelectedbuttons: MutableList<String>,
        dislikeSelectedbuttons: MutableList<String>
    ) {
        TODO("Not yet implemented")
    }

    object FirestoreHelper {
        private val firestore = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()

        // 사용자의 취향을 Firestore에 저장하는 함수
        suspend fun saveUserColors(likeColors: List<String>, dislikeColors: List<String>) {
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                val userData = hashMapOf(
                    "like_colors" to likeColors,
                    "dislike_colors" to dislikeColors
                )
                firestore.collection("users").document(userId).set(userData).await()
            } else {
                throw Exception("User not logged in")
            }
        }
    }

}