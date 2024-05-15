package choijjyo.reco.Recognize

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.R
import com.bumptech.glide.Glide

class RecommendImageAdapter(private var imageList: List<SearchResultItem>) : RecyclerView.Adapter<RecommendImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.recommendImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recommend_image_xml, parent, false)
        Log.d("imageList", imageList.toString())
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentItem = imageList[position]
        Log.d("recommnedImage", "onBindViewBinder")
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