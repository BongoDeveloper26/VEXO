package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.tabs.TabLayout
import data.model.UserList
import data.repository.WatchlistRepository

class ListFragment : Fragment() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var listAdapter: UserListAdapter
    private lateinit var vexoListAdapter: VexoListAdapter

    private val officialLists = listOf(
        VexoList(
            id = "top_250_movies",
            name = "LAS 250 MEJORES PELÍCULAS",
            description = "Explora la élite del séptimo arte con nuestra selección de las 250 mejores películas de todos los tiempos. Desde los clásicos imperecederos que definieron géneros hasta las obras maestras contemporáneas que desafían nuestra percepción. Una lista imprescindible para cualquier cinéfilo que se precie, curada meticulosamente basándose en la calidad narrativa, técnica e impacto cultural. ¿Cuántas de estas joyas has tachado ya de tu lista? Es hora de completar tu cultura cinematográfica con los títulos que hicieron historia y que todo amante del cine debería ver al menos una vez en la vida.",
            imageRes = R.drawable.vexo_logo,
            countText = "250 PELÍCULAS",
            totalItems = 250,
            previewPosters = listOf(
                "https://image.tmdb.org/t/p/w500/q6y0Go1tsYmUuAtfj6KyB30OXvN.jpg",
                "https://image.tmdb.org/t/p/w500/3bhkrjSTWv4ayisdqAs6arW0Lja.jpg",
                "https://image.tmdb.org/t/p/w500/8S9NoP10n5S5G87G9vGf56zP167.jpg",
                "https://image.tmdb.org/t/p/w500/v9970pP2XF8X8Z6n1P2pS5X8X8Z.jpg"
            )
        ),
        VexoList(
            id = "top_250_tv",
            name = "LAS 250 MEJORES SERIES",
            description = "Bienvenido a la era dorada de la televisión. Esta lista reúne las 250 mejores series y producciones televisivas de la historia. Sumérgete en tramas complejas, personajes inolvidables y mundos que te mantendrán pegado a la pantalla durante horas. Desde dramas criminales intensos hasta comedias que definieron una época, aquí encontrarás lo mejor de lo mejor en formato episódico. Prepárate para tu próximo gran maratón y descubre por qué estas historias han cautivado a millones de espectadores en todo el mundo, elevando el formato televisivo a una forma de arte sin precedentes.",
            imageRes = R.drawable.vexo_logo,
            countText = "250 SERIES",
            totalItems = 250,
            previewPosters = listOf(
                "https://image.tmdb.org/t/p/w500/ztkUQvBZ77Z7iB1u66NuJvSTN7h.jpg",
                "https://image.tmdb.org/t/p/w500/7WsyChvLEz79BMo33owrR7Z9XnS.jpg",
                "https://image.tmdb.org/t/p/w500/reksS7S7S7S7S7S7S7S7S.jpg",
                "https://image.tmdb.org/t/p/w500/69Uqt7vSbeFwb1L3rsLbt64H64o.jpg"
            )
        ),
        VexoList(
            id = "marvel_universe",
            name = "MARVEL",
            description = "Prepárate para la experiencia superheroica definitiva. Desde el humilde nacimiento de Iron Man hasta las épicas batallas cósmicas por el destino del multiverso. Esta lista contiene todas las películas y series del Universo Cinematográfico de Marvel (MCU), organizadas para que no te pierdas ni un solo detalle de la Saga del Infinito y la Saga del Multiverso. ¡Únete a los Vengadores, explora reinos desconocidos y vive la acción más espectacular del cine contemporáneo en una cronología que ha redefinido el concepto de franquicia cinematográfica!",
            imageRes = R.drawable.vexo_logo,
            countText = "SAGA MULTIVERSAL",
            totalItems = 75,
            previewPosters = listOf(
                "https://image.tmdb.org/t/p/w500/7WsyChvLEz79BMo33owrR7Z9XnS.jpg", // Avengers
                "https://image.tmdb.org/t/p/w500/RYMX2wcG6MAmJhSTqXv4vTckqG.jpg", // Infinity War
                "https://image.tmdb.org/t/p/w500/or06vSfvSu12BqG9pCNCtzpNU1M.jpg", // Endgame
                "https://image.tmdb.org/t/p/w500/8Gxv8S7HaU0STqyzSjN9769K1oP.jpg"  // Iron Man
            )
        ),
        VexoList(
            id = "star_wars_universe",
            name = "STAR WARS",
            description = "Hace mucho tiempo, en una galaxia muy, muy lejana... La leyenda de los Skywalker, las crónicas de los cazarrecompensas y los secretos milenarios de la Fuerza se reúnen en esta colección exhaustiva. Revive la trilogía original que lo cambió todo, las precuelas que expandieron el mito, las secuelas y todas las series que exploran los rincones más oscuros y brillantes de este universo infinito. Ya seas un Caballero Jedi o un Lord Sith, esta es la lista definitiva que estabas buscando. ¡Que la Fuerza te acompañe en este viaje estelar a través del tiempo y el espacio!",
            imageRes = R.drawable.vexo_logo,
            countText = "SAGA GALÁCTICA",
            totalItems = 35,
            previewPosters = listOf(
                "https://image.tmdb.org/t/p/w500/6FfCt3Svxn5Hms0M19pZ9oq9fsW.jpg", // A New Hope
                "https://image.tmdb.org/t/p/w500/e9n27asTMYef20XmB604oInODbp.jpg", // Mandalorian
                "https://image.tmdb.org/t/p/w500/mS91N96mAs3l4C7Z894m955f1Tz.jpg", // Revenge of the Sith
                "https://image.tmdb.org/t/p/w500/y9X7678X96N06mAt86f4H8v0X.jpg"  // Ahsoka
            )
        ),
        VexoList(
            id = "harry_potter",
            name = "HARRY POTTER",
            description = "Sumérgete en el universo cinematográfico más mágico de todos los tiempos. Desde el primer viaje en el Expreso de Hogwarts hasta la épica batalla final contra el que no debe ser nombrado, pasando por las asombrosas criaturas de Newt Scamander. Revive la historia que cautivó a generaciones con esta colección definitiva que incluye las 8 películas originales de Harry Potter y la trilogía completa de Animales Fantásticos. ¡Prepara tu varita, ponte tu túnica y deja que la magia te envuelva en este maratón inolvidable que te hará creer en lo imposible una vez más!",
            imageRes = R.drawable.vexo_logo,
            countText = "11 PELÍCULAS",
            totalItems = 11,
            previewPosters = listOf(
                "https://image.tmdb.org/t/p/w500/wuMc08IPisT7Y9PqXn97Xn3BbsC.jpg", // Philosopher's Stone
                "https://image.tmdb.org/t/p/w500/6v9qnMVsYAAU0pve99U8S9moX6r.jpg", // Deathly Hallows 2
                "https://image.tmdb.org/t/p/w500/7WsyChvLEz79BMo33owrR7Z9XnS.jpg", // Fantastic Beasts 1
                "https://image.tmdb.org/t/p/w500/A8NoO23S4e6GZk2f55737RIdpA3.jpg"  // Secrets of Dumbledore
            )
        ),
        VexoList(
            id = "john_wick_universe",
            name = "JOHN WICK UNIVERSE",
            description = "Entra en el submundo criminal donde las reglas lo son todo y la elegancia se mezcla con la violencia más pura. Sigue la odisea de John Wick, el hombre que una vez fue el asesino más temido y que ahora busca venganza y redención. Esta colección incluye la tetralogía completa que redefinió el cine de acción moderno y la serie 'The Continental', que explora los orígenes del hotel que sirve de santuario para los asesinos más peligrosos del mundo. ¡Prepárate para la acción táctica definitiva!",
            imageRes = R.drawable.vexo_logo,
            countText = "5 ELEMENTOS",
            totalItems = 5,
            previewPosters = listOf(
                "https://image.tmdb.org/t/p/w500/867S1EBZg99mno8pWo9srIq669G.jpg", // Wick 1
                "https://image.tmdb.org/t/p/w500/hXp97fBfVp6GvSREfI0O5pX4vB3.jpg", // Wick 2
                "https://image.tmdb.org/t/p/w500/ziEuG1SXiE0rS646ioA6Kyv31Yp.jpg", // Wick 3
                "https://image.tmdb.org/t/p/w500/892v9pX5f2f5S5G87G9vGf56zP1.jpg"  // Wick 4
            )
        )
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        watchlistRepository = WatchlistRepository(requireContext())
        setupUI(view)
        setupVexoLists(view)
        setupSearchLogic(view)
    }

    override fun onResume() {
        super.onResume()
        refreshLists()
    }

    private fun setupUI(view: View) {
        view.findViewById<View>(R.id.btnCreateList).setOnClickListener { showCreateListDialog() }

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerUserLists)
        listAdapter = UserListAdapter(emptyList(), 
            onListClick = { userList -> openListDetail(userList) }
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = listAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutLists)
        val containerMisColecciones = view.findViewById<View>(R.id.containerMisColecciones)
        val containerOtrasListas = view.findViewById<View>(R.id.containerOtrasListas)
        val btnCreate = view.findViewById<View>(R.id.btnCreateList)
        val btnSearch = view.findViewById<View>(R.id.btnSearchOtherLists)
        val cardSearch = view.findViewById<View>(R.id.cardSearchLists)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        containerMisColecciones.visibility = View.VISIBLE
                        containerOtrasListas.visibility = View.GONE
                        btnCreate.visibility = View.VISIBLE
                        btnSearch.visibility = View.GONE
                        cardSearch.visibility = View.GONE
                    }
                    1 -> {
                        containerMisColecciones.visibility = View.GONE
                        containerOtrasListas.visibility = View.VISIBLE
                        btnCreate.visibility = View.GONE
                        btnSearch.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        
        refreshLists()
    }

    private fun setupVexoLists(view: View) {
        val recyclerVexo = view.findViewById<RecyclerView>(R.id.recyclerVexoLists)
        
        vexoListAdapter = VexoListAdapter(officialLists) { vexolist ->
            val intent = Intent(requireContext(), VexoListDetailActivity::class.java)
            intent.putExtra("listId", vexolist.id)
            intent.putExtra("listName", vexolist.name)
            intent.putExtra("listDesc", vexolist.description)
            startActivity(intent)
        }

        recyclerVexo.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerVexo.adapter = vexoListAdapter
    }

    private fun setupSearchLogic(view: View) {
        val btnSearch = view.findViewById<View>(R.id.btnSearchOtherLists)
        val cardSearch = view.findViewById<View>(R.id.cardSearchLists)
        val editSearch = view.findViewById<EditText>(R.id.editSearchLists)
        val btnClear = view.findViewById<View>(R.id.btnClearSearch)

        btnSearch.setOnClickListener {
            if (cardSearch.visibility == View.VISIBLE) {
                cardSearch.visibility = View.GONE
                editSearch.setText("")
                vexoListAdapter.updateLists(officialLists)
            } else {
                cardSearch.visibility = View.VISIBLE
                editSearch.requestFocus()
            }
        }

        editSearch.addTextChangedListener { text ->
            val query = text.toString().lowercase().trim()
            val filtered = if (query.isEmpty()) {
                officialLists
            } else {
                officialLists.filter { 
                    it.name.lowercase().contains(query) || it.description.lowercase().contains(query) 
                }
            }
            vexoListAdapter.updateLists(filtered)
            btnClear.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
        }

        btnClear.setOnClickListener { editSearch.setText("") }
    }

    private fun showCreateListDialog() {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_create_list, null)
        
        val editName = view.findViewById<EditText>(R.id.editListName)
        val editDescription = view.findViewById<EditText>(R.id.editListDescription)
        val switchPublic = view.findViewById<SwitchMaterial>(R.id.switchPublic)
        val textHint = view.findViewById<TextView>(R.id.textPublicHint)
        val btnCreate = view.findViewById<MaterialButton>(R.id.btnConfirmCreate)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelCreate)

        switchPublic.isChecked = false
        switchPublic.text = "Lista Privada"
        textHint.text = "Solo tú puedes ver esta lista."

        switchPublic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchPublic.text = "Lista Pública"
                textHint.text = "Otros usuarios podrán ver esta lista en tu perfil."
            } else {
                switchPublic.text = "Lista Privada"
                textHint.text = "Solo tú puedes ver esta lista."
            }
        }

        btnCreate.setOnClickListener {
            val name = editName.text.toString().trim()
            val description = editDescription.text.toString().trim()
            val isPublic = switchPublic.isChecked
            if (name.isNotEmpty()) {
                watchlistRepository.createUserList(
                    name, 
                    if (description.isEmpty()) null else description,
                    isPublic
                )
                refreshLists()
                bottomSheet.dismiss()
            } else {
                editName.error = "Escribe un nombre"
            }
        }

        btnCancel.setOnClickListener { bottomSheet.dismiss() }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun refreshLists() {
        if (!isAdded) return
        val lists = watchlistRepository.getUserLists()
        view?.findViewById<View>(R.id.layoutEmptyCollections)?.visibility = if (lists.isEmpty()) View.VISIBLE else View.GONE
        listAdapter.updateLists(lists)
    }

    private fun openListDetail(userList: UserList) {
        val intent = Intent(requireContext(), UserListDetailActivity::class.java)
        intent.putExtra("listId", userList.id)
        intent.putExtra("listName", userList.name)
        startActivity(intent)
    }
}
