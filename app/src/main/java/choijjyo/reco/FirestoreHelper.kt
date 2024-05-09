package choijjyo.reco

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

object FirestoreHelper {
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
}