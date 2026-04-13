package com.vexo.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import data.repository.WatchlistRepository
import ui.detail.DetailActivity

class AllRatedMoviesActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var adapter: RecentActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_rated_movies)
        watchlistRepository = WatchlistRepository(this)
        
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    private fun setupUI() {
        findViewById<ImageButton>(R.id.btnBackAllRated).setOnClickListener { finish() }
        val recycler = findViewById<RecyclerView>(R.id.recyclerAllRatedMovies)

        val ratedMovies = watchlistRepository.getAllRatedMovies()

        if (ratedMovies.isEmpty()) {
            findViewById<View>(R.id.layoutEmptyAllRated).visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            findViewById<View>(R.id.layoutEmptyAllRated).visibility = View.GONE
            recycler.visibility = View.VISIBLE
            recycler.layoutManager = GridLayoutManager(this, 3)
            adapter = RecentActivityAdapter(ratedMovies, watchlistRepository) { movie ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("movie", movie)
                startActivity(intent)
            }
            recycler.adapter = adapter
        }
    }

    private fun applyTheme() {
        val bgName = watchlistRepository.getHeaderBackground()
        val imgBg = findViewById<ImageView>(R.id.imgAllRatedBackground)
        val overlay = findViewById<View>(R.id.viewAllRatedOverlay)
        val root = findViewById<View>(R.id.rootAllRated)
        val title = findViewById<TextView>(R.id.textTitleAllRated)
        val btnBack = findViewById<ImageButton>(R.id.btnBackAllRated)

        val themeConfig = when (bgName) {
            "fondo_futurista" -> ThemeConfig(R.drawable.fondo_futurista, "#00E5FF", "#1A1A1A")
            "fondo_espacio" -> ThemeConfig(R.drawable.fondo_espacio, "#B0E0E6", "#0B1026")
            "fondo_salacine" -> ThemeConfig(R.drawable.fondo_salacine, "#FFD700", "#2B0000")
            "fondo_cineclasico" -> ThemeConfig(R.drawable.fondo_cineclasico, "#D2B48C", "#1A110D")
            "fondo_vaporwave" -> ThemeConfig(R.drawable.fondo_vaporwave, "#FF71CE", "#2D1B4B")
            "fondo_playa" -> ThemeConfig(R.drawable.fondo_playa, "#00BCD4", "#002F2F")
            "fondo_callejerocine" -> ThemeConfig(R.drawable.fondo_callejerocine, "#FF9800", "#1A1A1A")
            else -> null
        }

        if (themeConfig != null) {
            imgBg.visibility = View.VISIBLE
            imgBg.setImageResource(themeConfig.resId)
            overlay.visibility = View.VISIBLE
            root.setBackgroundColor(Color.TRANSPARENT)
            
            val accent = Color.parseColor(themeConfig.accent)
            title.setTextColor(accent)
            btnBack.imageTintList = android.content.res.ColorStateList.valueOf(accent)
            
            if (::adapter.isInitialized) {
                adapter.updateTheme(true, accent)
            }
            window.statusBarColor = Color.parseColor(themeConfig.background)
        } else {
            imgBg.visibility = View.GONE
            overlay.visibility = View.GONE
            root.setBackgroundColor(ContextCompat.getColor(this, R.color.background_app))
            title.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            btnBack.imageTintList = android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary))
            
            if (::adapter.isInitialized) {
                adapter.updateTheme(false)
            }
            window.statusBarColor = ContextCompat.getColor(this, R.color.background_app)
        }
    }

    private data class ThemeConfig(val resId: Int, val accent: String, val background: String)
}
