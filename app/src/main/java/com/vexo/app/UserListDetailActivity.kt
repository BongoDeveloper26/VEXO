package com.vexo.app

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputFilter
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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

        if (userListId == null) {
            finish()
            return
        }

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        refreshListData()
    }

    private fun setupUI() {
        findViewById<ImageButton>(R.id.btnBackUserListDetail).setOnClickListener { finish() }

        // Personalización para "Mis Listas" (Cuenta del usuario)
        val imgProfile = findViewById<ImageView>(R.id.imgCreatorProfile)
        imgProfile.setImageResource(R.drawable.ic_nav_profile)
        imgProfile.setPadding(15, 15, 15, 15)
        imgProfile.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
        
        findViewById<TextView>(R.id.textCreatorName).text = watchlistRepository.getUserName()
        findViewById<TextView>(R.id.textListInfo).text = "Tu colección personal"

        val recycler = findViewById<RecyclerView>(R.id.recyclerUserListMovies)
        val gridLayoutManager = GridLayoutManager(this, 4)
        recycler.layoutManager = gridLayoutManager

        movieAdapter = MovieAdapter(emptyList(), isGridView = true)
        movieAdapter.onItemClick = { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        recycler.adapter = movieAdapter
        recycler.setPadding(12, 0, 12, 20)

        findViewById<ImageButton>(R.id.btnEditListName).setOnClickListener { showEditNameDialog() }
        findViewById<ImageButton>(R.id.btnDeleteUserList).setOnClickListener { showDeleteConfirmDialog() }
    }

    private fun refreshListData() {
        val lists = watchlistRepository.getUserLists()
        val currentList = lists.find { it.id == userListId }
        
        if (currentList != null) {
            findViewById<TextView>(R.id.textUserListNameHeader).text = currentList.name.uppercase()
            findViewById<TextView>(R.id.textUserListDescription).apply {
                text = currentList.description ?: ""
                visibility = if (currentList.description.isNullOrEmpty()) View.GONE else View.VISIBLE
            }
            
            val movies = currentList.movies
            findViewById<View>(R.id.layoutEmptyUserList).visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
            movieAdapter.updateMovies(movies)
        } else {
            finish()
        }
    }

    private fun showEditNameDialog() {
        val lists = watchlistRepository.getUserLists()
        val currentList = lists.find { it.id == userListId } ?: return

        val input = EditText(this).apply {
            setText(currentList.name)
            filters = arrayOf(InputFilter.LengthFilter(15))
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar nombre")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    watchlistRepository.updateUserList(currentList.id, newName, currentList.description)
                    refreshListData()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmDialog() {
        val lists = watchlistRepository.getUserLists()
        val currentList = lists.find { it.id == userListId } ?: return

        AlertDialog.Builder(this)
            .setTitle("¿Borrar lista?")
            .setMessage("¿Estás seguro de que quieres eliminar la lista \"${currentList.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                watchlistRepository.deleteUserList(currentList.id)
                finish()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
