package choijjyo.reco

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Fragment_SimilarClothes : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.similar_clothes, container, false)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = requireView().findViewById(R.id.similarRecyclerView)
        adapter = SearchImageAdapter(listOf())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }
    fun updateUIWithNewKeyword(newKeyword: String){
        // Bundle에서 데이터 받기
        //val similarQuery = arguments?.getString("similarquery")


        // Retrofit 객체 생성 및 ApiService 인터페이스 초기화
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(GoogleCustomSearchAPI::class.java)

        // API 호출
        val call = newKeyword.let { apiService.searchImages(query = it, apiKey = BuildConfig.GOOGLESEARCH_APIKEY, searchEngineId = BuildConfig.GOOGLESEARCH_SEARCHID) }
        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    // API 응답으로부터 이미지 목록을 초기화
                    val imageList: List<SearchResultItem> = response.body()?.items?.map { apiItem ->
                        SearchResultItem(
                            imageUrl = apiItem.link,
                            clickUrl = apiItem.image.contextLink
                        )
                    } ?: listOf()

                    adapter.updateData(imageList)
                } else {
                    // 에러 처리
                    Log.e("API Error", "Response Code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                // 네트워크 에러 처리
                Log.e("API Error", "Network Error: ${t.message}")
            }
        })
    }

}