package choijjyo.reco

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Fragment_SimilarClothes : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchImageAdapter
    private var searchKeyword: String? = null
    private var docId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.similar_clothes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.similarRecyclerView)
        adapter = SearchImageAdapter(listOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

    }
    fun setSearchKeyword(keyword: String, docid: String) {
        searchKeyword = keyword
        docId = docid
        searchImages(keyword, docid)
    }

    private fun searchImages(googleSearchKeyword: String, docId: String) {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GoogleCustomSearchAPI::class.java)

        val call = apiService.searchImages(
            query = googleSearchKeyword,
            apiKey = BuildConfig.GOOGLESEARCH_APIKEY,
            searchEngineId = BuildConfig.GOOGLESEARCH_SEARCHID
        )

        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    val imageList: List<SearchResultItem> = response.body()?.items?.map { apiItem ->
                        SearchResultItem(
                            imageUrl = apiItem.link,
                            clickUrl = apiItem.image.contextLink
                        )
                    } ?: listOf()
                    adapter.updateData(imageList)
                    val imageData = hashMapOf<String, Any>()
                    imageList.forEachIndexed { index, searchResultItem ->
                        imageData["similarImageUrl${index + 1}"] = searchResultItem.imageUrl ?: ""
                        imageData["similarClickUrl${index + 1}"] = searchResultItem.clickUrl ?: ""
                    }
                    if (currentUser != null) {
                        uid = currentUser.uid
                        FirestoreHelper.saveSimilarUrlToCloset(activity,uid,docId,imageData)
                    }
                } else {
                    Log.e("API Error", "Response Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.e("API Error", "Network Error: ${t.message}")
            }
        })
    }
}
