package choijjyo.reco.MyCloset

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.FirestoreHelper
import choijjyo.reco.R
import com.google.firebase.auth.FirebaseAuth

class Fragment_all : Fragment(), ClosetImageAdapter.OnItemClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.mycloset_all, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        recyclerView = view.findViewById(R.id.closet_all_view)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
            FirestoreHelper.loadImagesFromFirestoreForFragment(
                this,
                uid,
                recyclerView,
                mutableListOf("티셔츠", "원피스", "자켓", "바지", "셔츠", "반바지", "치마", "긴팔", "긴소매")
            )
        }
    }

    override fun onItemClick(position: Int) {
    }

}
