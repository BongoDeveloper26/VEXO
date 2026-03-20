package com.vexo.app

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
import data.model.Movie
import data.repository.TMDBRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import ui.detail.DetailActivity
import ui.explore.MovieAdapter

class VexoListDetailActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private lateinit var movieAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list_detail) // Reutilizamos el layout que ya es perfecto

        val listName = intent.getStringExtra("listName") ?: "Top 250"
        setupUI(listName)
        loadTop250Movies()
    }

    private fun setupUI(name: String) {
        findViewById<TextView>(R.id.textUserListNameHeader).text = name
        findViewById<ImageButton>(R.id.btnBackUserListDetail).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerUserListMovies)
        recycler.layoutManager = GridLayoutManager(this, 3)
        
        movieAdapter = MovieAdapter(emptyList(), isGridView = true)
        movieAdapter.onItemClick = { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        recycler.adapter = movieAdapter
    }

    private fun loadTop250Movies() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                // TMDB devuelve 20 pelis por página. Para 250 necesitamos unas 13 páginas.
                val allMovies = mutableListOf<Movie>()
                val deferredPages = (1..13).map { page ->
                    async { repository.getTopRatedMovies(page) }
                }
                
                deferredPages.awaitAll().forEach { pageResults ->
                    allMovies.addAll(pageResults)
                }

                movieAdapter.updateMovies(allMovies.take(250))
            } catch (e: Exception) {
                // Manejar error si no hay internet
            } finally {
                progress.visibility = View.GONE
            }
        }
    }
}
