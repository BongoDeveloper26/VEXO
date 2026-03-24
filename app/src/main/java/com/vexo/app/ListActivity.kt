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
import com.google.android.material.tabs.TabLayout
import data.model.UserList
import data.repository.WatchlistRepository

class ListActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var listAdapter: UserListAdapter
    private lateinit var vexoAdapter: VexoListAdapter

    // DEFINICIÓN ÚNICA Y FIJA DE LAS LISTAS OFICIALES
    private val officialVexoLists = listOf(
        VexoList("top_250_movies", "Las 250 mejores películas", "La selección oficial con las obras maestras del cine.", R.drawable.vexo_logo),
        VexoList("top_250_tv", "Las 250 mejores series", "El ranking definitivo con las mejores series de la historia.", R.drawable.vexo_logo)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        watchlistRepository = WatchlistRepository(this)
        
        setupUserListsView()
        setupVexoListsView()
        setupTabs()
    }

    override fun onResume() {
        super.onResume()
        refreshUserListsData()
    }

    private fun setupUserListsView() {
        findViewById<ImageButton>(R.id.btnBackList).setOnClickListener { finish() }
        findViewById<View>(R.id.btnCreateList).setOnClickListener { showCreateListDialog() }

        val recycler = findViewById<RecyclerView>(R.id.recyclerUserLists)
        recycler.layoutManager = LinearLayoutManager(this)
        listAdapter = UserListAdapter(emptyList(), 
            onListClick = { openListDetail(it) },
            onDeleteClick = { showDeleteConfirmDialog(it) }
        )
        recycler.adapter = listAdapter
    }

    private fun setupVexoListsView() {
        val recyclerVexo = findViewById<RecyclerView>(R.id.recyclerVexoLists)
        recyclerVexo.layoutManager = LinearLayoutManager(this)
        
        vexoAdapter = VexoListAdapter(officialVexoLists) { item ->
            val intent = Intent(this, VexoListDetailActivity::class.java)
            intent.putExtra("listId", item.id)
            intent.putExtra("listName", item.name)
            startActivity(intent)
        }
        recyclerVexo.adapter = vexoAdapter
    }

    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutLists)
        val containerUser = findViewById<View>(R.id.containerMisColecciones)
        val containerVexo = findViewById<View>(R.id.containerOtrasListas)
        val btnCreate = findViewById<View>(R.id.btnCreateList)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    containerUser.visibility = View.VISIBLE
                    containerVexo.visibility = View.GONE
                    btnCreate.visibility = View.VISIBLE
                } else {
                    containerUser.visibility = View.GONE
                    containerVexo.visibility = View.VISIBLE
                    btnCreate.visibility = View.GONE
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun refreshUserListsData() {
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

    private fun showCreateListDialog() {
        val input = EditText(this).apply { hint = "Nombre de la lista" }
        AlertDialog.Builder(this)
            .setTitle("Nueva Colección")
            .setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    watchlistRepository.createUserList(name)
                    refreshUserListsData()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDeleteConfirmDialog(userList: UserList) {
        AlertDialog.Builder(this)
            .setTitle("¿Eliminar?")
            .setMessage("¿Borrar '${userList.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                watchlistRepository.deleteUserList(userList.id)
                refreshUserListsData()
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

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.textListName)
        val count: TextView = v.findViewById(R.id.textMovieCount)
        val btnDelete: View = v.findViewById(R.id.btnDeleteList)
        val img1: ImageView = v.findViewById(R.id.imgPreview1)
        val img2: ImageView = v.findViewById(R.id.imgPreview2)
        val img3: ImageView = v.findViewById(R.id.imgPreview3)
        val img4: ImageView = v.findViewById(R.id.imgPreview4)
        val textMore: TextView = v.findViewById(R.id.textMoreMovies)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_user_list, p, false))

    override fun onBindViewHolder(h: ViewHolder, p: Int) {
        val l = lists[p]
        h.name.text = l.name
        h.count.text = "${l.movies.size} ELEMENTOS"
        val imgs = listOf(h.img1, h.img2, h.img3, h.img4)
        imgs.forEach { it.visibility = View.GONE }
        h.textMore.visibility = View.GONE
        l.movies.take(4).forEachIndexed { i, m ->
            imgs[i].visibility = View.VISIBLE
            Glide.with(h.itemView.context).load(m.posterPath).centerCrop().into(imgs[i])
        }
        if (l.movies.size > 4) { h.textMore.visibility = View.VISIBLE; h.textMore.text = "+${l.movies.size-4}" }
        h.itemView.setOnClickListener { onListClick(l) }
        h.btnDelete.setOnClickListener { onDeleteClick(l) }
    }

    override fun getItemCount() = lists.size
    fun updateLists(n: List<UserList>) { lists = n; notifyDataSetChanged() }
}
