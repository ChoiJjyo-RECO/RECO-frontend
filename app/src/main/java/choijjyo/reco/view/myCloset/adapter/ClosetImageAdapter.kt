package choijjyo.reco.view.myCloset.adapter

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R
import choijjyo.reco.view.myCloset.adapter.holder.ClosetImageViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ClosetImageAdapter(
    private val imageList: List<String>,
    private val colorCategoryList: List<String>,
    private val clothesList: List<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ClosetImageViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClosetImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_closet, parent, false)
        return ClosetImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClosetImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        val colorCategory = colorCategoryList[position]
        val clothes = clothesList[position]
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val requestOptions = RequestOptions().apply {
            override(screenWidth / 3)
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
