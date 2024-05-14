package choijjyo.reco.Recognize

import com.google.firebase.Timestamp

data class ClosetData(
    val closetColorRGB: List<Double>,
    val closetColorCategory: String,
    val clothes: String,
    val imgURL: String,
    val timestamp: Timestamp
)
