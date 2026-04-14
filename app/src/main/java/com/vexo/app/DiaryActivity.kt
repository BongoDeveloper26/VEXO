package com.vexo.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vexo.app.R
import data.model.DiaryEntry
import data.model.Movie
import data.repository.WatchlistRepository
import ui.detail.DetailActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DiaryActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    private var onlyReviews: Boolean = false
    private var adapter: DiaryGroupedAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        watchlistRepository = WatchlistRepository(this)
        onlyReviews = intent.getBooleanExtra("ONLY_REVIEWS", false)

        findViewById<ImageButton>(R.id.btnBackDiary).setOnClickListener {
            finish()
        }
        
        if (onlyReviews) {
            findViewById<TextView>(R.id.textDiaryTitle).text = "Mis Reseñas"
            findViewById<TextView>(R.id.textDiaryLabel).text = "TUS CRÍTICAS"
            findViewById<View>(R.id.layoutDiaryStats)?.visibility = View.GONE
        }

        loadDiary()
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    private fun loadDiary() {
        var diaryEntries = watchlistRepository.getDiary()
        
        if (onlyReviews) {
            diaryEntries = diaryEntries.filter { !it.review.isNullOrEmpty() }
        }
        
        if (!onlyReviews) {
            updateStats(diaryEntries)
        }

        val groupedItems = mutableListOf<DiaryListItem>()
        var lastMonth = ""

        diaryEntries.forEach { entry ->
            val monthTitle = formatMonth(entry.date)
            if (monthTitle != lastMonth) {
                groupedItems.add(DiaryListItem.Header(monthTitle))
                lastMonth = monthTitle
            }
            groupedItems.add(DiaryListItem.Entry(entry))
        }

        val recycler = findViewById<RecyclerView>(R.id.recyclerDiaryFull)
        recycler.layoutManager = LinearLayoutManager(this)
        
        adapter = DiaryGroupedAdapter(
            groupedItems, 
            showTimeline = !onlyReviews,
            isFavorite = { movieId -> watchlistRepository.isFavorite(movieId) }
        ) { entry ->
            val movieToOpen = entry.movie ?: watchlistRepository.getAllRatedMovies().find { it.id == entry.movieId }
            if (movieToOpen != null) {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("movie", movieToOpen)
                startActivity(intent)
            }
        }
        recycler.adapter = adapter
    }

    private fun applyTheme() {
        val bgName = watchlistRepository.getHeaderBackground()
        val imgBg = findViewById<ImageView>(R.id.imgDiaryBackground)
        val overlay = findViewById<View>(R.id.viewDiaryOverlay)
        val rootLayout = findViewById<View>(R.id.rootDiaryLayout)
        val headerContainer = findViewById<View>(R.id.headerContainer)
        val title = findViewById<TextView>(R.id.textDiaryTitle)
        val label = findViewById<TextView>(R.id.textDiaryLabel)
        val btnBack = findViewById<ImageButton>(R.id.btnBackDiary)
        
        // Stats Labels
        val vistasLabel = findViewById<TextView>(R.id.textVistasLabel)
        val monthLabel = findViewById<TextView>(R.id.textMonthLabel)
        val mediaLabel = findViewById<TextView>(R.id.textMediaLabel)

        val themeConfig = when (bgName) {
            "fondo_vexocine" -> ThemeConfig(R.drawable.fondo_vexocine, "#7C3AED", "#1A1A1A", "#CC1A1A1A", "#407C3AED")
            "fondo_futurista" -> ThemeConfig(R.drawable.fondo_futurista, "#00E5FF", "#1A1A1A", "#CC1A1A1A", "#3300E5FF")
            "fondo_espacio" -> ThemeConfig(R.drawable.fondo_espacio, "#B0E0E6", "#0B1026", "#CC0B1026", "#40B0E0E6")
            "fondo_salacine" -> ThemeConfig(R.drawable.fondo_salacine, "#FFD700", "#2B0000", "#CC2B0000", "#40FFD700")
            "fondo_cineclasico" -> ThemeConfig(R.drawable.fondo_cineclasico, "#D2B48C", "#1A110D", "#CC1A110D", "#40D2B48C")
            "fondo_vaporwave" -> ThemeConfig(R.drawable.fondo_vaporwave, "#FF71CE", "#2D1B4B", "#CC2D1B4B", "#40FF71CE")
            "fondo_playa" -> ThemeConfig(R.drawable.fondo_playa, "#00BCD4", "#002F2F", "#CC002F2F", "#4000BCD4")
            "fondo_callejerocine" -> ThemeConfig(R.drawable.fondo_callejerocine, "#FF9800", "#1A1A1A", "#CC1A1A1A", "#40FF9800")
            else -> null
        }

        if (themeConfig != null) {
            imgBg?.visibility = View.VISIBLE
            imgBg?.setImageResource(themeConfig.resId)
            imgBg?.alpha = 1.0f // Imagen con brillo total
            
            overlay?.visibility = View.VISIBLE
            overlay?.setBackgroundColor(Color.parseColor("#99000000")) // Overlay al 60% para que se vea mucho más el fondo

            rootLayout?.setBackgroundColor(Color.TRANSPARENT)
            headerContainer?.setBackgroundColor(Color.TRANSPARENT)
            
            val accent = Color.parseColor(themeConfig.accent)
            title?.setTextColor(accent)
            label?.setTextColor(accent)
            btnBack?.imageTintList = android.content.res.ColorStateList.valueOf(accent)
            
            // Labels stats en blanco/claro para contraste sobre fondo oscuro
            vistasLabel?.setTextColor(Color.WHITE)
            monthLabel?.setTextColor(Color.WHITE)
            mediaLabel?.setTextColor(Color.WHITE)
            vistasLabel?.alpha = 0.7f
            monthLabel?.alpha = 0.7f
            mediaLabel?.alpha = 0.7f

            adapter?.updateTheme(true, accent, Color.parseColor(themeConfig.cardBg), Color.parseColor(themeConfig.stroke))
            window.statusBarColor = Color.parseColor(themeConfig.background)
        } else {
            imgBg?.visibility = View.GONE
            overlay?.visibility = View.GONE
            rootLayout?.setBackgroundColor(ContextCompat.getColor(this, R.color.background_app))
            headerContainer?.setBackgroundResource(R.color.surface_app)
            
            val primary = ContextCompat.getColor(this, R.color.primary)
            title?.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
            label?.setTextColor(primary)
            btnBack?.imageTintList = android.content.res.ColorStateList.valueOf(primary)
            
            vistasLabel?.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            monthLabel?.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            mediaLabel?.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            
            adapter?.updateTheme(false)
            window.statusBarColor = ContextCompat.getColor(this, R.color.background_app)
        }
    }

    private fun updateStats(entries: List<DiaryEntry>) {
        findViewById<TextView>(R.id.textTotalCount)?.text = entries.size.toString()
        if (entries.isNotEmpty()) {
            val avg = entries.map { it.rating }.average()
            findViewById<TextView>(R.id.textAvgRating)?.text = String.format(Locale.getDefault(), "%.1f", avg)
        } else {
            findViewById<TextView>(R.id.textAvgRating)?.text = "0.0"
        }
        val currentMonthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        val countThisMonth = entries.count { it.date.contains(currentMonthYear) }
        findViewById<TextView>(R.id.textMonthCount)?.text = countThisMonth.toString()
    }

    private fun formatMonth(date: String): String {
        return try {
            val input = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val output = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            val parsedDate = input.parse(date)
            val raw = output.format(parsedDate!!)
            raw.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) { "Sin fecha" }
    }

    private data class ThemeConfig(val resId: Int, val accent: String, val background: String, val cardBg: String, val stroke: String)
}
