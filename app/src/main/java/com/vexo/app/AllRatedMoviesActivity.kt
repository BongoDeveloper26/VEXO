package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import data.repository.WatchlistRepository
import ui.detail.DetailActivity

class AllRatedMoviesActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var adapter: RecentActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_rated_movies)

        watchlistRepository = WatchlistRepository(this)
        setupUI()
    }

    private fun setupUI() {
        findViewById<ImageButton>(R.id.btnBackAllRated).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerAllRatedMovies)
        val ratedMovies = watchlistRepository.getAllRatedMovies()

        if (ratedMovies.isEmpty()) {
            findViewById<View>(R.id.layoutEmptyAllRated).visibility = View.VISIBLE
            recycler.visibility = View.GONE
        } else {
            findViewById<View>(R.id.layoutEmptyAllRated).visibility = View.GONE
            recycler.visibility = View.VISIBLE
            
            // Usamos un Grid para que se vea como una colección
            recycler.layoutManager = GridLayoutManager(this, 3)
            adapter = RecentActivityAdapter(ratedMovies, watchlistRepository) { movie ->
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("movie", movie)
                startActivity(intent)
            }
            recycler.adapter = adapter
        }
    }
}
