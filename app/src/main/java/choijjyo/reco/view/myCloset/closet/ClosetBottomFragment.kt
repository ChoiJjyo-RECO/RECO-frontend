package choijjyo.reco.view.myCloset.closet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.data.source.firestore.FirestoreHelper
import choijjyo.reco.R
import choijjyo.reco.view.myCloset.adapter.ClosetImageAdapter
import com.google.firebase.auth.FirebaseAuth

class ClosetBottomFragment : Fragment(), ClosetImageAdapter.OnItemClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_closet_bottom, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.closet_bottom_view)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
            FirestoreHelper.loadImagesFromFirestoreForFragment(
                this,
                uid,
                recyclerView,
                mutableListOf("바지", "반바지", "치마")
            )
        }
    }

    override fun onItemClick(position: Int) {
    }

}
