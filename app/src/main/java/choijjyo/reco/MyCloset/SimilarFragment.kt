package choijjyo.reco.MyCloset

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.FirestoreHelper
import choijjyo.reco.R
import com.google.firebase.auth.FirebaseAuth

class SimilarFragment(private val itemImageName: String?) : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_similar, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.closet_similar_view)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
            if (itemImageName != null) {
                FirestoreHelper.loadSimilarImages(this, uid, itemImageName, recyclerView)
            }
        }
    }
}