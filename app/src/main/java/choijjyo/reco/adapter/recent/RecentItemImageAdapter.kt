package choijjyo.reco.adapter.recent

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.data.Constants
import choijjyo.reco.R
import choijjyo.reco.adapter.basic.holder.ItemImageViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class RecentItemImageAdapter(
    private val imageList: List<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ItemImageViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_basic, parent, false)
        return ItemImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        val requestOptions = RequestOptions().apply {
            override(Constants.COLUMN_COUNT)
        }
        Glide.with(holder.itemView.context).load(imageUrl).apply(requestOptions)
            .into(holder.imageView)
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
