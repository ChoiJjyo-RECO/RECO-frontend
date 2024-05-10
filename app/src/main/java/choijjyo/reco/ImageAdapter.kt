package choijjyo.reco

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ImageAdapter(private val imageList: List<String>) : RecyclerView.Adapter<ImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = imageList[position]
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val requestOptions = RequestOptions().apply {
            override(screenWidth/3)
        }
        Glide.with(holder.itemView.context).load(imageUrl).apply(requestOptions).into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}
