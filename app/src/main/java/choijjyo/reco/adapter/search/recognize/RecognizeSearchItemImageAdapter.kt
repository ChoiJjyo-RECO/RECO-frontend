package choijjyo.reco.adapter.search.recognize

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R
import choijjyo.reco.adapter.basic.holder.ItemImageViewHolder
import choijjyo.reco.data.Constants
import choijjyo.reco.data.entity.search.SearchResultItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class RecognizeSearchItemImageAdapter(
    private var imageList: List<SearchResultItem>
) : RecyclerView.Adapter<ItemImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image_basic, parent, false)
        return ItemImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemImageViewHolder, position: Int) {
        val currentItem = imageList[position]
        val requestOptions = RequestOptions().apply {
            override(Constants.COLUMN_COUNT)
        }
        Glide.with(holder.imageView.context).load(currentItem.imageUrl).apply(requestOptions)
            .into(holder.imageView)
        // ImageView에 클릭 리스너 설정
        holder.imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(currentItem.clickUrl)
            it.context.startActivity(intent)
        }
    }
    fun updateData(newData: List<SearchResultItem>) {
        this.imageList = newData
        notifyDataSetChanged()
    }


    override fun getItemCount() = imageList.size
}