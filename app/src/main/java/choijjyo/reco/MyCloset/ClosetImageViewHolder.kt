package choijjyo.reco.MyCloset

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R

class ClosetImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.closetImageView)
    val colorCategoryTextView: TextView = itemView.findViewById(R.id.colorCategoryTextView)
    val clothesTextView: TextView = itemView.findViewById(R.id.clothesTextView)
}
