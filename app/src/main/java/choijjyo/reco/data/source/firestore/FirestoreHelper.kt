package choijjyo.reco.data.source.firestore

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R
import choijjyo.reco.data.entity.preference.PreferenceClothTypeData
import choijjyo.reco.data.entity.preference.PreferenceColorData
import choijjyo.reco.data.Constants
import choijjyo.reco.adapter.basic.RecyclerViewImageAdapter
import choijjyo.reco.view.myCloset.adapter.ClosetImageAdapter
import choijjyo.reco.adapter.search.ClosetSearchImageAdapter
import choijjyo.reco.view.myCloset.clothes.ClothesActivity
import choijjyo.reco.data.entity.closet.ClosetData
import choijjyo.reco.data.entity.search.SearchResultItem
import choijjyo.reco.data.entity.user.UsersData
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

object FirestoreHelper {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    interface OnPreferenceColorDataLoadedListener {
        fun onDataLoaded(preferenceColorData: PreferenceColorData?)
    }
    interface OnPreferenceClothTypeDataLoadedListener {
        fun onDataLoaded(preferenceClothTypeData: PreferenceClothTypeData?)
    }


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
        val imageNameList = mutableListOf<String>()
        val imageUrlList = mutableListOf<String>()
        val colorCategoryList = mutableListOf<String>()
        val clothesList = mutableListOf<String>()

