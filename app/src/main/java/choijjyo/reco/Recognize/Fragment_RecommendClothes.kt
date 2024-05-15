package choijjyo.reco.Recognize

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import choijjyo.reco.BuildConfig
import choijjyo.reco.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException

class Fragment_RecommendClothes: Fragment() {
//    val client = OkHttpClient()
//    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
//    val adapter = moshi.adapter(GptResponse::class.java)
//    val json = """
//    {
//        "prompt": "여기에 질문을 입력하세요",
//        "max_tokens": 60
//    }
//    """.trimIndent()
//    val request = Request.Builder()
//        .url("https://api.openai.com/v1/engines/davinci-codex/completions")
//        .addHeader("Authorization", BuildConfig.OPENAI_APIKEY) // 여기에 OpenAI 키를 입력하세요.
//        .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
//        .build()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recommend_clothes, container, false)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    private fun searchOnGpt(){
//        client.newCall(request).enqueue(object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//                    val responseBody = response.body?.string()
//                    val gptResponse = adapter.fromJson(responseBody)
//
//                    Handler(Looper.getMainLooper()).post {
//                        textView.text = gptResponse?.choices?.first()?.text
//                    }
//                }
//            }
//        })

    }
}