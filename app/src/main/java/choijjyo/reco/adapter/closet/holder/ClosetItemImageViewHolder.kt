package choijjyo.reco.adapter.closet.holder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R

class ClosetItemImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.closet_item_IV)
    val colorTextView: TextView = itemView.findViewById(R.id.clothes_color_TV)
    val typeTextView: TextView = itemView.findViewById(R.id.clothes_type_TV)
}
