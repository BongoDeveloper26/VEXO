package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
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
            "harry_potter" -> loadHarryPotterSaga()
            "john_wick_universe" -> loadJohnWickUniverse()
            else -> loadTop250Movies()
        }
    }

    private fun setupUI(name: String, description: String) {
        findViewById<TextView>(R.id.textUserListNameHeader).text = name.uppercase()
        
        val descView = findViewById<TextView>(R.id.textUserListDescription)
        val btnReadMore = findViewById<TextView>(R.id.btnReadMore)

        descView.text = description
        descView.visibility = if (description.isNotEmpty()) View.VISIBLE else View.GONE

        // Lógica precisa para el botón "Leer más"
        descView.maxLines = Int.MAX_VALUE // Primero permitimos todo el texto para medir
        btnReadMore.visibility = View.GONE

        descView.post {
            if (descView.lineCount > 3) {
                descView.maxLines = 3
                btnReadMore.visibility = View.VISIBLE
                btnReadMore.text = "..."
            }
        }

        btnReadMore.setOnClickListener {
            if (descView.maxLines == 3) {
                descView.maxLines = Int.MAX_VALUE
                btnReadMore.text = "Leer menos"
            } else {
                descView.maxLines = 3
                btnReadMore.text = "..."
            }
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
                val deferredPages = (1..13).map { page -> async { repository.getTopRatedMovies(page) } }
                deferredPages.awaitAll().forEach { allMovies.addAll(it) }
                movieAdapter.updateMovies(allMovies.take(250))
            } catch (e: Exception) { e.printStackTrace() } finally { progress.visibility = View.GONE }
        }
    }

    private fun loadTop250Series() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val allSeries = mutableListOf<Movie>()
                val deferredPages = (1..13).map { page -> async { repository.getTopRatedTV(page) } }
                deferredPages.awaitAll().forEach { allSeries.addAll(it) }
                movieAdapter.updateMovies(allSeries.take(250))
            } catch (e: Exception) { e.printStackTrace() } finally { progress.visibility = View.GONE }
        }
    }

    private fun loadMarvelUniverse() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val allContent = mutableListOf<Movie>()
                val movieDeferred = (1..6).map { page -> async { repository.discoverMovies(sortBy = "primary_release_date.desc", page = page, companyIds = listOf(420)) } }
                val tvDeferred = (1..4).map { page -> async { repository.discoverTV(sortBy = "first_air_date.desc", page = page, companyIds = listOf(420)) } }
                val movieResults = movieDeferred.awaitAll()
                val tvResults = tvDeferred.awaitAll()
                movieResults.forEach { movies -> allContent.addAll(movies.filter { !it.genreIds.contains(99) && !(it.title ?: "").contains("Making of", true) }) }
                tvResults.forEach { series -> allContent.addAll(series.filter { !it.genreIds.contains(99) }) }
                val finalResult = allContent.distinctBy { it.id }.sortedByDescending { it.releaseDate }
                movieAdapter.updateMovies(finalResult)
                findViewById<TextView>(R.id.textListInfo).text = "MCU COMPLETO • ${finalResult.size} ELEMENTOS"
            } catch (e: Exception) { e.printStackTrace() } finally { progress.visibility = View.GONE }
        }
    }

    private fun loadStarWarsUniverse() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val allContent = mutableListOf<Movie>()
                val movieDeferred = (1..8).map { page -> async { repository.discoverMovies(sortBy = "primary_release_date.desc", page = page, companyIds = listOf(1)) } }
                val tvDeferred = (1..5).map { page -> async { repository.discoverTV(sortBy = "first_air_date.desc", page = page, companyIds = listOf(1)) } }
                val movieResults = movieDeferred.awaitAll()
                val tvResults = tvDeferred.awaitAll()
                movieResults.forEach { movies -> allContent.addAll(movies.filter { ((it.title ?: "").contains("Star Wars", true)) && !it.genreIds.contains(99) }) }
                tvResults.forEach { series -> allContent.addAll(series.filter { (it.title?.contains("Star Wars", true) ?: false) || (it.title?.contains("Mandalorian", true) ?: false) }) }
                val finalResult = allContent.distinctBy { it.id }.sortedByDescending { it.releaseDate }
                movieAdapter.updateMovies(finalResult)
                findViewById<TextView>(R.id.textListInfo).text = "SAGA COMPLETA • ${finalResult.size} ELEMENTOS"
            } catch (e: Exception) { e.printStackTrace() } finally { progress.visibility = View.GONE }
        }
    }

    private fun loadHarryPotterSaga() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val allContent = mutableListOf<Movie>()
                val hpCol = async { repository.getCollectionMovies(1241) } 
                val fbCol = async { repository.getCollectionMovies(435637) } 
                allContent.addAll(hpCol.await())
                allContent.addAll(fbCol.await())
                val finalResult = allContent.distinctBy { it.id }.filter { !it.title.isNullOrEmpty() }.sortedBy { it.releaseDate }
                movieAdapter.updateMovies(finalResult)
                findViewById<TextView>(R.id.textListInfo).text = "WIZARDING WORLD • ${finalResult.size} ELEMENTOS"
            } catch (e: Exception) { e.printStackTrace() } finally { progress.visibility = View.GONE }
        }
    }

    private fun loadJohnWickUniverse() {
        val progress = findViewById<ProgressBar>(R.id.progressUserList)
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val allContent = mutableListOf<Movie>()
                
                // 1. Saga principal (John Wick 1, 2, 3 y 4) vía Colección
                val wickCol = async { repository.getCollectionMovies(297753) }
                
                // 2. Ballerina (Spin-off)
                val ballerina = async { repository.searchMovies("John Wick Ballerina", 1) }
                
                // 3. The Continental (Serie)
                val continental = async { repository.searchTV("The Continental", 1) }

                allContent.addAll(wickCol.await())
                allContent.addAll(ballerina.await().filter { it.title.contains("Ballerina", true) })
                allContent.addAll(continental.await().filter { it.title.contains("Continental", true) })

                val finalResult = allContent
                    .distinctBy { it.id }
                    .filter { !it.title.isNullOrEmpty() }
                    .sortedBy { it.releaseDate }

                movieAdapter.updateMovies(finalResult)
                findViewById<TextView>(R.id.textListInfo).text = "JOHN WICK UNIVERSE • ${finalResult.size} ELEMENTOS"
            } catch (e: Exception) { 
                e.printStackTrace() 
            } finally { 
                progress.visibility = View.GONE 
            }
        }
    }
}
