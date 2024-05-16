package choijjyo.reco.view.recognize

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.BuildConfig
import choijjyo.reco.data.source.firestore.FirestoreHelper
import choijjyo.reco.data.entity.preference.PreferenceClothTypeData
import choijjyo.reco.data.entity.preference.PreferenceColorData
import choijjyo.reco.R
import choijjyo.reco.adapter.search.RecommendImageAdapter
import choijjyo.reco.data.entity.search.SearchResponse
import choijjyo.reco.data.entity.search.SearchResultItem
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.time.Duration.Companion.seconds

class RecognizeRecommendClothesFragment: Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: RecommendImageAdapter
    private var searchKeyword: String? = null
    private var googleSearchKeyword: String? = null
    private var docId: String? = null
    private var colorLike = mutableListOf<String>()
    private var colorDisLike = mutableListOf<String>()
    private var clothesLike = mutableListOf<String>()
    private var clothesDisLike = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recommend_clothes, container, false)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recommendRecyclerView)
        imageAdapter = RecommendImageAdapter(listOf())
        recyclerView.adapter = imageAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

    }
    fun setSearchKeyword(keyword: String, docid: String) {
        searchKeyword = keyword
        docId = docid
        lifecycleScope.launch {
            try {
                searchOnGpt(keyword, docId!!)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("openAi API Error", "추천을 불러오는 데 실패했습니다.")
                }
            }
        }
    }
    private suspend fun searchOnGpt(keyword: String, docId: String) {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
            FirestoreHelper.loadPreferenceColor(activity, uid, object : FirestoreHelper.OnPreferenceColorDataLoadedListener {
                override fun onDataLoaded(preferenceColorData: PreferenceColorData?) {
                    preferenceColorData?.let { data ->

                        colorLike = data.colorLikeList.toMutableList()
                        colorDisLike = data.colorDislikeList.toMutableList()

                    }
                }
            })
            FirestoreHelper.loadPreferenceClothType(activity, uid, object : FirestoreHelper.OnPreferenceClothTypeDataLoadedListener {
                override fun onDataLoaded(preferenceClothTypeData: PreferenceClothTypeData?) {
                    preferenceClothTypeData?.let { data ->

                        clothesLike = data.clothTypeLikeList.toMutableList()
                        clothesDisLike = data.clothTypeDislikeList.toMutableList()

                    }
                }
            })
        }
        val openAi = OpenAI(
            token = BuildConfig.OPENAI_APIKEY,
            timeout = Timeout(socket = 60.seconds),
        )
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo-0125"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "사용자가 선호하는 색상은 $colorLike , 싫어하는 색상은 $colorDisLike 입니다. 사용자가 선호하는 옷 종류는 $clothesLike 이고, 싫어하는 옷 종류는 $clothesDisLike 입니다. 사용자의 취향을 고려해서 옷을 추천해주세요."
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = "$keyword 에 어울릴 만한 옷을 6가지  추천해줘.\\n답변 예시는 다음과 같습니다: \"검은색 치마\" OR \"하늘색 바지\" OR \"핑크색 셔츠\" OR \"주황색 티셔츠\" OR \"파란색 셔츠\" OR \"검은색 자켓\". \\n이 형식을 따라 답변해주세요. "
                )
            )
        )
        Log.d("챗지피티", "working")
        try {
            val completion: ChatCompletion = openAi.chatCompletion(chatCompletionRequest)
            googleSearchKeyword = completion.choices.firstOrNull()?.message?.content ?: "추천을 받아올 수 없습니다."
            googleSearchKeyword += " 상품 사진"
            Log.d("추천상품 검색 키워드", googleSearchKeyword!!)
            searchImages(googleSearchKeyword!!, docId)
        } catch (e: Exception) {
            Log.e("챗지피티", "오류 발생: ${e.message}")
        }

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
                    imageAdapter.updateData(imageList)
                    if (currentUser != null) {
                        uid = currentUser.uid
                        imageList.forEachIndexed { index, searchResultItem ->
                            FirestoreHelper.saveRecommendUrlToCloset(
                                activity,
                                uid,
                                docId,
                                searchResultItem,
                                index + 1
                            )
                        }
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