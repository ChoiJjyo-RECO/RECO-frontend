package choijjyo.reco.MyCloset

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import choijjyo.reco.Main.Constants
import choijjyo.reco.R
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class ClosetSearchImageAdapter(
    private val imageUrlList: List<String>,
    private val clickUrlList: List<String>,
) : RecyclerView.Adapter<ClosetSearchImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClosetSearchImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recent_item_image, parent, false)
        return ClosetSearchImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClosetSearchImageViewHolder, position: Int) {
        val imageUrl = imageUrlList[position]
        val clickUrl = clickUrlList[position]
        val screenWidth = Resources.getSystem().displayMetrics.widthPixels
        val requestOptions = RequestOptions().apply {
            override(screenWidth / Constants.SPAN_COUNT)
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
