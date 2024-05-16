package choijjyo.reco.adapter.basic

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.data.Constants
import choijjyo.reco.R
import choijjyo.reco.adapter.basic.holder.RecyclerViewImageViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class RecyclerViewImageAdapter(
    private val imageList: List<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerViewImageViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_item_image, parent, false)
        return RecyclerViewImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val requestOptions = RequestOptions().apply {
            override(screenWidth / Constants.SPAN_COUNT)
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

    fun getImageUrl(position: Int): String {
        return imageList[position]
    }
}
