package choijjyo.reco

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import choijjyo.reco.databinding.ActivityClothesBinding
import com.bumptech.glide.Glide

class ClothesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityClothesBinding
    private lateinit var itemImageView: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_clothes)

        itemImageView = findViewById(R.id.itemImageView)

        val itemImageUrl = intent.getStringExtra("itemImageUrl")


        Glide.with(this)
            .load(itemImageUrl)
            .into(itemImageView)

        binding.backToClosetButton.setOnClickListener {
            intent = Intent(this, MyClosetActivity::class.java)
            startActivity(intent)
        }

    }
}