package choijjyo.reco.adapter.search.closet

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.data.Constants
import choijjyo.reco.R
import choijjyo.reco.adapter.basic.holder.ItemImageViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ClosetSearchItemImageAdapter(
    private val imageUrlList: List<String>,
    private val clickUrlList: List<String>,
) : RecyclerView.Adapter<ItemImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_basic, parent, false)
        return ItemImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemImageViewHolder, position: Int) {
        val imageUrl = imageUrlList[position]
        val clickUrl = clickUrlList[position]
        val requestOptions = RequestOptions().apply {
            override(Constants.COLUMN_COUNT)
        }
        Glide.with(holder.itemView.context).load(imageUrl).apply(requestOptions)
            .into(holder.imageView)
        holder.imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(clickUrl)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return imageUrlList.size
    }

    fun getImageUrl(position: Int): String {
        return imageUrlList[position]
    }
}
