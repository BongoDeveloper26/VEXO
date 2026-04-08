package com.vexo.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import data.repository.WatchlistRepository
import ui.explore.ExploreFragment
import ui.search.SearchFragment
import ui.news.NewsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        watchlistRepository = WatchlistRepository(this)
        
        // Sincronizar datos de la nube
        syncCloudData()
        
        setupBottomNavigation(savedInstanceState)

        // Manejar Deep Link si existe
        handleDeepLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.scheme == "vexo" && data.host == "profile") {
            val userId = data.lastPathSegment
            if (userId != null) {
                val publicIntent = Intent(this, PublicProfileActivity::class.java)
                publicIntent.putExtra("USER_ID", userId)
                startActivity(publicIntent)
            }
        }
    }

    private fun syncCloudData() {
        watchlistRepository.downloadCloudData { success ->
            if (success) {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                if (currentFragment is ProfileFragment) {
                    currentFragment.onResume()
                }
            }
        }
    }

    private fun setupBottomNavigation(savedInstanceState: Bundle?) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        val vexPurple = Color.parseColor("#7C3AED")
        val navBg = Color.parseColor("#FFFFFF")
        
        bottomNav.setBackgroundColor(navBg)
        bottomNav.itemActiveIndicatorColor = ColorStateList.valueOf(Color.parseColor("#267C3AED"))
        bottomNav.itemIconTintList = ColorStateList.valueOf(vexPurple)
        bottomNav.itemTextColor = ColorStateList.valueOf(vexPurple)
        
        if (savedInstanceState == null) {
            loadFragment(ExploreFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment? = when (item.itemId) {
                R.id.nav_explore -> ExploreFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_list -> ListFragment()
                R.id.nav_news -> NewsFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> null
            }
            
            fragment?.let {
                loadFragment(it)
                true
            } ?: false
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .commit()
    }
}