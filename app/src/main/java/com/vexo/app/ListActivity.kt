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
import androidx.core.widget.addTextChangedListener
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

    private val officialVexoLists = listOf(
        VexoList(
            "top_250_movies", 
            "LAS 250 MEJORES PELÍCULAS", 
            "La selección oficial de cine con las películas mejor valoradas de todos los tiempos.", 
            R.drawable.vexo_logo,
            listOf(
                "https://image.tmdb.org/t/p/w500/q6y0Go1tsYmUuAtfj6KyB30OXvN.jpg",
                "https://image.tmdb.org/t/p/w500/3bhkrjSTWv4ayisdqAs6arW0Lja.jpg",
                "https://image.tmdb.org/t/p/w500/8S9NoP10n5S5G87G9vGf56zP167.jpg",
                "https://image.tmdb.org/t/p/w500/v9970pP2XF8X8Z6n1P2pS5X8X8Z.jpg"
            )
        ),
        VexoList(
            "top_250_tv", 
            "LAS 250 MEJORES SERIES", 
            "El ranking oficial de televisión con las producciones más aclamadas por la crítica.", 
            R.drawable.vexo_logo,
            listOf(
                "https://image.tmdb.org/t/p/w500/ztkUQvBZ77Z7iB1u66NuJvSTN7h.jpg",
                "https://image.tmdb.org/t/p/w500/7WsyChvLEz79BMo33owrR7Z9XnS.jpg",
                "https://image.tmdb.org/t/p/w500/reksS7S7S7S7S7S7S7S7S7S7S7S.jpg",
                "https://image.tmdb.org/t/p/w500/69Uqt7vSbeFwb1L3rsLbt64H64o.jpg"
            )
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        watchlistRepository = WatchlistRepository(this)
        
        setupUserListsView()
        setupVexoListsView()
        setupSearchLogic()
        setupTabs()
    }

    override fun onResume() {
        super.onResume()
        refreshUserListsData()
    }

    private fun setupUserListsView() {
        findViewById<View>(R.id.btnCreateList).setOnClickListener { showCreateListDialog() }
        val recycler = findViewById<RecyclerView>(R.id.recyclerUserLists)
        recycler.layoutManager = LinearLayoutManager(this)
        listAdapter = UserListAdapter(emptyList()) { openListDetail(it) }
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

    private fun setupSearchLogic() {
        val btnSearch = findViewById<View>(R.id.btnSearchOtherLists)
        val cardSearch = findViewById<View>(R.id.cardSearchLists)
        val editSearch = findViewById<EditText>(R.id.editSearchLists)
        val btnClear = findViewById<View>(R.id.btnClearSearch)

        btnSearch.setOnClickListener {
            if (cardSearch.visibility == View.VISIBLE) {
                cardSearch.visibility = View.GONE
                editSearch.setText("")
                // Al cerrar, restauramos la lista completa
                vexoAdapter.updateLists(officialVexoLists)
            } else {
                cardSearch.visibility = View.VISIBLE
                editSearch.requestFocus()
            }
        }

        editSearch.addTextChangedListener { text ->
            val query = text.toString().lowercase().trim()
            val filtered = if (query.isEmpty()) {
                officialVexoLists
            } else {
                officialVexoLists.filter { 
                    it.name.lowercase().contains(query) || it.description.lowercase().contains(query) 
                }
            }
            vexoAdapter.updateLists(filtered)
            btnClear.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
        }

        btnClear.setOnClickListener { editSearch.setText("") }
    }

    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayoutLists)
        val containerUser = findViewById<View>(R.id.containerMisColecciones)
        val containerVexo = findViewById<View>(R.id.containerOtrasListas)
        val btnCreate = findViewById<View>(R.id.btnCreateList)
        val btnSearch = findViewById<View>(R.id.btnSearchOtherLists)
        val cardSearch = findViewById<View>(R.id.cardSearchLists)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 0) {
                    containerUser.visibility = View.VISIBLE
                    containerVexo.visibility = View.GONE
                    btnCreate.visibility = View.VISIBLE
                    btnSearch.visibility = View.GONE
                    cardSearch.visibility = View.GONE
                } else {
                    containerUser.visibility = View.GONE
                    containerVexo.visibility = View.VISIBLE
                    btnCreate.visibility = View.GONE
                    btnSearch.visibility = View.VISIBLE
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
        val input = EditText(this).apply { hint = "Nombre" }
        AlertDialog.Builder(this).setTitle("Nueva Lista").setView(input)
            .setPositiveButton("Crear") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) { 
                    watchlistRepository.createUserList(name)
                    refreshUserListsData() 
                }
            }.setNegativeButton("Cancelar", null).show()
    }
}

class UserListAdapter(
    private var lists: List<UserList>,
    private val onListClick: (UserList) -> Unit
) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.textListName)
        val description: TextView = v.findViewById(R.id.textListDescription)
        val count: TextView = v.findViewById(R.id.textMovieCount)
        val img1: ImageView = v.findViewById(R.id.imgPreview1)
        val img2: ImageView = v.findViewById(R.id.imgPreview2)
        val img3: ImageView = v.findViewById(R.id.imgPreview3)
        val img4: ImageView = v.findViewById(R.id.imgPreview4)
        val textMore: TextView = v.findViewById(R.id.textMoreMovies)
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_user_list, p, false))

    override fun onBindViewHolder(h: ViewHolder, p: Int) {
        val l = lists[p]
        val title = if (l.name.length > 25) l.name.take(25) + "..." else l.name
        h.name.text = title.uppercase()
        h.description.text = l.description
        h.description.visibility = if (!l.description.isNullOrEmpty()) View.VISIBLE else View.GONE
        h.count.text = "${l.movies.size} ELEMENTOS"

        val imgs = listOf(h.img1, h.img2, h.img3, h.img4)
        imgs.forEach { it.visibility = View.GONE; it.setPadding(0, 0, 0, 0); it.imageTintList = null }
        h.textMore.visibility = View.GONE
        
        if (l.movies.isEmpty()) {
            h.img1.visibility = View.VISIBLE
            h.img1.setImageResource(R.drawable.ic_nav_profile)
            h.img1.setPadding(12, 12, 12, 12)
            h.img1.imageTintList = android.content.res.ColorStateList.valueOf(h.itemView.context.getColor(R.color.text_secondary))
        } else {
            l.movies.take(4).forEachIndexed { i, m ->
                imgs[i].visibility = View.VISIBLE
                Glide.with(h.itemView.context).load(m.posterPath).centerCrop().into(imgs[i])
            }
        }
        if (l.movies.size > 4) { h.textMore.visibility = View.VISIBLE; h.textMore.text = "+${l.movies.size-4}" }
        h.itemView.setOnClickListener { onListClick(l) }
    }

    override fun getItemCount() = lists.size
    fun updateLists(n: List<UserList>) { lists = n; notifyDataSetChanged() }
}
