package choijjyo.reco.data.entity.closet

import com.google.firebase.Timestamp

data class ClosetData(
    val closetColorRGB: List<Double>,
    val closetColorCategory: String,
    val clothes: String,
    val imgURL: String,
    val timestamp: Timestamp
)
