package choijjyo.reco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class Fragment_top : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mycloset_top, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.closet_top_view)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
            FirestoreHelper.loadImagesFromFirestoreForFragment(this, uid, recyclerView, mutableListOf("티셔츠", "셔츠", "긴팔", "긴소매"))
        }
    }
}
