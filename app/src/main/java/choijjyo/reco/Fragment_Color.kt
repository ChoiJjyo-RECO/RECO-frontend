package choijjyo.reco

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R

class Fragment_Color : Fragment() {

    private val colors = mutableListOf<Pair<String, Int>>()
    private val selectedColors = mutableListOf<Pair<String, Int>>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ColorAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.like_color, container, false)

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 색상 데이터 설정
        setColors()

        // RecyclerView 어댑터 설정
        adapter = ColorAdapter(colors)
        recyclerView.adapter = adapter

        return view
    }

    // 색상 데이터 설정
    private fun setColors() {
        colors.add(Pair("검정색", Color.BLACK))
        colors.add(Pair("흰색", Color.WHITE))
        colors.add(Pair("회색", Color.GRAY))
        colors.add(Pair("빨간색", Color.RED))
        colors.add(Pair("자주색", Color.MAGENTA))
        colors.add(Pair("주황색", Color.rgb(255, 165, 0))) // Orange
        colors.add(Pair("베이지", Color.rgb(255, 228, 181))) // Beige
        colors.add(Pair("갈색", Color.rgb(139, 69, 19))) // Brown
        colors.add(Pair("노란색", Color.YELLOW))
        colors.add(Pair("초록색", Color.GREEN))
        colors.add(Pair("카키색", Color.rgb(189, 183, 107))) // Khaki
        colors.add(Pair("연한 파란색", Color.rgb(173, 216, 230))) // Light blue
        colors.add(Pair("파란색", Color.BLUE))
        colors.add(Pair("보라색", Color.rgb(138, 43, 226))) // Blue violet
        colors.add(Pair("연한 보라색", Color.rgb(100, 149, 237))) // Cornflower blue
        colors.add(Pair("연한 분홍색", Color.rgb(255, 182, 193))) // Light pink
        colors.add(Pair("라벤더", Color.rgb(204, 204, 255))) // Lavender
        colors.add(Pair("진한 분홍색", Color.rgb(255, 105, 180))) // Hot pink
        colors.add(Pair("청록색", Color.CYAN))
        colors.add(Pair("금색", Color.rgb(255, 215, 0))) // Gold
    }

    // RecyclerView 어댑터
    inner class ColorAdapter(private val colorList: List<Pair<String, Int>>) :
        RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.color_item, parent, false)
            return ColorViewHolder(view)
        }

        override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
            val (colorName, colorCode) = colorList[position]
            holder.bind(colorName, colorCode)
        }

        override fun getItemCount(): Int {
            return colorList.size
        }

        inner class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val button: Button = itemView.findViewById(R.id.colorButton)

            init {
                button.setOnClickListener {
                    val position = adapterPosition
                    val (colorName, colorCode) = colorList[position]
                    if (selectedColors.any { it.second == colorCode }) {
                        selectedColors.removeIf { it.second == colorCode }
                        button.setTextColor(Color.BLACK)
                    } else {
                        if (selectedColors.size < 3) {
                            selectedColors.add(Pair(colorName, colorCode))
                            button.setTextColor(Color.WHITE)
                        } else {
                            Toast.makeText(
                                context,
                                "3가지 색상까지만 선택할 수 있습니다.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

            fun bind(colorName: String, colorCode: Int) {
                // 버튼의 배경색과 텍스트 설정
                button.setBackgroundColor(colorCode)
                button.text = colorName
                // 선택된 색상이면 글자색을 흰색으로, 아니면 검정색으로 설정
                if (selectedColors.any { it.second == colorCode }) {
                    button.setTextColor(Color.WHITE)
                } else {
                    button.setTextColor(Color.BLACK)
                }
            }
        }
    }

    fun getSelectedColors(): List<Pair<String, Int>> {
        return selectedColors
    }
}