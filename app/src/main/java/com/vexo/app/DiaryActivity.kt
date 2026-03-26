package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
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
import java.util.Locale

class DiaryActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        watchlistRepository = WatchlistRepository(this)

        findViewById<ImageButton>(R.id.btnBackDiary).setOnClickListener {
            finish()
        }

        loadDiary()
    }

    private fun loadDiary() {
        val diaryEntries = watchlistRepository.getDiary()

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
        
        recycler.adapter = DiaryGroupedAdapter(groupedItems) { entry ->
            // CORRECCIÓN: Usamos el objeto movie guardado en la entrada o lo buscamos como backup
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