        firestore.collection("users").document(userId).collection("closet")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageName = document.reference.path.split("/").last()
                    val imageUrl = document.getString("imgURL")
                    val colorCategory = document.getString("closetColorCategory")
                    val clothes = document.getString("clothes")
                    if (imageUrl != null && colorCategory != null && clothes != null) {
                        imageNameList.add(imageName)
                        imageUrlList.add(imageUrl)
                        colorCategoryList.add(colorCategory)
                        clothesList.add(clothes)
                    }
                }
                if (imageUrlList.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    val noImagesTextView = activity.findViewById<TextView>(R.id.noImagesTextView)
                    noImagesTextView.visibility = View.VISIBLE
                } else {
                    recyclerView.visibility = View.VISIBLE
                    val adapter = RecyclerViewImageAdapter(imageUrlList, object : RecyclerViewImageAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val imageName = imageNameList[position]
                            val imageUrl = imageUrlList[position]
                            val colorCategory = colorCategoryList[position]
                            val clothes = clothesList[position]
                            val intent = Intent(activity, ClothesActivity::class.java).apply {
                                putExtra("imageName", imageName)
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
        val imageNameList = mutableListOf<String>()
        val imageUrlList = mutableListOf<String>()
        val colorCategoryList = mutableListOf<String>()
        val clothesList = mutableListOf<String>()

        firestore.collection("users").document(userId).collection("closet")
            .whereIn("clothes", categories)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageName = document.reference.path.split("/").last()
                    val imageUrl = document.getString("imgURL")
                    val colorCategory = document.getString("closetColorCategory")
                    val clothes = document.getString("clothes")

                    if (imageUrl != null && colorCategory != null && clothes != null) {
                        imageNameList.add(imageName)
                        imageUrlList.add(imageUrl)
                        colorCategoryList.add(colorCategory)
                        clothesList.add(clothes)
                    }
                }
                val adapter = ClosetImageAdapter(imageUrlList, colorCategoryList, clothesList, object : ClosetImageAdapter.OnItemClickListener {
                    override fun onItemClick(position: Int) {
                        val imageName = imageNameList[position]
                        val imageUrl = imageUrlList[position]
                        val colorCategory = colorCategoryList[position]
                        val clothes = clothesList[position]
                        val intent = Intent(fragment.requireContext(), ClothesActivity::class.java).apply {
                            putExtra("imageName", imageName)
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

    fun loadSimilarImages(fragment: Fragment, userId: String, imageName: String, recyclerView: RecyclerView) {
        val imageUrlList = mutableListOf<String>()
        val clickUrlList = mutableListOf<String>()

        firestore.collection("users").document(userId).collection("closet")
            .document(imageName).collection("similarClothes")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl")
                    val clickUrl = document.getString("clickUrl")
                    if (imageUrl != null && clickUrl != null) {
                        imageUrlList.add(imageUrl)
                        clickUrlList.add(clickUrl)
                    }
                }
                val adapter = ClosetSearchImageAdapter(imageUrlList, clickUrlList)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = GridLayoutManager(fragment.requireContext(), 3)
            }
            .addOnFailureListener { exception ->
                Log.e("LoadImages", "Error getting documents: ", exception)
            }
    }
    fun loadRecommendImages(fragment: Fragment, userId: String, imageName: String, recyclerView: RecyclerView) {
        val imageUrlList = mutableListOf<String>()
        val clickUrlList = mutableListOf<String>()

        firestore.collection("users").document(userId).collection("closet")
            .document(imageName).collection("recommendClothes")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val imageUrl = document.getString("imageUrl")
                    val clickUrl = document.getString("clickUrl")
                    if (imageUrl != null && clickUrl != null) {
                        imageUrlList.add(imageUrl)
                        clickUrlList.add(clickUrl)
                    }
                }
                val adapter = ClosetSearchImageAdapter(imageUrlList, clickUrlList)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = GridLayoutManager(fragment.requireContext(), 3)
            }
            .addOnFailureListener { exception ->
                Log.e("LoadImages", "Error getting documents: ", exception)
            }
    }

    fun loadPreferenceColor(activity: FragmentActivity?, userId: String, listener: OnPreferenceColorDataLoadedListener) {
        firestore.collection("users")
            .document(userId)
            .collection("preference")
            .document("color")
            .get()
            .addOnSuccessListener { document ->
                val preferenceColorData = document.toObject(PreferenceColorData::class.java)
                listener.onDataLoaded(preferenceColorData)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "선호 색 불러오기 실패", e)
                listener.onDataLoaded(null)
            }
    }

    fun loadPreferenceClothType(activity: FragmentActivity?, userId: String, listener: OnPreferenceClothTypeDataLoadedListener) {
        firestore.collection("users")
            .document(userId)
            .collection("preference")
            .document("clothType")
            .get()
            .addOnSuccessListener { document ->
                val preferenceClothTypeData = document.toObject(PreferenceClothTypeData::class.java)
                listener.onDataLoaded(preferenceClothTypeData)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "선호 옷 유형 불러오기 실패", e)
                listener.onDataLoaded(null)
            }
    }

    fun saveSimilarUrlToCloset(activity: FragmentActivity?, userId: String, docId:String, searchData: SearchResultItem, index: Int) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("closet")
            .document(docId)
            .collection("similarClothes")
            .document(index.toString())
            .set(searchData)
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "Image URL saved to closet successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Error saving image URL to closet", e)
            }
    }
    fun saveRecommendUrlToCloset(activity: FragmentActivity?, userId: String, docId:String, searchData: SearchResultItem, index: Int) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("closet")
            .document(docId)
            .collection("recommendClothes")
            .document(index.toString())
            .set(searchData)
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "Image URL saved to closet successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Error saving image URL to closet", e)
            }
    }

    fun savePreferenceColor(activity: FragmentActivity?, userId: String, preferenceColorData: PreferenceColorData): Task<Void> {
        val taskCompletionSource = TaskCompletionSource<Void>()
        firestore.collection("users")
            .document(userId)
            .collection("preference")
            .document("color")
            .set(preferenceColorData)
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "Color selections saved successfully!")
                taskCompletionSource.setResult(null)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Error saving color selections", e)
                taskCompletionSource.setException(e)
            }
        return taskCompletionSource.task
    }

    fun savePreferenceClothType(activity: FragmentActivity?, userId: String, preferenceClothTypeData: PreferenceClothTypeData): Task<Void> {
        val taskCompletionSource = TaskCompletionSource<Void>()
        firestore.collection("users")
            .document(userId)
            .collection("preference")
            .document("clothType")
            .set(preferenceClothTypeData)
            .addOnSuccessListener {
                Log.d("FirestoreHelper", "Color selections saved successfully!")
                taskCompletionSource.setResult(null)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Error saving color selections", e)
                taskCompletionSource.setException(e)
            }
        return taskCompletionSource.task
    }
}