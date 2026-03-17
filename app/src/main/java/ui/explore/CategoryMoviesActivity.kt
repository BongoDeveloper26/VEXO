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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_movies)

        val categoryTitle = intent.getStringExtra("categoryTitle") ?: "Películas"
        
        findViewById<TextView>(R.id.textCategoryFullTitle).text = categoryTitle
        findViewById<ImageButton>(R.id.btnBackCategory).setOnClickListener { finish() }

        val recyclerView: RecyclerView = findViewById(R.id.recyclerCategoryFull)
        // Configuramos 5 columnas para que las películas se vean muy pequeñas y quepan muchas por fila
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
        val progressBar: ProgressBar = findViewById(R.id.progressBarCategory) ?: ProgressBar(this)
        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val results = mutableListOf<Movie>()
                
                // Mapeo de títulos a IDs de género para cargar muchas páginas
                val genreId = when {
                    title.contains("Terror", true) -> 27
                    title.contains("Acción", true) -> 28
                    title.contains("Animación", true) -> 16
                    title.contains("Aventuras", true) -> 12
                    title.contains("Risas", true) || title.contains("Comedia", true) -> 35
                    title.contains("Crimen", true) -> 80
                    title.contains("Drama", true) -> 18
                    title.contains("Familia", true) -> 10751
                    title.contains("Fantasía", true) -> 14
                    title.contains("Historia", true) -> 36
                    title.contains("Musical", true) -> 10402
                    title.contains("Misterio", true) || title.contains("Intriga", true) -> 9648
                    title.contains("Románticas", true) -> 10749
                    title.contains("Ciencia Ficción", true) -> 878
                    title.contains("Thriller", true) -> 53
                    title.contains("Bélico", true) -> 10752
                    title.contains("Oeste", true) -> 37
                    else -> null
                }

                if (genreId != null) {
                    // Cargamos 10 páginas de golpe (aprox 200 pelis) para que la lista sea inmensa
                    for (page in 1..10) {
                        val pageMovies = repository.getMoviesByGenre(listOf(genreId), page)
                        results.addAll(pageMovies)
                    }
                } else {
                    // Para categorías especiales como Tendencias o Valoradas
                    when {
                        title.contains("Semana", true) || title.contains("Top", true) -> {
                            for (page in 1..5) {
                                results.addAll(repository.getTrendingMovies(page))
                            }
                        }
                        title.contains("Valoradas", true) -> {
                            for (page in 1..5) {
                                results.addAll(repository.getTopRatedMovies(page))
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
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}
