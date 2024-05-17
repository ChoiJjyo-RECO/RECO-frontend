package choijjyo.reco.view.preference
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.fragment.app.Fragment
import choijjyo.reco.data.source.firestore.FirestoreHelper

import choijjyo.reco.R
import choijjyo.reco.data.entity.preference.PreferenceColorData
import com.google.firebase.auth.FirebaseAuth

class PreferenceColorFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    private val like_colorButtonIds = arrayOf(
        R.id.like_colorButton1, R.id.like_colorButton2, R.id.like_colorButton3, R.id.like_colorButton4,
        R.id.like_colorButton5, R.id.like_colorButton6, R.id.like_colorButton7, R.id.like_colorButton8,
        R.id.like_colorButton9, R.id.like_colorButton10, R.id.like_colorButton11, R.id.like_colorButton12,
        R.id.like_colorButton13, R.id.like_colorButton14, R.id.like_colorButton15, R.id.like_colorButton16,
        R.id.like_colorButton17, R.id.like_colorButton18, R.id.like_colorButton19, R.id.like_colorButton20
    )

    private val dislike_colorButtonIds = arrayOf(
        R.id.dislike_colorButton1, R.id.dislike_colorButton2, R.id.dislike_colorButton3, R.id.dislike_colorButton4,
        R.id.dislike_colorButton5, R.id.dislike_colorButton6, R.id.dislike_colorButton7, R.id.dislike_colorButton8,
        R.id.dislike_colorButton9, R.id.dislike_colorButton10, R.id.dislike_colorButton11, R.id.dislike_colorButton12,
        R.id.dislike_colorButton13, R.id.dislike_colorButton14, R.id.dislike_colorButton15, R.id.dislike_colorButton16,
        R.id.dislike_colorButton17, R.id.dislike_colorButton18, R.id.dislike_colorButton19, R.id.dislike_colorButton20
    )

    private val like_selectedButtons = mutableListOf<String>()
    private val dislike_selectedButtons = mutableListOf<String>()
    private lateinit var like_selectedButtonsTextView: TextView
    private lateinit var dislike_selectedButtonsTextView: TextView
    private lateinit var saveButton: Button
    private var customDialog: ShowColorExplain? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_preference_color, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            uid = currentUser.uid
        }

        // 선택된 버튼을 표시할 TextView 초기화
        like_selectedButtonsTextView = view.findViewById(R.id.like_selectedButtonsTextView)
        dislike_selectedButtonsTextView = view.findViewById(R.id.dislike_selectedButtonsTextView)
        saveButton = view.findViewById(R.id.save_colorlike)

        // 첫 번째 TableLayout에 대한 버튼 설정
        setupButtons(view.findViewById(R.id.like_color_buttonlayout), like_colorButtonIds, like_selectedButtons)

        // 두 번째 TableLayout에 대한 버튼 설정
        setupButtons(view.findViewById(R.id.dislike_color_buttonlayout), dislike_colorButtonIds, dislike_selectedButtons)

        saveButton.setOnClickListener {
            saveSelectedButtonsToFirestore()
        }

        loadPreferenceFromFirestore()
    }

    // TableLayout에 버튼 설정하는 함수
    private fun setupButtons(tableLayout: TableLayout, buttonIds: Array<Int>, selectedButtons: MutableList<String>) {

        val colorDescriptions = mapOf(
            "검정색" to "아무런 색도 없이 어두운 색으로, 깊고 신비로운 느낌을 줍니다.\n\n눈을 감았을 때에 느껴지는 어둠을 상상해 보세요.",
            "하얀색" to "아무 색도 섞이지 않은 순수한 색으로, 깨끗하고 차분한 느낌을 줍니다.\n\n순수함과 청결을 상징하며, 밝고 평화로운 이미지를 상상해 보세요.",
            "회색" to "검은색과 흰색이 섞인 색으로, 중립적이고 차분한 느낌을 줍니다.\n\n안정감과 신뢰를 상징하며, 조용하고 고요한 이미지를 상상해 보세요.",
            "빨간색" to "열정과 활력을 상징하는 밝고 강렬한 색으로, 감정적인 느낌을 줍니다.\n\n사랑과 흥분, 강렬한 분위기를 상징하며,\n뜨겁고 타오르는 이미지를 상상해 보세요.",
            "핑크색" to "연인의 사랑 또는 사랑스러움을 상징하는 부드럽고 로맨틱한 색으로,\n설렘, 로맨틱하거나 달콤한 느낌을 줍니다.\n\n사랑에 관련된 로맨틱한 이미지를 상상해 보세요.",
            "주황색" to "활기찬 활력과 따뜻한 느낌을 주는 밝은 색으로,\n열정과 활동적인 분위기를 상징합니다.\n\n따뜻하고 활기찬 이미지를 상상해 보세요.",
            "베이지" to "온화하고 부드러운 느낌을 주는 발연한 갈색의 변형으로,\n은은한 안정감과 차분함을 상징합니다.\n\n부드럽고 따뜻한 이미지를 상상해 보세요.",
            "갈색" to "대부분의 나무와 흙의 색상과 비슷한 색으로, 안정감과 신뢰를 상징합니다.\n\n자연스러우며 진중한 이미지를 상상해 보세요.",
            "노랑색" to "따뜻하고 행복한 느낌과 활기찬 색으로, 즐거움과 활력을 상징합니다.\n\n따뜻한 햇빛, 햇살 가득한 들판의 꽃들, 화려한 향기 등을 상상해보세요.",
            "초록색" to "자연과 생명을 상징하는 색으로, 신선하고 안정된 느낌을 줍니다.\n\n싱그러운 푸르름과 생명력 넘치는 이미지를 상상해 보세요.",
            "카키색" to "녹색과 갈색이 섞인 색으로, 중립적이면서도 세련된 느낌을 줍니다.\n\n크하고 차분한 이미지를 상상해 보세요.",
            "민트색" to "상쾌하고 시원한 느낌을 주는 연한 초록색으로,\n청량감과 신선함을 상징합니다.\n\n상쾌한 이미지나 허브향의 향을 이미지로 상상해 보세요.",
            "파란색" to "하늘과 바다의 색으로, 안정감과 평온함을 상징합니다.\n\n불어오는 바람의 이미지나 맑고 깨끗한 이미지를 상상해 보세요.",
            "남색" to "진한 파란색의 변형으로, 깊은 신뢰와 안정감을 상징합니다.\n\n깊은 생각과 차분한 이미지를 상상해 보세요.",
            "청색" to "밝고 생기 넘치는 파란색으로, 청량하고 깨끗한 느낌을 줍니다.\n\n청명하고 맑은 이미지를 상상해 보세요.",
            "하늘색" to "맑고 청명한 느낌의 색으로, 신선하고 평온한 느낌을 줍니다.\n\n맑고 고요한 하늘의 이미지를 상상해 보세요.",
            "보라색" to "신비로움과 우아함을 상징하는 색으로, 고귀하고 로맨틱한 느낌을 줍니다.\n\n우아하고 신비로운 이미지를 상상해 보세요.",
            "라벤더" to "연한 보라색의 변형으로, 부드럽고 우아한 느낌을 줍니다.\n\n로맨틱하고 부드러운 이미지를 상상해 보세요.",
            "와인색" to "깊고 풍부한 보라색의 변형으로, 고급스러우며 우아한 느낌을 줍니다.\n\n고급스럽고 고혹적인 이미지를 상상해 보세요.",
            "네온" to "선명하고 발랄한 느낌을 주는 밝은 색으로,\n주로 현대적이고 활동적인 분위기를 연상시킵니다.\n\n활기찬 이미지를 상상해 보세요."
        )
        for (buttonId in buttonIds) {
            val button = tableLayout.findViewById<Button>(buttonId)
            button.setOnClickListener { toggleButtonSelection(button, selectedButtons) }
            button.setOnLongClickListener {
                val colorName = button.text.toString()
                val colorDescription = colorDescriptions[colorName] ?: ""
                customDialog = ShowColorExplain(requireContext(), button.text.toString(), colorDescription)
                customDialog?.show()
                customDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

                true
            }
        }
    }

    private fun toggleButtonSelection(button: Button, selectedButtons: MutableList<String>) {
        val text = button.text.toString()

        if (selectedButtons.contains(text)) {
            selectedButtons.remove(text)
            button.isSelected = false
        } else {
            if (selectedButtons.size < 5) {
                selectedButtons.add(text)
                button.isSelected = true
            } else {
                // 5개를 초과하는 경우
                Toast.makeText(requireContext(), "최대 5개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 선택된 버튼을 표시하는 TextView 업데이트
        updateSelectedButtonsText()
    }

    // 선택된 버튼을 표시하는 TextView를 업데이트하는 함수
    private fun updateSelectedButtonsText() {
        val likeSelectedText = like_selectedButtons.joinToString(", ")
        like_selectedButtonsTextView.text = "선택된 버튼(좋아하는 색상): $likeSelectedText"

        val dislikeSelectedText = dislike_selectedButtons.joinToString(", ")
        dislike_selectedButtonsTextView.text = "선택된 버튼(싫어하는 색상): $dislikeSelectedText"
    }

    private fun saveSelectedButtonsToFirestore() {
        val preferenceColorData = PreferenceColorData(
            colorLikeList = like_selectedButtons,
            colorDislikeList = dislike_selectedButtons
        )
        FirestoreHelper.savePreferenceColor(activity, uid, preferenceColorData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "선택한 선호 색상이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "선택한 선호 색상을 저장하는 중에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPreferenceFromFirestore() {
        FirestoreHelper.loadPreferenceColor(activity, uid, object : FirestoreHelper.OnPreferenceColorDataLoadedListener {
            override fun onDataLoaded(preferenceColorData: PreferenceColorData?) {
                preferenceColorData?.let { data ->
                    like_selectedButtons.clear()
                    like_selectedButtons.addAll(data.colorLikeList)

                    dislike_selectedButtons.clear()
                    dislike_selectedButtons.addAll(data.colorDislikeList)

                    updateSelectedButtonsText()
                }
            }
        })
    }
}
