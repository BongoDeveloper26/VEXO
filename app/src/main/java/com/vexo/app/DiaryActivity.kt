package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
            findViewById<View>(R.id.layoutDiaryStats).visibility = View.GONE
        }

        loadDiary()
    }

    private fun loadDiary() {
        var diaryEntries = watchlistRepository.getDiary()
        
        if (onlyReviews) {
            diaryEntries = diaryEntries.filter { !it.review.isNullOrEmpty() }
        }
        
        // Calcular estadísticas si no es modo reseñas
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
        
        // Pasamos showTimeline = false y la función isFavorite
        recycler.adapter = DiaryGroupedAdapter(
            groupedItems, 
            showTimeline = !onlyReviews,
            isFavorite = { movieId -> watchlistRepository.isFavorite(movieId) }
        ) { entry ->
            val movieToOpen = entry.movie ?: watchlistRepository.getAllRatedMovies().find { it.id == entry.movieId }
            
            if (movieToOpen != null) {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("movie", movieToOpen)
                startActivity(intent)
            } else {
                Toast.makeText(this, "No se pudo abrir la ficha de la película", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateStats(entries: List<DiaryEntry>) {
        val textTotal = findViewById<TextView>(R.id.textTotalCount)
        val textMonth = findViewById<TextView>(R.id.textMonthCount)
        val textAvg = findViewById<TextView>(R.id.textAvgRating)

        // Total
        textTotal.text = entries.size.toString()

        // Media
        if (entries.isNotEmpty()) {
            val avg = entries.map { it.rating }.average()
            textAvg.text = String.format(Locale.getDefault(), "%.1f", avg)
        } else {
            textAvg.text = "0.0"
        }

        // Este mes
        val currentMonthYear = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
        val countThisMonth = entries.count { it.date.contains(currentMonthYear) }
        textMonth.text = countThisMonth.toString()
    }

    private fun formatMonth(date: String): String {
        return try {
            val input = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val output = SimpleDateFormat("MMMM yyyy", Locale("es", "ES"))
            val parsedDate = input.parse(date)
            val raw = output.format(parsedDate!!)
            raw.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "Sin fecha"
        }
    }
}
