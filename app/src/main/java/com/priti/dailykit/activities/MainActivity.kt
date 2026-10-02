package com.priti.dailykit.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.priti.dailykit.R
import com.priti.dailykit.adapters.CategoryAdapter
import com.priti.dailykit.adapters.QuickToolAdapter
import com.priti.dailykit.adapters.ToolCardAdapter
import com.priti.dailykit.ads.AdsManager
import com.priti.dailykit.databinding.ActivityMainBinding
import com.priti.dailykit.models.ToolCategory
import com.priti.dailykit.models.ToolItem
import com.priti.dailykit.storage.DailyKitPreferences
import com.priti.dailykit.utils.AppFeedback
import com.priti.dailykit.utils.ToolsRegistry

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: DailyKitPreferences

    private lateinit var toolsAdapter: ToolCardAdapter
    private lateinit var favoritesAdapter: ToolCardAdapter
    private lateinit var popularAdapter: ToolCardAdapter
    private lateinit var quickAdapter: QuickToolAdapter
    private lateinit var recentsAdapter: QuickToolAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    private var currentCategory: ToolCategory = ToolCategory.ALL
    private var currentSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = DailyKitPreferences.getInstance(this)

        setupBottomNavigation()
        setupHomeTab()
        setupToolsTab()
        setupFavoritesTab()
        setupSettingsTab()
        setupBackNavigation()

        // Load banner ad
        AdsManager.loadBanner(this, binding.adBannerContainer)
    }

    override fun onResume() {
        super.onResume()
        refreshRecentsSection()
        refreshFavoritesTab()
        toolsAdapter.notifyDataSetChanged()
        popularAdapter.notifyDataSetChanged()
        updateCurrencyLabel()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.bottomNavigation.selectedItemId != R.id.menu_home) {
                    binding.bottomNavigation.selectedItemId = R.id.menu_home
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    showTab(0)
                    true
                }
                R.id.menu_tools -> {
                    showTab(1)
                    true
                }
                R.id.menu_favorites -> {
                    showTab(2)
                    refreshFavoritesTab()
                    true
                }
                R.id.menu_settings -> {
                    showTab(3)
                    true
                }
                else -> false
            }
        }
    }

    private fun showTab(index: Int) {
        binding.layoutTabHome.root.visibility = if (index == 0) View.VISIBLE else View.GONE
        binding.layoutTabTools.root.visibility = if (index == 1) View.VISIBLE else View.GONE
        binding.layoutTabFavorites.root.visibility = if (index == 2) View.VISIBLE else View.GONE
        binding.layoutTabSettings.root.visibility = if (index == 3) View.VISIBLE else View.GONE
    }

    private fun setupHomeTab() {
        val home = binding.layoutTabHome

        // Quick Search Button & Bar
        val openSearch = View.OnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.menu_tools
            binding.layoutTabTools.etSearchTools.requestFocus()
        }
        home.btnQuickSearch.setOnClickListener(openSearch)
        home.searchPillBar.setOnClickListener(openSearch)

        // Quick Tools Recycler
        quickAdapter = QuickToolAdapter(this, ToolsRegistry.getQuickTools())
        home.rvQuickTools.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        home.rvQuickTools.adapter = quickAdapter

        // Recents Recycler
        recentsAdapter = QuickToolAdapter(this, emptyList())
        home.rvRecentTools.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        home.rvRecentTools.adapter = recentsAdapter

        home.btnClearRecents.setOnClickListener {
            prefs.clearRecentTools()
            refreshRecentsSection()
        }

        // Popular Tools
        popularAdapter = ToolCardAdapter(this, ToolsRegistry.getPopularTools()) { _, _ ->
            refreshFavoritesTab()
        }
        home.rvPopularTools.layoutManager = LinearLayoutManager(this)
        home.rvPopularTools.adapter = popularAdapter

        // Notes Banner
        home.cardHomeNotesBanner.setOnClickListener {
            prefs.addRecentTool("notes")
            startActivity(Intent(this, NotesActivity::class.java))
        }

        refreshRecentsSection()
    }

    private fun refreshRecentsSection() {
        val recentIds = prefs.getRecentToolIds()
        val recentTools = recentIds.mapNotNull { ToolsRegistry.getToolById(it) }

        if (recentTools.isEmpty()) {
            binding.layoutTabHome.layoutRecentSection.visibility = View.GONE
        } else {
            binding.layoutTabHome.layoutRecentSection.visibility = View.VISIBLE
            recentsAdapter = QuickToolAdapter(this, recentTools)
            binding.layoutTabHome.rvRecentTools.adapter = recentsAdapter
        }
    }

    private fun setupToolsTab() {
        val toolsTab = binding.layoutTabTools

        // Categories
        val categories = ToolCategory.values().toList()
        categoryAdapter = CategoryAdapter(this, categories) { selected ->
            currentCategory = selected
            applyToolsFilter()
        }
        toolsTab.rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        toolsTab.rvCategories.adapter = categoryAdapter

        // Tools List
        toolsAdapter = ToolCardAdapter(this, ToolsRegistry.allTools) { _, _ ->
            refreshFavoritesTab()
        }
        toolsTab.rvAllTools.layoutManager = LinearLayoutManager(this)
        toolsTab.rvAllTools.adapter = toolsAdapter

        // Search Input
        toolsTab.etSearchTools.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                toolsTab.btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                applyToolsFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        toolsTab.btnClearSearch.setOnClickListener {
            toolsTab.etSearchTools.setText("")
        }
    }

    private fun applyToolsFilter() {
        val categoryFiltered = ToolsRegistry.getToolsByCategory(currentCategory)
        val filtered = if (currentSearchQuery.isEmpty()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { tool ->
                val title = getString(tool.titleRes).lowercase()
                val desc = getString(tool.descRes).lowercase()
                val q = currentSearchQuery.lowercase()
                title.contains(q) || desc.contains(q) || tool.id.contains(q)
            }
        }

        toolsAdapter.updateData(filtered)

        val emptyView = binding.layoutTabTools.layoutEmptySearch
        if (filtered.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            binding.layoutTabTools.rvAllTools.visibility = View.GONE
        } else {
            emptyView.visibility = View.GONE
            binding.layoutTabTools.rvAllTools.visibility = View.VISIBLE
        }
    }

    private fun setupFavoritesTab() {
        val favTab = binding.layoutTabFavorites

        favoritesAdapter = ToolCardAdapter(this, emptyList()) { _, _ ->
            refreshFavoritesTab()
        }
        favTab.rvFavorites.layoutManager = LinearLayoutManager(this)
        favTab.rvFavorites.adapter = favoritesAdapter

        favTab.btnExploreTools.setOnClickListener {
            binding.bottomNavigation.selectedItemId = R.id.menu_tools
        }

        refreshFavoritesTab()
    }

    private fun refreshFavoritesTab() {
        val favIds = prefs.getFavorites()
        val favTools = ToolsRegistry.allTools.filter { favIds.contains(it.id) }

        favoritesAdapter.updateData(favTools)

        val favTab = binding.layoutTabFavorites
        if (favTools.isEmpty()) {
            favTab.layoutEmptyFavorites.visibility = View.VISIBLE
            favTab.rvFavorites.visibility = View.GONE
            favTab.tvFavoritesSubtitle.text = "0 tools saved"
        } else {
            favTab.layoutEmptyFavorites.visibility = View.GONE
            favTab.rvFavorites.visibility = View.VISIBLE
            favTab.tvFavoritesSubtitle.text = "${favTools.size} tool${if (favTools.size > 1) "s" else ""} saved"
        }
    }

    private fun setupSettingsTab() {
        val settings = binding.layoutTabSettings

        settings.cardSettingCurrency.setOnClickListener {
            showCurrencyPickerDialog()
        }

        settings.cardSettingAbout.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        settings.cardSettingPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        settings.cardSettingSupport.setOnClickListener {
            startActivity(Intent(this, SupportActivity::class.java))
        }

        settings.cardSettingShare.setOnClickListener {
            AppFeedback.shareText(
                this,
                getString(R.string.settings_share_app),
                getString(R.string.share_app_text)
            )
        }

        settings.cardSettingRate.setOnClickListener {
            val appPackageName = packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (e: ActivityNotFoundException) {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                } catch (e2: Exception) {
                    Toast.makeText(this, "Thank you for your rating!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        updateCurrencyLabel()
    }

    private fun updateCurrencyLabel() {
        val symbol = prefs.getCurrency()
        binding.layoutTabSettings.tvCurrentCurrencySymbol.text = symbol
        val name = when (symbol) {
            "₹" -> "Indian Rupee (₹)"
            "$" -> "US Dollar ($)"
            "€" -> "Euro (€)"
            "£" -> "British Pound (£)"
            else -> "Currency ($symbol)"
        }
        binding.layoutTabSettings.tvCurrencyLabel.text = name
    }

    private fun showCurrencyPickerDialog() {
        val currencies = arrayOf(
            "Indian Rupee (₹)",
            "US Dollar ($)",
            "Euro (€)",
            "British Pound (£)"
        )
        val symbols = arrayOf("₹", "$", "€", "£")
        val currentIdx = symbols.indexOf(prefs.getCurrency()).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.settings_currency))
            .setSingleChoiceItems(currencies, currentIdx) { dialog, which ->
                prefs.setCurrency(symbols[which])
                updateCurrencyLabel()
                dialog.dismiss()
                Toast.makeText(this, "Currency set to ${symbols[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(getString(R.string.action_cancel), null)
            .show()
    }
}
