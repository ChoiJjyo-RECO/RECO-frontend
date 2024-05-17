package choijjyo.reco.view.myCloset.clothes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Lifecycle
import choijjyo.reco.R
import choijjyo.reco.databinding.ActivityClothesBinding
import choijjyo.reco.view.myCloset.closet.ClosetActivity
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout

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

        val itemImageName = intent.getStringExtra("imageName")
        val itemImageUrl = intent.getStringExtra("imageUrl")
        val itemColorCategory = intent.getStringExtra("colorCategory")
        val itemClothes = intent.getStringExtra("clothes")

        Glide.with(this)
            .load(itemImageUrl)
            .into(itemImageView)

        itemColorCategoryTextView.text = "색상: $itemColorCategory"
        itemClothesTextView.text = "분류: $itemClothes"

        binding.backToClosetButton.setOnClickListener {
            intent = Intent(this, ClosetActivity::class.java)
            startActivity(intent)
        }
        // 초기화 시 첫 번째 탭의 프래그먼트를 추가
        if (savedInstanceState == null) {
            val fragment = ClothesRecommendFragment(itemImageName)
            supportFragmentManager.beginTransaction()
                .add(R.id.searchRecord_container, fragment, "FragmentTag0")
                .commit()
        }

        binding.tabLayoutClothes.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position
                val tag = "FragmentTag$position"
                var fragment = supportFragmentManager.findFragmentByTag(tag)

                if (fragment == null) {
                    fragment = when (position) {
                        0 -> ClothesRecommendFragment(itemImageName)
                        1 -> ClothesSimilarFragment(itemImageName)
                        else -> null
                    }
                }

                fragment?.let {
                    val transaction = supportFragmentManager.beginTransaction()
                    if (!it.isAdded) {
                        transaction.add(R.id.searchRecord_container, it, tag)
                    }
                    hideOtherFragments(transaction, tag)
                    transaction.show(it).commitNow()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        binding.tabLayoutClothes.selectTab(binding.tabLayoutClothes.getTabAt(0))

    }

    private fun hideOtherFragments(transaction: FragmentTransaction, exceptTag: String) {
        supportFragmentManager.fragments.forEach {
            if (it.tag != exceptTag) {
                transaction.hide(it)
            }
        }
    }
}