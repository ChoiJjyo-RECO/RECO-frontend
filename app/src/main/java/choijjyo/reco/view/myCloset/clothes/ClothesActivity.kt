package choijjyo.reco.view.myCloset.clothes

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentTransaction
import choijjyo.reco.R
import choijjyo.reco.databinding.ActivityClothesBinding
import choijjyo.reco.view.myCloset.closet.ClosetActivity
import choijjyo.reco.view.recognize.ColorFilterHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import javax.sql.DataSource
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL

class ClothesActivity : AppCompatActivity() {
    companion object {
        init{
            System.loadLibrary("opencv_java4")
        }
    }

    private lateinit var binding: ActivityClothesBinding
    private lateinit var itemImageView: ImageView
    private lateinit var itemColorCategoryTextView: TextView
    private lateinit var itemClothesTextView: TextView
    private lateinit var deuteranopiaButton: Button
    private lateinit var protanopiaButton: Button
    private lateinit var tritanopiaButton: Button
    private lateinit var originalImgButton: Button

    private var isDeuteranopiaButtonPressed = false
    private var isProtanopiaButtonPressed = false
    private var isTritanopiaButtonPressed = false
    private var isOriginalImgButtonPressed = true
    private lateinit var originalBitmap: Bitmap

    private val colorFilterHelper = ColorFilterHelper()

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
            .apply(RequestOptions().override(Target.SIZE_ORIGINAL))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // Handle the error here
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let {
                        originalBitmap = (it as BitmapDrawable).bitmap
                    }
                    return false
                }
            })
            .into(itemImageView)

        itemColorCategoryTextView.text = "색상: $itemColorCategory"
        itemClothesTextView.text = "분류: $itemClothes"

        binding.backToClosetButton.setOnClickListener {
            intent = Intent(this, ClosetActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_left_enter, R.anim.slide_left_exit)
        }
        deuteranopiaButton = binding.clothesDeuteranopiaButton
        protanopiaButton = binding.clothesProtanopiaButton
        tritanopiaButton = binding.clothesTritanopiaButton
        originalImgButton = binding.clothesOriginalImgButton

        binding.clothesDeuteranopiaButton.setOnClickListener {
            if (!isDeuteranopiaButtonPressed) {
                applyDeuteranopia()
                updateButtonBackground(deuteranopiaButton, true)
                resetButtonBackground(deuteranopiaButton)
                isDeuteranopiaButtonPressed = true
                isProtanopiaButtonPressed = false
                isTritanopiaButtonPressed = false
                isOriginalImgButtonPressed = false
            }
        }
        binding.clothesProtanopiaButton.setOnClickListener {
            if (!isProtanopiaButtonPressed) {
                applyProtanopia()
                updateButtonBackground(protanopiaButton, true)
                resetButtonBackground(protanopiaButton)
                isDeuteranopiaButtonPressed = false
                isProtanopiaButtonPressed = true
                isTritanopiaButtonPressed = false
                isOriginalImgButtonPressed = false
            }
        }
        binding.clothesTritanopiaButton.setOnClickListener {
            if (!isTritanopiaButtonPressed) {
                applyTritanopia()
                updateButtonBackground(tritanopiaButton, true)
                resetButtonBackground(tritanopiaButton)
                isDeuteranopiaButtonPressed = false
                isProtanopiaButtonPressed = false
                isTritanopiaButtonPressed = true
                isOriginalImgButtonPressed = false
            }
        }
        binding.clothesOriginalImgButton.setOnClickListener {
            if (!isOriginalImgButtonPressed) {
                showOriginal()
                updateButtonBackground(originalImgButton, true)
                resetButtonBackground(originalImgButton)
                isDeuteranopiaButtonPressed = false
                isProtanopiaButtonPressed = false
                isTritanopiaButtonPressed = false
                isOriginalImgButtonPressed = true
            }
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
    fun applyDeuteranopia() {
        val correctedBitmap = colorFilterHelper.applyDeuteranopia(originalBitmap)
        binding.itemImageView.setImageBitmap(correctedBitmap)
    }

    fun applyProtanopia() {
        val correctedBitmap = colorFilterHelper.applyProtanopia(originalBitmap)
        binding.itemImageView.setImageBitmap(correctedBitmap)
    }

    fun applyTritanopia() {
        val correctedBitmap = colorFilterHelper.applyTritanopia(originalBitmap)
        binding.itemImageView.setImageBitmap(correctedBitmap)
    }
    fun showOriginal() {
        binding.itemImageView.setImageBitmap(originalBitmap)
    }
    private fun updateButtonBackground(button: Button, isPressed: Boolean) {
        if (isPressed) {
            button.setBackgroundResource(R.drawable.btn_selected_bg) // 눌린 상태 배경색
        } else {
            button.setBackgroundResource(R.drawable.btn_not_selected_bg) // 일반 상태 배경색
        }
    }

    private fun resetButtonBackground(clickedButton: Button) {
        if (clickedButton != binding.clothesDeuteranopiaButton) {
            updateButtonBackground(binding.clothesDeuteranopiaButton, false)
        }
        if (clickedButton != binding.clothesProtanopiaButton) {
            updateButtonBackground(binding.clothesProtanopiaButton, false)
        }
        if (clickedButton != binding.clothesTritanopiaButton) {
            updateButtonBackground(binding.clothesTritanopiaButton, false)
        }
        if (clickedButton != binding.clothesOriginalImgButton) {
            updateButtonBackground(binding.clothesOriginalImgButton, false)
        }
    }
}