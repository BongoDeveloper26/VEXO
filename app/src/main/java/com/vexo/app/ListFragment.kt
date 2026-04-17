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
            description = "La selección oficial de Vexo con las obras maestras del cine.",
            imageRes = R.drawable.vexo_logo,
            countText = "250 ELEMENTOS",
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
            description = "El ranking definitivo de Vexo con las mejores producciones de TV.",
            imageRes = R.drawable.vexo_logo,
            countText = "250 ELEMENTOS",
            previewPosters = listOf(
                "https://image.tmdb.org/t/p/w500/ggm8bbv9v0f15c10v9f9f15c10v.jpg",
                "https://image.tmdb.org/t/p/w500/7WsyChvLEz79BMo33owrR7Z9XnS.jpg",
                "https://image.tmdb.org/t/p/w500/reksS7S7S7S7S7S7S7S7S7S7S.jpg",
                "https://image.tmdb.org/t/p/w500/y6y6y6y6y6y6y6y6y6y6y6y6y.jpg"
            )
        ),
        VexoList(
            id = "marvel_universe",
            name = "MARVEL",
            description = "Todas las películas y series del Universo Cinematográfico de Marvel Studios.",
            imageRes = R.drawable.vexo_logo,
            countText = "MCU COMPLETO",
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
            description = "Toda la saga galáctica: películas, series y animaciones de una galaxia muy, muy lejana.",
            imageRes = R.drawable.vexo_logo,
            countText = "SAGA COMPLETA",
            previewPosters = listOf(
                "https://image.tmdb.org/t/p/w500/6FfCt3Svxn5Hms0M19pZ9oq9fsW.jpg", // A New Hope
                "https://image.tmdb.org/t/p/w500/e9n27asTMYef20XmB604oInODbp.jpg", // Mandalorian
                "https://image.tmdb.org/t/p/w500/mS91N96mAs3l4C7Z894m955f1Tz.jpg", // Revenge of the Sith
                "https://image.tmdb.org/t/p/w500/y9X7678X96N06mAt86f4H8v0X.jpg"  // Ahsoka
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
