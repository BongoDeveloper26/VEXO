package com.vexo.app

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import ui.explore.ExploreFragment
import ui.search.SearchFragment
import ui.news.NewsFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigation(savedInstanceState)
    }

    private fun setupBottomNavigation(savedInstanceState: Bundle?) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        bottomNav.itemActiveIndicatorColor = ColorStateList.valueOf(Color.parseColor("#267C3AED"))
        
        if (savedInstanceState == null) {
            loadFragment(ExploreFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_explore -> ExploreFragment()
                R.id.nav_search -> SearchFragment()
                R.id.nav_list -> ListFragment()
                R.id.nav_news -> NewsFragment() // Nueva sección de noticias
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
