package choijjyo.reco.data

import android.content.res.Resources

object Constants {
    private val SCREEN_WIDTH = Resources.getSystem().displayMetrics.widthPixels
    const val SPAN_COUNT = 3
    val COLUMN_COUNT = this.SCREEN_WIDTH / SPAN_COUNT
}