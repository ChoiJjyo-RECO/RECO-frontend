package choijjyo.reco

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class RecommendImageAdapter (private val imageList: List<SearchResultItem>) : RecyclerView.Adapter<RecommendImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.recoImage)
        //val imageUrlText: TextView = view.findViewById(R.id.recoImageUrlText1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recommend_image_xml, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentItem = imageList[position]
        Glide.with(holder.imageView.context).load(currentItem.imageUrl).into(holder.imageView)
        // ImageView에 클릭 리스너 설정
        holder.imageView.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(currentItem.clickUrl)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = imageList.size
}