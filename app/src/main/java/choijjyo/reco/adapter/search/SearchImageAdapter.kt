package choijjyo.reco.adapter.search

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R
import choijjyo.reco.adapter.basic.holder.RecyclerViewImageViewHolder
import choijjyo.reco.data.Constants
import choijjyo.reco.data.entity.search.SearchResultItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SearchImageAdapter (private var imageList: List<SearchResultItem>) : RecyclerView.Adapter<RecyclerViewImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recent_item_image, parent, false)
        return RecyclerViewImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerViewImageViewHolder, position: Int) {
        val currentItem = imageList[position]
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val requestOptions = RequestOptions().apply {
            override(screenWidth / Constants.SPAN_COUNT)
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