package choijjyo.reco.view.preference

import android.content.Context
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import choijjyo.reco.R

class ShowColorExplain(context: Context, colorname: String, colorexplain: String) : AlertDialog(context) {
    private val colorExplain: TextView
    private val colorName: TextView
    private val shutdownClick: Button

    init {
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        val view = layoutInflater.inflate(R.layout.activity_show_color_explain, null)
        setView(view)

        colorName = view.findViewById(R.id.colorName)
        colorName.text = colorname

        colorExplain = view.findViewById(R.id.colorExplain)
        colorExplain.text = colorexplain

        shutdownClick = view.findViewById(R.id.btn_color_explain_shutdown)
        shutdownClick.setOnClickListener {
            dismiss()
        }
    }
}
