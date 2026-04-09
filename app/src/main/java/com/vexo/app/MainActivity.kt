package com.vexo.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import data.repository.WatchlistRepository
import ui.explore.ExploreFragment
import ui.search.SearchFragment
import ui.news.NewsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    
    private val exploreFragment by lazy { ExploreFragment() }
    private val searchFragment by lazy { SearchFragment() }
    private val listFragment by lazy { ListFragment() }
    private val newsFragment by lazy { NewsFragment() }
    private val profileFragment by lazy { ProfileFragment() }
    private var activeFragment: Fragment = exploreFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        watchlistRepository = WatchlistRepository(this)
        
        setupFragments()
        setupBottomNavigation()
        handleDeepLink(intent)
    }

    private fun setupFragments() {
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainer, profileFragment, "profile").hide(profileFragment)
            add(R.id.fragmentContainer, newsFragment, "news").hide(newsFragment)
            add(R.id.fragmentContainer, listFragment, "list").hide(listFragment)
            add(R.id.fragmentContainer, searchFragment, "search").hide(searchFragment)
            add(R.id.fragmentContainer, exploreFragment, "explore")
        }.commit()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        bottomNav.setOnItemSelectedListener { item ->
            val targetFragment = when (item.itemId) {
                R.id.nav_explore -> exploreFragment
                R.id.nav_search -> searchFragment
                R.id.nav_list -> listFragment
                R.id.nav_news -> newsFragment
                R.id.nav_profile -> profileFragment
                else -> exploreFragment
            }
            
            if (activeFragment != targetFragment) {
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .hide(activeFragment)
                    .show(targetFragment)
                    .commit()
                activeFragment = targetFragment
            }
            true
        }
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
}