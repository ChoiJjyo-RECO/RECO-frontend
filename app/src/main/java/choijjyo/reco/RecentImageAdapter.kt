package choijjyo.reco

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class RecentImageAdapter(private val imageList: List<String>) : RecyclerView.Adapter<RecentImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_item_image, parent, false)
        return RecentImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecentImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val requestOptions = RequestOptions().apply {
            override(screenWidth/Constants.SPAN_COUNT)
        }
        Glide.with(holder.itemView.context).load(imageUrl).apply(requestOptions).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
