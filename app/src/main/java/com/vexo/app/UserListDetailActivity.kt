package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import data.model.UserList
import data.repository.WatchlistRepository
import ui.detail.DetailActivity
import ui.explore.MovieAdapter

class UserListDetailActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var movieAdapter: MovieAdapter
    private var userListId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_list_detail)

        watchlistRepository = WatchlistRepository(this)
        userListId = intent.getStringExtra("listId")
        val listName = intent.getStringExtra("listName")

        if (userListId == null) {
            finish()
            return
        }

        setupUI(listName ?: "")
    }

    override fun onResume() {
        super.onResume()
        refreshMovies()
    }

    private fun setupUI(name: String) {
        findViewById<TextView>(R.id.textUserListNameHeader).text = name
        findViewById<ImageButton>(R.id.btnBackUserListDetail).setOnClickListener { finish() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerUserListMovies)
        
        // Cambiamos a 3 columnas. Es el estándar de la industria (Netflix, Disney+) 
        // para que los posters se vean grandes, nítidos y profesionales en el móvil.
        val gridLayoutManager = GridLayoutManager(this, 3)
        recycler.layoutManager = gridLayoutManager

        // Usamos el modo Grid (true) en el adaptador
        movieAdapter = MovieAdapter(emptyList(), isGridView = true)
        movieAdapter.onItemClick = { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        recycler.adapter = movieAdapter
        
        // Añadimos un poco de padding al recycler para que no pegue a los bordes
        recycler.setPadding(12, 0, 12, 20)
    }

    private fun refreshMovies() {
        val lists = watchlistRepository.getUserLists()
        val currentList = lists.find { it.id == userListId }
        
        if (currentList != null) {
            val movies = currentList.movies
            findViewById<View>(R.id.layoutEmptyUserList).visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
            movieAdapter.updateMovies(movies)
        }
    }
}
