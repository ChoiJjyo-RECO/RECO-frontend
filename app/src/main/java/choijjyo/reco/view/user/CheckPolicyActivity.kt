package choijjyo.reco.view.user

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import choijjyo.reco.R
import choijjyo.reco.databinding.ActivityCheckPolicyBinding
import choijjyo.reco.databinding.ActivitySignUpBinding

class CheckPolicyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_check_policy)

        val inputName = intent.getStringExtra("inputName")
        val inputEmail = intent.getStringExtra("inputEmail")
        val inputPW = intent.getStringExtra("inputPW")
        val checkPW = intent.getStringExtra("checkPW")

        binding.exitBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra("AGREED", false)
            intent.putExtra("inputName", inputName)
            intent.putExtra("inputEmail", inputEmail)
            intent.putExtra("inputPW", inputPW)
            intent.putExtra("checkPW", checkPW)
            startActivity(intent)
            finish()
        }
        binding.agreeBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            intent.putExtra("AGREED", true)
            intent.putExtra("inputName", inputName)
            intent.putExtra("inputEmail", inputEmail)
            intent.putExtra("inputPW", inputPW)
            intent.putExtra("checkPW", checkPW)
            startActivity(intent)
            Toast.makeText(this, "이용약관에 동의하였습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}