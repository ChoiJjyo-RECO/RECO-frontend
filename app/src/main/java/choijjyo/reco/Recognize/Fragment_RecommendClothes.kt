package choijjyo.reco.Recognize

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import choijjyo.reco.BuildConfig
import choijjyo.reco.R
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import kotlin.time.Duration.Companion.seconds

class Fragment_RecommendClothes: Fragment() {
    private lateinit var textview : TextView
    val client = OkHttpClient()
    val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    val adapter = moshi.adapter(GptResponse::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.recommend_clothes, container, false)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textview = view.findViewById(R.id.recommend_text)
        lifecycleScope.launch {
            try {
                searchOnGpt()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    textview.text = "추천을 불러오는 데 실패했습니다."
                }
            }
        }
    }
    private suspend fun searchOnGpt() {
        val openAi = OpenAI(
            token = BuildConfig.OPENAI_APIKEY,
            timeout = Timeout(socket = 60.seconds),
        )
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo-0125"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "초록색 니트에 어울릴 만한 하의를 6가지 추천해줘"
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = "Hello!"
                )
            )
        )
        val completion: ChatCompletion = openAi.chatCompletion(chatCompletionRequest)
        // 결과 처리
        withContext(Dispatchers.Main) {
            // UI 업데이트
            textview.text = completion.choices.firstOrNull()?.message?.content ?: "추천을 받아올 수 없습니다."
        }
    }
}