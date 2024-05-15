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

import choijjyo.reco.FirestoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Fragment_Color : Fragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.like_color, container, false)

        // 선택된 버튼을 표시할 TextView 초기화
        like_selectedButtonsTextView = view.findViewById(R.id.like_selectedButtonsTextView)
        dislike_selectedButtonsTextView = view.findViewById(R.id.dislike_selectedButtonsTextView)

        // 첫 번째 TableLayout에 대한 버튼 설정
        setupButtons(view.findViewById(R.id.like_color_buttonlayout), like_colorButtonIds, like_selectedButtons)

        // 두 번째 TableLayout에 대한 버튼 설정
        setupButtons(view.findViewById(R.id.dislike_color_buttonlayout), dislike_colorButtonIds, dislike_selectedButtons)

        // 저장 버튼 클릭 시 취향을 저장
        view.findViewById<Button>(R.id.save_colorlike).setOnClickListener {
            saveUserColors()
        }

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

    // 사용자의 취향을 저장하는 함수
    private fun saveUserColors() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                FirestoreHelper.saveUserColors(like_selectedButtons, dislike_selectedButtons)
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "취향이 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "취향을 저장하는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
