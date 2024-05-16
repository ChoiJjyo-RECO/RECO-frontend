package choijjyo.reco.Recognize

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.Main.RecentImageViewHolder
import choijjyo.reco.R
import com.bumptech.glide.Glide

class RecommendImageAdapter(
    private var imageList: List<SearchResultItem>
) : RecyclerView.Adapter<RecentImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recent_item_image, parent, false)
        return RecentImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecentImageViewHolder, position: Int) {
        val currentItem = imageList[position]
        Glide.with(holder.imageView.context).load(currentItem.imageUrl).into(holder.imageView)
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