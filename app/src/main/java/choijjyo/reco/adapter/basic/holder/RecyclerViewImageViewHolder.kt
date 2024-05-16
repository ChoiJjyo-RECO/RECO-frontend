package choijjyo.reco.adapter.basic.holder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R

class RecyclerViewImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.recentImageView)
}
