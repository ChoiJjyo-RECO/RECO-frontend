package choijjyo.reco

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout

class MyClosetActivity : AppCompatActivity() {
    private lateinit var fragment1: Fragment
    private lateinit var fragment2: Fragment
    private lateinit var fragment3: Fragment
    private lateinit var fragment4: Fragment
    private lateinit var fragment5: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mycloset)

        fragment1 = Fragment_all()
        fragment2 = Fragment_top()
        fragment3 = Fragment_bottom()
        fragment4 = Fragment_outer()
        fragment5 = Fragment_onepiece()

        supportFragmentManager.beginTransaction().add(R.id.frameLayout, fragment1).commit()
        val tabLayout = findViewById<TabLayout>(R.id.mycloset_tab)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val position = tab.position

                val selected: Fragment? = when (position) {
                    0 -> fragment1
                    1 -> fragment2
                    2 -> fragment3
                    3 -> fragment4
                    4 -> fragment5
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
