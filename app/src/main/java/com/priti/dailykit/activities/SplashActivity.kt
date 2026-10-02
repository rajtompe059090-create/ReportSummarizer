package com.priti.dailykit.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Ads SDK in background
        AdsManager.initialize(this)

        // Smooth entry animation
        binding.splashContent.alpha = 0f
        binding.splashContent.scaleX = 0.88f
        binding.splashContent.scaleY = 0.88f

        binding.splashContent.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(700)
            .setInterpolator(DecelerateInterpolator())
            .withEndAction {
                binding.root.postDelayed({
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }, 400)
            }
            .start()
    }
}
