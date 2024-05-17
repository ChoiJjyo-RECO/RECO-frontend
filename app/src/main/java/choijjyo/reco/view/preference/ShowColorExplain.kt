package choijjyo.reco.view.preference

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import choijjyo.reco.R
import choijjyo.reco.view.recognize.ColorFilterHelper

class ShowColorExplain(context: Context, colorname: String, colorexplain: String) : AlertDialog(context) {
    private val colorExplain: TextView
    private val colorName: TextView
    private val colorBox: View
    private val shutdownClick: Button
    private val deuteranopiaButton: Button
    private val protanopiaButton: Button
    private val tritanopiaButton: Button
    private val originalImgButton: Button
    private val colorFilterHelper = ColorFilterHelper()
    private lateinit var originalBitmap: Bitmap

    init {
        System.loadLibrary("opencv_java4")
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        val view = LayoutInflater.from(context).inflate(R.layout.activity_show_color_explain, null)
        setView(view)

        colorName = view.findViewById(R.id.colorName)
        colorExplain = view.findViewById(R.id.colorExplain)
        colorBox = view.findViewById(R.id.colorBox)
        shutdownClick = view.findViewById(R.id.btn_color_explain_shutdown)
        deuteranopiaButton = view.findViewById(R.id.color_deuteranopiaButton)
        protanopiaButton = view.findViewById(R.id.color_protanopiaButton)
        tritanopiaButton = view.findViewById(R.id.color_tritanopiaButton)
        originalImgButton = view.findViewById(R.id.color_originalImgButton)

        colorName.text = colorname
        colorExplain.text = colorexplain

        // get color resource ID
        val colorResId = context.resources.getIdentifier(colorname, "color", context.packageName)
        val color = ContextCompat.getColor(context, colorResId)
        colorBox.setBackgroundColor(color)

        originalBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(originalBitmap)
        canvas.drawColor(color)

        deuteranopiaButton.setOnClickListener {
            applyDeuteranopia()
        }
        protanopiaButton.setOnClickListener {
            applyProtanopia()
        }
        tritanopiaButton.setOnClickListener {
            applyTritanopia()
        }
        originalImgButton.setOnClickListener {
            showOriginal()
        }

        shutdownClick.setOnClickListener {
            dismiss()
        }
    }

    private fun applyDeuteranopia() {
        val correctedBitmap = colorFilterHelper.applyDeuteranopia(originalBitmap)
        colorBox.setBackgroundColor(getColorFromBitmap(correctedBitmap))
    }

    private fun applyProtanopia() {
        val correctedBitmap = colorFilterHelper.applyProtanopia(originalBitmap)
        colorBox.setBackgroundColor(getColorFromBitmap(correctedBitmap))
    }

    private fun applyTritanopia() {
        val correctedBitmap = colorFilterHelper.applyTritanopia(originalBitmap)
        colorBox.setBackgroundColor(getColorFromBitmap(correctedBitmap))
    }

    private fun showOriginal() {
        colorBox.setBackgroundColor(getColorFromBitmap(originalBitmap))
    }

    private fun getColorFromBitmap(bitmap: Bitmap): Int {
        return if (bitmap.width > 0 && bitmap.height > 0) {
            bitmap.getPixel(0, 0)
        } else {
            Color.BLACK
        }
    }
}
