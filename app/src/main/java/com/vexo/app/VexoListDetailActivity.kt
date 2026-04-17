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
        setContentView(R.layout.activity_user_list_detail)

        val listId = intent.getStringExtra("listId") ?: "top_250_movies"
        val listName = intent.getStringExtra("listName") ?: "Top 250"
        val listDesc = intent.getStringExtra("listDesc") ?: ""
        
        setupUI(listName, listDesc)
        
        when (listId) {
            "top_250_tv" -> loadTop250Series()
            "marvel_universe" -> loadMarvelUniverse()
            "star_wars_universe" -> loadStarWarsUniverse()
            else -> loadTop250Movies()
        }
    }

    private fun setupUI(name: String, description: String) {
        findViewById<TextView>(R.id.textUserListNameHeader).text = name.uppercase()
        findViewById<TextView>(R.id.textUserListDescription).apply {
            text = description
            visibility = if (description.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        findViewById<ImageButton>(R.id.btnBackUserListDetail).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerUserListMovies)
        recycler.layoutManager = GridLayoutManager(this, 4)
        
        movieAdapter = MovieAdapter(emptyList(), isGridView = true)
        movieAdapter.onItemClick = { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        recycler.adapter = movieAdapter

        findViewById<View>(R.id.btnLikeList).visibility = View.VISIBLE
        findViewById<View>(R.id.btnMoreOptions).visibility = View.GONE
    }

    private fun loadTop250Movies() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val allMovies = mutableListOf<Movie>()
                val deferredPages = (1..13).map { page ->
                    async { repository.getTopRatedMovies(page) }
                }
                deferredPages.awaitAll().forEach { allMovies.addAll(it) }
                movieAdapter.updateMovies(allMovies.take(250))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun loadTop250Series() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val allSeries = mutableListOf<Movie>()
                val deferredPages = (1..13).map { page ->
                    async { repository.getTopRatedTV(page) }
                }
                deferredPages.awaitAll().forEach { allSeries.addAll(it) }
                movieAdapter.updateMovies(allSeries.take(250))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun loadMarvelUniverse() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val allContent = mutableListOf<Movie>()
                
                val movieDeferred = (1..6).map { page ->
                    async { 
                        repository.discoverMovies(
                            sortBy = "primary_release_date.desc",
                            page = page,
                            companyIds = listOf(420)
                        )
                    }
                }
                
                val tvDeferred = (1..4).map { page ->
                    async {
                        repository.discoverTV(
                            sortBy = "first_air_date.desc",
                            page = page,
                            companyIds = listOf(420)
                        )
                    }
                }

                val movieResults = movieDeferred.awaitAll()
                val tvResults = tvDeferred.awaitAll()

                movieResults.forEach { movies ->
                    allContent.addAll(movies.filter { movie ->
                        !movie.genreIds.contains(99) && 
                        !movie.title.contains("Making of", true) &&
                        !movie.title.contains("Assembled", true)
                    })
                }

                tvResults.forEach { series ->
                    allContent.addAll(series.filter { show ->
                        !show.genreIds.contains(99) &&
                        !show.title.contains("Making of", true) &&
                        !show.title.contains("Assembled", true)
                    })
                }

                val finalResult = allContent
                    .distinctBy { it.id }
                    .sortedByDescending { it.releaseDate }

                movieAdapter.updateMovies(finalResult)
                findViewById<TextView>(R.id.textListInfo).text = "MCU COMPLETO • ${finalResult.size} ELEMENTOS"

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }

    private fun loadStarWarsUniverse() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val allContent = mutableListOf<Movie>()
                
                // Lucasfilm Ltd. ID es 1
                // Ampliamos la búsqueda a más páginas para no perder nada
                val movieDeferred = (1..8).map { page ->
                    async { 
                        repository.discoverMovies(
                            sortBy = "primary_release_date.desc",
                            page = page,
                            companyIds = listOf(1)
                        )
                    }
                }
                
                val tvDeferred = (1..5).map { page ->
                    async {
                        repository.discoverTV(
                            sortBy = "first_air_date.desc",
                            page = page,
                            companyIds = listOf(1)
                        )
                    }
                }

                val movieResults = movieDeferred.awaitAll()
                val tvResults = tvDeferred.awaitAll()

                movieResults.forEach { movies ->
                    allContent.addAll(movies.filter { movie ->
                        // Filtro más permisivo para Star Wars (títulos que contienen "Star Wars", "Vader", "Skywalker", etc.)
                        (movie.title.contains("Star Wars", true) || 
                         movie.title.contains("Skywalker", true) || 
                         movie.title.contains("Empire Strikes", true) ||
                         movie.title.contains("Jedi", true)) && 
                        !movie.genreIds.contains(99) &&
                        !movie.title.contains("Making of", true) &&
                        !movie.title.contains("Legacy of", true)
                    })
                }

                tvResults.forEach { series ->
                    allContent.addAll(series.filter { show ->
                        (show.title.contains("Star Wars", true) || 
                         show.title.contains("Mandalorian", true) || 
                         show.title.contains("Ahsoka", true) ||
                         show.title.contains("Andor", true) ||
                         show.title.contains("Obi-Wan", true) ||
                         show.title.contains("Boba Fett", true) ||
                         show.title.contains("Bad Batch", true) ||
                         show.title.contains("Clone Wars", true) ||
                         show.title.contains("Rebels", true)) && 
                        !show.genreIds.contains(99)
                    })
                }

                val finalResult = allContent
                    .distinctBy { it.id }
                    .sortedByDescending { it.releaseDate }

                movieAdapter.updateMovies(finalResult)
                findViewById<TextView>(R.id.textListInfo).text = "SAGA COMPLETA • ${finalResult.size} ELEMENTOS"

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                progress.visibility = View.GONE
            }
        }
    }
}
