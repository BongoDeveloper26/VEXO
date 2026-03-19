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
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import data.model.UserList
import data.repository.WatchlistRepository

class ListFragment : Fragment() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var listAdapter: UserListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        watchlistRepository = WatchlistRepository(requireContext())
        setupUI(view)
    }

    override fun onResume() {
        super.onResume()
        refreshLists()
    }

    private fun setupUI(view: View) {
        // En el fragmento principal, ocultamos el botón de volver
        view.findViewById<ImageButton>(R.id.btnBackList).visibility = View.GONE
        
        view.findViewById<View>(R.id.btnCreateList).setOnClickListener { showCreateListDialog() }

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerUserLists)
        listAdapter = UserListAdapter(emptyList(), 
            onListClick = { userList -> openListDetail(userList) },
            onDeleteClick = { userList -> showDeleteConfirmDialog(userList) }
        )
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = listAdapter
        
        refreshLists()
    }

    private fun showCreateListDialog() {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_create_list, null)
        
        val editName = view.findViewById<EditText>(R.id.editListName)
        val btnCreate = view.findViewById<MaterialButton>(R.id.btnConfirmCreate)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelCreate)

        btnCreate.setOnClickListener {
            val name = editName.text.toString().trim()
            if (name.isNotEmpty()) {
                watchlistRepository.createUserList(name)
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

    private fun showDeleteConfirmDialog(userList: UserList) {
        AlertDialog.Builder(requireContext())
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
