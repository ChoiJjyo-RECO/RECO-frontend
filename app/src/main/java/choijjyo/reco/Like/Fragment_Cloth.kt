package choijjyo.reco.Like
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

import choijjyo.reco.R

class Fragment_Cloth : Fragment() {

    private val like_clothButtonIds = arrayOf(
        R.id.like_clothButton1, R.id.like_clothButton2, R.id.like_clothButton3, R.id.like_clothButton4,
        R.id.like_clothButton5, R.id.like_clothButton6, R.id.like_clothButton7, R.id.like_clothButton8
    )

    private val dislike_clothButtonIds = arrayOf(
        R.id.dislike_clothButton1, R.id.dislike_clothButton2, R.id.dislike_clothButton3, R.id.dislike_clothButton4,
        R.id.dislike_clothButton5, R.id.dislike_clothButton6, R.id.dislike_clothButton7, R.id.dislike_clothButton8
    )

    private val like_selectedButtons = mutableListOf<String>()
    private val dislike_selectedButtons = mutableListOf<String>()
    private lateinit var like_selectedButtonsTextView: TextView
    private lateinit var dislike_selectedButtonsTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.like_cloth, container, false)

        // 선택된 버튼을 표시할 TextView 초기화
        like_selectedButtonsTextView = view.findViewById(R.id.like_selectedButtonsTextView)
        dislike_selectedButtonsTextView = view.findViewById(R.id.dislike_selectedButtonsTextView)

        // 첫 번째 TableLayout에 대한 버튼 설정
        setupButtons(view.findViewById(R.id.like_cloth_buttonlayout), like_clothButtonIds, like_selectedButtons)

        // 두 번째 TableLayout에 대한 버튼 설정
        setupButtons(view.findViewById(R.id.dislike_cloth_buttonlayout), dislike_clothButtonIds, dislike_selectedButtons)

        return view
    }

    // TableLayout에 버튼 설정하는 함수
    private fun setupButtons(tableLayout: TableLayout, buttonIds: Array<Int>, selectedButtons: MutableList<String>) {
        for (buttonId in buttonIds) {
            val button = tableLayout.findViewById<Button>(buttonId)
            button.setOnClickListener { toggleButtonSelection(button, selectedButtons) }
        }
    }

    private fun toggleButtonSelection(button: Button, selectedButtons: MutableList<String>) {
        val text = button.text.toString()

        if (selectedButtons.contains(text)) {
            selectedButtons.remove(text)
            button.isSelected = false
        } else {
            if (selectedButtons.size < 3) {
                selectedButtons.add(text)
                button.isSelected = true
            } else {
                // 3개를 초과하는 경우
                Toast.makeText(requireContext(), "최대 3개까지 선택 가능합니다.", Toast.LENGTH_SHORT).show()
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
}
