package ui.genre

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vexo.app.R
import data.model.Category
import data.repository.TMDBRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import ui.detail.DetailActivity
import ui.explore.CategoryAdapter

class GenreActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genre)

        val genreId = intent.getIntExtra("genreId", -1)
        val genreName = intent.getStringExtra("genreName") ?: ""

        if (genreId == -1) {
            finish()
            return
        }

        setupHeader(genreName)
        setupRecyclerView()
        loadGenreContent(genreId, genreName)
    }

    private fun setupHeader(genreName: String) {
        findViewById<TextView>(R.id.textGenreTitleHeader).text = genreName
        findViewById<ImageButton>(R.id.btnBackGenre).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        val recycler = findViewById<RecyclerView>(R.id.recyclerGenreSections)
        adapter = CategoryAdapter(emptyList()) { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter
    }

    private fun loadGenreContent(genreId: Int, genreName: String) {
        val isSpanish = repository.getLanguage() == "es-ES"
        
        lifecycleScope.launch {
            // Cargamos diferentes secciones para el género
            // Corregido: Ahora pasamos listOf(genreId) porque el repositorio espera una lista
            val popular = async { repository.getMoviesByGenre(listOf(genreId), sortBy = "popularity.desc") }
            val topRated = async { repository.getMoviesByGenre(listOf(genreId), sortBy = "vote_average.desc", voteCountGte = 1000) }
            val newReleases = async { repository.getMoviesByGenre(listOf(genreId), sortBy = "primary_release_date.desc", voteCountGte = 100) }
            val hiddenGems = async { repository.getMoviesByGenre(listOf(genreId), sortBy = "vote_average.desc", voteCountGte = 200) }

            val categories = listOf(
                Category(
                    if (isSpanish) "Lo más popular en $genreName" else "Most Popular in $genreName",
                    popular.await().filter { it.posterPath != null }
                ),
                Category(
                    if (isSpanish) "Obras maestras de $genreName" else "$genreName Masterpieces",
                    topRated.await().filter { it.posterPath != null }
                ),
                Category(
                    if (isSpanish) "Novedades" else "New Releases",
                    newReleases.await().filter { it.posterPath != null }
                ),
                Category(
                    if (isSpanish) "Joyas ocultas" else "Hidden Gems",
                    hiddenGems.await().filter { it.posterPath != null }
                )
            )

            adapter.updateCategories(categories.filter { it.movies.isNotEmpty() })
        }
    }
}
