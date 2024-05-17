package choijjyo.reco.view.myCloset.closet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.data.source.firestore.FirestoreHelper
import choijjyo.reco.R
import choijjyo.reco.adapter.closet.ClosetItemImageAdapter
import com.google.firebase.auth.FirebaseAuth

class ClosetOnepieceFragment : Fragment(), ClosetItemImageAdapter.OnItemClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_closet_onepiece, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.closet_onepiece_view)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
            FirestoreHelper.loadImagesFromFirestoreForFragment(
                this,
                uid,
                recyclerView,
                mutableListOf("원피스")
            )
        }
    }

    override fun onItemClick(position: Int) {
    }

}
