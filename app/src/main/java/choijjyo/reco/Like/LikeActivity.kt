package choijjyo.reco.Like

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import choijjyo.reco.Main.MainActivity
import choijjyo.reco.R
import com.google.android.material.tabs.TabLayout

class LikeActivity : AppCompatActivity() {
    private lateinit var fragment1: Fragment
    private lateinit var fragment2: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        fragment1 = Fragment_Color()
        fragment2 = Fragment_Cloth()

        supportFragmentManager.beginTransaction().add(R.id.frameLayout, fragment1).commit()
        val tabLayout = findViewById<TabLayout>(R.id.like_tab)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position

                val selected: Fragment? = when (position) {
                    0 -> fragment1
                    1 -> fragment2
                    else -> null
                }

                selected?.let {
                    supportFragmentManager.beginTransaction().replace(R.id.frameLayout, it).commit()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val backButton = findViewById<Button>(R.id.backToMainButton)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Optional: Finish the current activity
        }
    }
}