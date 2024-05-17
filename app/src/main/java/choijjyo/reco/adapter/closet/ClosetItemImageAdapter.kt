package choijjyo.reco.adapter.closet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R
import choijjyo.reco.data.Constants
import choijjyo.reco.adapter.closet.holder.ClosetItemImageViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ClosetItemImageAdapter(
    private val imageList: List<String>,
    private val colorCategoryList: List<String>,
    private val clothesList: List<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ClosetItemImageViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClosetItemImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_closet, parent, false)
        return ClosetItemImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClosetItemImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        val colorCategory = colorCategoryList[position]
        val clothes = clothesList[position]
        val requestOptions = RequestOptions().apply {
            override(Constants.COLUMN_COUNT)
        }
        Glide.with(holder.itemView.context).load(imageUrl).apply(requestOptions).into(holder.imageView)
        holder.colorTextView.text = "색상: $colorCategory"
        holder.typeTextView.text = "분류: $clothes"
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
