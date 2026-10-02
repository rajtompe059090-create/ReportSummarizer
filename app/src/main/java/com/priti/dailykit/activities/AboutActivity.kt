package com.priti.dailykit.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        AdsManager.loadBanner(this, binding.adBannerContainer)
    }
}
