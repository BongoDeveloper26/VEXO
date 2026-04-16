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
import data.model.Movie
import data.repository.TMDBRepository
import kotlinx.coroutines.launch
import ui.detail.DetailActivity

class CategoryMoviesActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private lateinit var adapter: MovieAdapter
    private val allMovies = mutableListOf<Movie>()
    private var isTvCategory = false
    private var passedGenreId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_movies)

        val categoryTitle = intent.getStringExtra("categoryTitle") ?: "Películas"
        isTvCategory = intent.getBooleanExtra("isTv", false)
        passedGenreId = intent.getIntExtra("genreId", -1)
        
        findViewById<TextView>(R.id.textCategoryFullTitle).text = categoryTitle
        findViewById<ImageButton>(R.id.btnBackCategory).setOnClickListener { finish() }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerCategoryFull)
        recyclerView.layoutManager = GridLayoutManager(this, 5)
        
        adapter = MovieAdapter(allMovies, isGridView = true)
        adapter.onItemClick = { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        loadManyMovies(categoryTitle)
    }

    private fun loadManyMovies(title: String) {
        val progressBar: ProgressBar = findViewById(R.id.progressBarCategory)
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val results = mutableListOf<Movie>()
                
                // Si tenemos un genreId directo, lo usamos (prioridad absoluta)
                if (passedGenreId != -1) {
                    for (page in 1..10) {
                        val pageResults = when {
                            passedGenreId == 9715 -> { // Caso especial Superhéroes (es una Keyword)
                                if (isTvCategory) repository.discoverTV(keywords = listOf(passedGenreId), page = page)
                                else repository.discoverMovies(keywords = listOf(passedGenreId), page = page)
                            }
                            passedGenreId == 53 || passedGenreId == 27 -> { // Suspense y Terror en TV usan discover
                                if (isTvCategory) repository.discoverTV(genreIds = listOf(passedGenreId), page = page)
                                else repository.getMoviesByGenre(listOf(passedGenreId), page)
                            }
                            isTvCategory -> repository.getTVByGenre(listOf(passedGenreId), page)
                            else -> repository.getMoviesByGenre(listOf(passedGenreId), page)
                        }
                        results.addAll(pageResults)
                    }
                } else {
                    // Para categorías que no son géneros (Tendencias, Valoradas, etc.)
                    when {
                        title.contains("Semana", true) || title.contains("Tendencia", true) || title.contains("Trending", true) -> {
                            for (page in 1..5) {
                                if (isTvCategory) results.addAll(repository.getTrendingTV(page))
                                else results.addAll(repository.getTrendingMovies(page))
                            }
                        }
                        title.contains("Valoradas", true) -> {
                            for (page in 1..5) {
                                if (isTvCategory) results.addAll(repository.getTopRatedTV(page))
                                else results.addAll(repository.getTopRatedMovies(page))
                            }
                        }
                        title.contains("Cines", true) -> {
                            for (page in 1..5) {
                                results.addAll(repository.getNowPlayingMovies(page))
                            }
                        }
                    }
                }

                allMovies.clear()
                allMovies.addAll(results.distinctBy { it.id })
                adapter.updateMovies(allMovies)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
