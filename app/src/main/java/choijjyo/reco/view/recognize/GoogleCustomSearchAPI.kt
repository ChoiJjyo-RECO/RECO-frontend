package choijjyo.reco.view.recognize

import choijjyo.reco.data.entity.search.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface GoogleCustomSearchAPI {
    @GET("customsearch/v1")
    fun searchImages(
        @Query("key") apiKey: String,
        @Query("cx") searchEngineId: String,
        @Query("q") query: String,
        @Query("searchType") searchType: String = "image",
        @Query("num") num: Int = 6
    ): Call<SearchResponse>
}