package choijjyo.reco

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import choijjyo.reco.databinding.ActivityRecognizeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

import com.bumptech.glide.Glide

class ClosetActivity : AppCompatActivity() {
    private lateinit var resultText: TextView
    lateinit var clotheImg : ImageView
    lateinit var button: Button
    private lateinit var binding: ClosetActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_closet)

        resultText = findViewById(R.id.resultText)
        clotheImg = findViewById(R.id.showimage)


        //통신시작
        GlobalScope.launch(Dispatchers.IO){
            request()
        }

    }

    private fun request() {
        try {
            val uid = intent.getStringExtra("userUid")
            Log.d("uid", "uid"+uid)
            //val uid = "q9u1gUypngbpZbKny5vwqDwm6qT2"
            val docId = "20240510"

            // 요청 URL에 쿼리 매개변수 추가
            val url = URL("https://b4f3-121-166-22-33.ngrok-free.app/detect_and_analyze?uid=$uid&doc_id=$docId")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10000
            conn.requestMethod = "GET"
            conn.doInput = true

            val resCode = conn.responseCode
            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val response = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                response.append(line).append("\n")
            }
            reader.close()
            conn.disconnect()

            // JSON 파싱
            val jsonResponse = JSONObject(response.toString())
            val objectClass = jsonResponse.getString("object_class")
            val closestColorCategory = jsonResponse.getString("closest_color_category")
            val imgURL = jsonResponse.getString("image_URL")

            // 결과 표시
            runOnUiThread {
                resultText.text = "Object Class: $objectClass\nClosest Color Category: $closestColorCategory"
                Glide.with(this@ClosetActivity)
                    .load(imgURL)
                    .into(clotheImg)
            }
        } catch (ex: Exception) {
            println("예외 발생함: ${ex.toString()}")
        }
    }
}
