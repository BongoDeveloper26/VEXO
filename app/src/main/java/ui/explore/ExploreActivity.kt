package ui.explore

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ui.detail.DetailActivity
import ui.explore.ExploreViewModel
import ui.explore.MovieAdapter
import com.vexo.app.R

class ExploreActivity : AppCompatActivity() {

    private val viewModel: ExploreViewModel by viewModels()
    private lateinit var recyclerMovies: RecyclerView
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        // Configurar RecyclerView
        recyclerMovies = findViewById(R.id.recyclerMovies)
        movieAdapter = MovieAdapter(emptyList())
        recyclerMovies.adapter = movieAdapter
        recyclerMovies.layoutManager = LinearLayoutManager(this)

        // Listener para cuando se pulse una película
        movieAdapter.onItemClick = { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }

        // Cargar las películas
        viewModel.loadTrendingMovies()

        // Observar cambios en el LiveData
        viewModel.movies.observe(this) { movies ->
            movieAdapter.updateMovies(movies)
        }
    }
}