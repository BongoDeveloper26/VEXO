package com.vexo.app

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
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
        findViewById<TextView>(R.id.textUserListNameHeader).text = name.uppercase()
        findViewById<ImageButton>(R.id.btnBackUserListDetail).setOnClickListener { finish() }

        // Personalización para "Mis Listas" (Cuenta del usuario)
        val imgProfile = findViewById<ImageView>(R.id.imgCreatorProfile)
        imgProfile.setImageResource(R.drawable.ic_nav_profile)
        imgProfile.setPadding(15, 15, 15, 15)
        imgProfile.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
        
        findViewById<TextView>(R.id.textCreatorName).text = "Usuario VEXO"
        findViewById<TextView>(R.id.textListInfo).text = "Tu colección personal"

        val recycler = findViewById<RecyclerView>(R.id.recyclerUserListMovies)
        val gridLayoutManager = GridLayoutManager(this, 3)
        recycler.layoutManager = gridLayoutManager

        movieAdapter = MovieAdapter(emptyList(), isGridView = true)
        movieAdapter.onItemClick = { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        recycler.adapter = movieAdapter
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
