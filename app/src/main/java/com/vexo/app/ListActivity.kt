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
            "Explora la élite del séptimo arte con nuestra selección de las 250 mejores películas de todos los tiempos. Desde los clásicos imperecederos que definieron géneros hasta las obras maestras contemporáneas que desafían nuestra percepción. Una lista imprescindible para cualquier cinéfilo que se precie, curada meticulosamente basándose en la calidad narrativa, técnica e impacto cultural. ¿Cuántas de estas joyas has tachado ya de tu lista? Es hora de completar tu cultura cinematográfica con los títulos que hicieron historia y que han dejado una huella imborrable en el corazón de millones de personas alrededor del mundo. ¡Prepara las palomitas y sumérgete en lo mejor del cine!", 
            R.drawable.vexo_logo,
            "250 PELÍCULAS",
            250,
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
            "Bienvenido a la era dorada de la televisión. Esta lista reúne las 250 mejores series y producciones televisivas de la historia. Sumérgete en tramas complejas, personajes inolvidables y mundos que te mantendrán pegado a la pantalla durante horas. Desde dramas criminales intensos hasta comedias que definieron una época, aquí encontrarás lo mejor de lo mejor en formato episódico. Prepárate para tu próximo gran maratón y descubre por qué estas historias han cautivado a millones de espectadores, elevando el formato de las series a un nivel artístico nunca antes visto. ¡La televisión nunca volverá a ser la misma para ti!", 
            R.drawable.vexo_logo,
            "250 SERIES",
            250,
            listOf(
                "https://image.tmdb.org/t/p/w500/ztkUQvBZ77Z7iB1u66NuJvSTN7h.jpg",
                "https://image.tmdb.org/t/p/w500/7WsyChvLEz79BMo33owrR7Z9XnS.jpg",
                "https://image.tmdb.org/t/p/w500/reksS7S7S7S7S7S7S7S.jpg",
                "https://image.tmdb.org/t/p/w500/69Uqt7vSbeFwb1L3rsLbt64H64o.jpg"
            )
        ),
        VexoList(
            "marvel_universe",
            "MARVEL",
            "Prepárate para la experiencia superheroica definitiva. Desde el humilde nacimiento de Iron Man hasta las épicas batallas cósmicas por el destino del multiverso. Esta lista contiene todas las películas y series del Universo Cinematográfico de Marvel (MCU), organizadas para que no te pierdas ni un solo detalle de la Saga del Infinito y la Saga del Multiverso. ¡Únete a los Vengadores, explora reinos desconocidos y vive la acción más espectacular del cine contemporáneo! Siente el poder de las gemas del infinito y la valentía de tus héroes favoritos en esta travesía sin igual que ha redefinido el cine de acción.",
            R.drawable.vexo_logo,
            "SAGA MULTIVERSAL",
            75,
            listOf(
                "https://image.tmdb.org/t/p/w500/7WsyChvLEz79BMo33owrR7Z9XnS.jpg",
                "https://image.tmdb.org/t/p/w500/RYMX2wcG6MAmJhSTqXv4vTckqG.jpg",
                "https://image.tmdb.org/t/p/w500/or06vSfvSu12BqG9pCNCtzpNU1M.jpg",
                "https://image.tmdb.org/t/p/w500/8Gxv8S7HaU0STqyzSjN9769K1oP.jpg"
            )
        ),
        VexoList(
            "star_wars_universe",
            "STAR WARS",
            "Hace mucho tiempo, en una galaxia muy, muy lejana... La leyenda de los Skywalker, las crónicas de los cazarrecompensas y los secretos milenarios de la Fuerza se reúnen en esta colección exhaustiva. Revive la trilogía original que lo cambió todo, las precuelas que expandieron el mito, las secuelas y todas las series que exploran los rincones más oscuros y brillantes de este universo infinito. Ya seas un Caballero Jedi o un Lord Sith, esta es la lista definitiva que estabas buscando. ¡Que la Fuerza te acompañe en este viaje estelar a través de las estrellas y el tiempo!",
            R.drawable.vexo_logo,
            "SAGA GALÁCTICA",
            35,
            listOf(
                "https://image.tmdb.org/t/p/w500/6FfCt3Svxn5Hms0M19pZ9oq9fsW.jpg", // A New Hope
                "https://image.tmdb.org/t/p/w500/e9n27asTMYef20XmB604oInODbp.jpg", // Mandalorian
                "https://image.tmdb.org/t/p/w500/mS91N96mAs3l4C7Z894m955f1Tz.jpg", // Revenge of the Sith
                "https://image.tmdb.org/t/p/w500/y9X7678X96N06mAt86f4H8v0X.jpg"  // Ahsoka
            )
        ),
        VexoList(
            "harry_potter",
            "HARRY POTTER",
            "Sumérgete en el universo cinematográfico más mágico de todos los tiempos. Desde el primer viaje en el Expreso de Hogwarts hasta la épica batalla final contra el que no debe ser nombrado, pasando por las asombrosas criaturas de Newt Scamander. Revive la historia que cautivó a generaciones con esta colección definitiva que incluye las 8 películas originales de Harry Potter y la trilogía completa de Animales Fantásticos. ¡Prepara tu varita, ponte tu túnica y deja que la magia te envuelva en este maratón inolvidable que te hará creer en lo imposible una vez más! La magia te espera detrás de cada rincón de Hogwarts.",
            R.drawable.vexo_logo,
            "11 PELÍCULAS",
            11,
            listOf(
                "https://image.tmdb.org/t/p/w500/wuMc08IPisT7Y9PqXn97Xn3BbsC.jpg", // Philosopher's Stone
                "https://image.tmdb.org/t/p/w500/6v9qnMVsYAAU0pve99U8S9moX6r.jpg", // Deathly Hallows 2
                "https://image.tmdb.org/t/p/w500/7WsyChvLEz79BMo33owrR7Z9XnS.jpg", // Fantastic Beasts 1
                "https://image.tmdb.org/t/p/w500/A8NoO23S4e6GZk2f55737RIdpA3.jpg"  // Secrets of Dumbledore
            )
        ),
        VexoList(
            "john_wick_universe",
            "JOHN WICK UNIVERSE",
            "Entra en el submundo criminal donde las reglas lo son todo y la elegancia se mezcla con la violencia más pura. Sigue la odisea de John Wick, el hombre que una vez fue el asesino más temido y que ahora busca venganza y redención. Esta colección incluye la tetralogía completa que redefinió el cine de acción moderno y la serie 'The Continental', que explora los orígenes del hotel que sirve de santuario para los asesinos más peligrosos del mundo. ¡Prepárate para la acción táctica definitiva!",
            R.drawable.vexo_logo,
            "5 ELEMENTOS",
            5,
            listOf(
                "https://image.tmdb.org/t/p/w500/867S1EBZg99mno8pWo9srIq669G.jpg", // Wick 1
                "https://image.tmdb.org/t/p/w500/hXp97fBfVp6GvSREfI0O5pX4vB3.jpg", // Wick 2
                "https://image.tmdb.org/t/p/w500/ziEuG1SXiE0rS646ioA6Kyv31Yp.jpg", // Wick 3
                "https://image.tmdb.org/t/p/w500/892v9pX5f2f5S5G87G9vGf56zP1.jpg"  // Wick 4
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
        recyclerVexo.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 2)
        vexoAdapter = VexoListAdapter(officialVexoLists) { item ->
            val intent = Intent(this, VexoListDetailActivity::class.java)
            intent.putExtra("listId", item.id)
            intent.putExtra("listName", item.name)
            intent.putExtra("listDesc", item.description)
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
                val posterUrl = "https://image.tmdb.org/t/p/w500${m.posterPath}"
                Glide.with(h.itemView.context).load(posterUrl).centerCrop().into(imgs[i])
            }
        }
        if (l.movies.size > 4) { h.textMore.visibility = View.VISIBLE; h.textMore.text = "+${l.movies.size-4}" }
        h.itemView.setOnClickListener { onListClick(l) }
    }

    override fun getItemCount() = lists.size
    fun updateLists(n: List<UserList>) { lists = n; notifyDataSetChanged() }
}
