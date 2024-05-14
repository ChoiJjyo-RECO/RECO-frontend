package choijjyo.reco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import choijjyo.reco.databinding.ActivityClothesBinding
import com.bumptech.glide.Glide

class ClothesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClothesBinding
    private lateinit var itemImageView: ImageView
    private lateinit var itemColorCategoryTextView: TextView
    private lateinit var itemClothesTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_clothes)

        itemImageView = findViewById(R.id.itemImageView)
        itemColorCategoryTextView = findViewById(R.id.itemColorCategory)
        itemClothesTextView = findViewById(R.id.itemClothes)

        val itemImageUrl = intent.getStringExtra("imageUrl")
        val itemColorCategory = intent.getStringExtra("colorCategory")
        val itemClothes = intent.getStringExtra("clothes")

        Glide.with(this)
            .load(itemImageUrl)
            .into(itemImageView)

        itemColorCategoryTextView.text = "색상: $itemColorCategory"
        itemClothesTextView.text = "분류: $itemClothes"

        binding.backToClosetButton.setOnClickListener {
            intent = Intent(this, MyClosetActivity::class.java)
            startActivity(intent)
        }

    }
}