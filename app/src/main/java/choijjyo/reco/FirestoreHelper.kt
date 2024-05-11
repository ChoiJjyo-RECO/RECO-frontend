package choijjyo.reco

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

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

    fun loadImagesFromFirestore(activity: AppCompatActivity, userId: String, recyclerView: RecyclerView) {
        val imageList = mutableListOf<String>()

        firestore.collection("users").document(userId).collection("closet")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrl = document.getString("imgURL")
                    if (imageUrl != null) {
                        imageList.add(imageUrl)
                    }
                }
                val adapter = RecentImageAdapter(imageList)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = GridLayoutManager(activity, 3)
            }
            .addOnFailureListener { exception ->
                Log.e("LoadImages", "Error getting documents: ", exception)
            }
    }

    fun loadImagesFromFirestoreForFragment(fragment: Fragment, userId: String, recyclerView: RecyclerView) {
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
                val adapter = ClosetImageAdapter(imageList, colorCategoryList, clothesList)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = GridLayoutManager(fragment.requireContext(), 3)
            }
            .addOnFailureListener { exception ->
                Log.e("LoadImages", "Error getting documents: ", exception)
            }
    }
}