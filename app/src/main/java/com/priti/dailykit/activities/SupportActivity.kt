package com.priti.dailykit.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.R
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivitySupportBinding
import com.priti.dailykit.utils.AppFeedback

class SupportActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnEmailSupport.setOnClickListener {
            val email = getString(R.string.support_email_placeholder)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, "DailyKit Support Request")
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                AppFeedback.copyToClipboard(this, "Support Email", email)
                Toast.makeText(this, "Copied $email to clipboard", Toast.LENGTH_LONG).show()
            }
        }

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }
}
