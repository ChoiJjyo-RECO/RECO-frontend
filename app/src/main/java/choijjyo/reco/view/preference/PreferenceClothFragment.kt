package choijjyo.reco.view.preference
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import choijjyo.reco.data.source.firestore.FirestoreHelper

import choijjyo.reco.R
import choijjyo.reco.data.entity.preference.PreferenceClothTypeData
import com.google.firebase.auth.FirebaseAuth

class PreferenceClothFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

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
    private lateinit var saveButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.preference_cloth_type, container, false)
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
        saveButton = view.findViewById(R.id.save_clothlike)

        // 첫 번째 TableLayout에 대한 버튼 설정
        setupButtons(view.findViewById(R.id.like_cloth_buttonlayout), like_clothButtonIds, like_selectedButtons)

        // 두 번째 TableLayout에 대한 버튼 설정
        setupButtons(view.findViewById(R.id.dislike_cloth_buttonlayout), dislike_clothButtonIds, dislike_selectedButtons)

        saveButton.setOnClickListener {
            saveSelectedButtonsToFirestore()
        }

        loadPreferenceFromFirestore()
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
        like_selectedButtonsTextView.text = "선택된 버튼(좋아하는 옷 종류): $likeSelectedText"

        val dislikeSelectedText = dislike_selectedButtons.joinToString(", ")
        dislike_selectedButtonsTextView.text = "선택된 버튼(싫어하는 욧 종류): $dislikeSelectedText"
    }

    private fun saveSelectedButtonsToFirestore() {
        val preferenceClothTypeData = PreferenceClothTypeData(
            clothTypeLikeList = like_selectedButtons,
            clothTypeDislikeList = dislike_selectedButtons
        )
        FirestoreHelper.savePreferenceClothType(activity, uid, preferenceClothTypeData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "선택한 선호 옷 유형이 저장되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "선택한 선호 옷 유형을 저장하는 중에 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadPreferenceFromFirestore() {
        FirestoreHelper.loadPreferenceClothType(activity, uid, object : FirestoreHelper.OnPreferenceClothTypeDataLoadedListener {
            override fun onDataLoaded(preferenceClothTypeData: PreferenceClothTypeData?) {
                preferenceClothTypeData?.let { data ->
                    like_selectedButtons.clear()
                    like_selectedButtons.addAll(data.clothTypeLikeList)

                    dislike_selectedButtons.clear()
                    dislike_selectedButtons.addAll(data.clothTypeDislikeList)

                    updateSelectedButtonsText()
                }
            }
        })
    }
}
