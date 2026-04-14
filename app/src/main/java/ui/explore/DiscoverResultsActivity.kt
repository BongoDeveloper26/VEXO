package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vexo.app.R
import data.repository.TMDBRepository
import kotlinx.coroutines.launch
import ui.detail.DetailActivity

class DiscoverResultsActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private lateinit var adapter: MovieAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoResults: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover_results)

        setupHeader()
        setupRecyclerView()
        loadResults()
    }

    private fun setupHeader() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<TextView>(R.id.textTitle).text = "Resultados Discover"
    }

    private fun setupRecyclerView() {
        progressBar = findViewById(R.id.progressBar)
        textNoResults = findViewById(R.id.textNoResults)
        val recycler = findViewById<RecyclerView>(R.id.recyclerResults)
        
        adapter = MovieAdapter(emptyList(), true).apply {
            onItemClick = { movie ->
                val intent = Intent(this@DiscoverResultsActivity, DetailActivity::class.java)
                intent.putExtra("movie", movie)
                startActivity(intent)
            }
        }
        
        recycler.layoutManager = GridLayoutManager(this, 3)
        recycler.adapter = adapter
    }

    private fun loadResults() {
        val genres = intent.getIntegerArrayListExtra("genres")
        val year = intent.getIntExtra("year", -1).takeIf { it != -1 }
        val rating = intent.getFloatExtra("rating", 0f)
        val isTv = intent.getBooleanExtra("isTv", false)

        progressBar.visibility = View.VISIBLE
        
        lifecycleScope.launch {
            // Corregido: Usamos parámetros nombrados para coincidir con la nueva firma de rango de fechas
            val results = if (isTv) {
                repository.discoverTV(genreIds = genres, yearStart = year, yearEnd = year, minRating = rating)
            } else {
                repository.discoverMovies(genreIds = genres, yearStart = year, yearEnd = year, minRating = rating)
            }
            
            progressBar.visibility = View.GONE
            if (results.isEmpty()) {
                textNoResults.visibility = View.VISIBLE
            } else {
                adapter.updateMovies(results)
            }
        }
    }
}
