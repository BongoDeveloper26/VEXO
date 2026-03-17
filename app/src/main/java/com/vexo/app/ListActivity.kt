package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import data.model.UserList
import data.repository.WatchlistRepository

class ListActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var listAdapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        watchlistRepository = WatchlistRepository(this)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        refreshLists()
    }

    private fun setupUI() {
        findViewById<ImageButton>(R.id.btnBackList).setOnClickListener { finish() }
        findViewById<View>(R.id.btnCreateList).setOnClickListener { showCreateListDialog() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerUserLists)
        listAdapter = UserListAdapter(emptyList(), 
            onListClick = { userList -> openListDetail(userList) },
            onDeleteClick = { userList -> showDeleteConfirmDialog(userList) }
        )
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = listAdapter
    }

    private fun showCreateListDialog() {
        val input = EditText(this).apply { hint = "Nombre de la lista (ej: Terror)" }
        AlertDialog.Builder(this)
            .setTitle("Nueva Colección")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    watchlistRepository.createUserList(name)
                    refreshLists()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun refreshLists() {
        val lists = watchlistRepository.getUserLists()
        findViewById<View>(R.id.layoutEmptyCollections).visibility = if (lists.isEmpty()) View.VISIBLE else View.GONE
        listAdapter.updateLists(lists)
    }

    private fun openListDetail(userList: UserList) {
        val intent = Intent(this, UserListDetailActivity::class.java)
        intent.putExtra("listId", userList.id)
        intent.putExtra("listName", userList.name)
        startActivity(intent)
    }

    private fun showDeleteConfirmDialog(userList: UserList) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar lista?")
            .setMessage("Se borrará la colección '${userList.name}' y todas las pelis que contiene.")
            .setPositiveButton("Eliminar") { _, _ ->
                watchlistRepository.deleteUserList(userList.id)
                refreshLists()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}

class UserListAdapter(
    private var lists: List<UserList>,
    private val onListClick: (UserList) -> Unit,
    private val onDeleteClick: (UserList) -> Unit
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textListName)
        val count: TextView = view.findViewById(R.id.textMovieCount)
        val btnDelete: View = view.findViewById(R.id.btnDeleteList)
        val img1: ImageView = view.findViewById(R.id.imgPreview1)
        val img2: ImageView = view.findViewById(R.id.imgPreview2)
        val img3: ImageView = view.findViewById(R.id.imgPreview3)
        val img4: ImageView = view.findViewById(R.id.imgPreview4)
        val textMore: TextView = view.findViewById(R.id.textMoreMovies)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.name.text = list.name
        holder.count.text = "${list.movies.size} PELÍCULAS"
        
        // Mostrar previews de los pósters
        val previews = listOf(holder.img1, holder.img2, holder.img3, holder.img4)
        previews.forEach { it.visibility = View.GONE }
        holder.textMore.visibility = View.GONE

        list.movies.take(4).forEachIndexed { index, movie ->
            previews[index].visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(movie.posterPath).centerCrop().into(previews[index])
        }

        if (list.movies.size > 4) {
            holder.textMore.visibility = View.VISIBLE
            holder.textMore.text = "+${list.movies.size - 4}"
        }

        holder.itemView.setOnClickListener { onListClick(list) }
        holder.btnDelete.setOnClickListener { onDeleteClick(list) }
    }

    override fun getItemCount() = lists.size

    fun updateLists(newLists: List<UserList>) {
        lists = newLists
        notifyDataSetChanged()
    }
}
