package choijjyo.reco.data

import android.content.res.Resources

object Constants {
    private val SCREEN_WIDTH = Resources.getSystem().displayMetrics.widthPixels
    const val VERTICAL_COUNT = 3
    const val HORIZONTAL_COUNT = 1
    val COLUMN_COUNT = this.SCREEN_WIDTH / VERTICAL_COUNT
}