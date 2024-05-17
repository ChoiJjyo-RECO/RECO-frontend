package choijjyo.reco.adapter.basic.holder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R

class ItemImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.basic_item_IV)
}
