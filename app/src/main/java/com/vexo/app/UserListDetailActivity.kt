package com.vexo.app

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.bumptech.glide.Glide

class UserListDetailActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var movieAdapter: MovieAdapter
    private var userListId: String? = null
    private var isLiked = false

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

        val imgProfile = findViewById<ImageView>(R.id.imgCreatorProfile)
        val profileUri = watchlistRepository.getProfileImageUri()
        
        if (!profileUri.isNullOrEmpty()) {
            imgProfile.setPadding(0, 0, 0, 0)
            Glide.with(this)
                .load(profileUri)
                .circleCrop()
                .into(imgProfile)
        } else {
            imgProfile.setImageResource(R.drawable.ic_nav_profile)
            imgProfile.setPadding(15, 15, 15, 15)
            imgProfile.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
        }
        
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

        findViewById<ImageButton>(R.id.btnMoreOptions).setOnClickListener {
            showModernOptionsSheet()
        }

        // Lógica del botón de Like
        val btnLike = findViewById<View>(R.id.btnLikeList)
        btnLike.setOnClickListener {
            toggleLike()
        }
    }

    private fun toggleLike() {
        val lists = watchlistRepository.getUserLists()
        val currentList = lists.find { it.id == userListId } ?: return
        
        // Cambiamos el estado
        isLiked = !isLiked
        
        // Actualizamos el contador acumulable
        if (isLiked) {
            currentList.likes += 1
        } else {
            if (currentList.likes > 0) currentList.likes -= 1
        }
        
        // Guardamos de forma persistente:
        // 1. El nuevo total de likes de la lista
        watchlistRepository.updateUserListLikes(currentList.id, currentList.likes)
        // 2. Que ESTE usuario ya ha dado like a ESTA lista (para que no pueda repetir)
        watchlistRepository.setListLiked(currentList.id, isLiked)
        
        updateLikeUI(currentList.likes)
    }

    private fun updateLikeUI(likes: Int) {
        val imgLike = findViewById<ImageView>(R.id.imgLike)
        val textLikes = findViewById<TextView>(R.id.textLikeCount)
        
        textLikes.text = likes.toString()
        
        if (isLiked) {
            imgLike.setImageResource(R.drawable.ic_heart_filled)
            imgLike.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
            textLikes.setTextColor(getColor(R.color.primary))
        } else {
            imgLike.setImageResource(R.drawable.ic_heart_outline)
            imgLike.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
            textLikes.setTextColor(getColor(R.color.text_secondary))
        }
    }

    private fun showModernOptionsSheet() {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_list_options_menu, null)

        view.findViewById<View>(R.id.optionShare).setOnClickListener {
            bottomSheet.dismiss()
            shareList()
        }

        view.findViewById<View>(R.id.optionEdit).setOnClickListener {
            bottomSheet.dismiss()
            showEditListDialog()
        }

        view.findViewById<View>(R.id.optionDelete).setOnClickListener {
            bottomSheet.dismiss()
            showDeleteConfirmDialog()
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
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
            
            // Mostrar si es pública o privada
            val infoText = if (currentList.isPublic) "Lista Pública • ${currentList.movies.size} películas" 
                           else "Lista Privada • ${currentList.movies.size} películas"
            findViewById<TextView>(R.id.textListInfo).text = infoText
            
            // IMPORTANTE: Cargar si el usuario ya le dio like anteriormente
            isLiked = watchlistRepository.isListLiked(currentList.id)
            updateLikeUI(currentList.likes)
            
            val movies = currentList.movies
            findViewById<View>(R.id.layoutEmptyUserList).visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
            movieAdapter.updateMovies(movies)
        } else {
            finish()
        }
    }

    private fun shareList() {
        val lists = watchlistRepository.getUserLists()
        val currentList = lists.find { it.id == userListId } ?: return
        
        val listLink = "vexo://list/${currentList.id}"
        val shareContent = "🍿 ¡Mira mi lista \"${currentList.name}\" en VEXO!\n📊 Contiene ${currentList.movies.size} películas\n🔗 $listLink"
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareContent)
        }
        startActivity(Intent.createChooser(intent, "Compartir lista"))
    }

    private fun showEditListDialog() {
        val lists = watchlistRepository.getUserLists()
        val currentList = lists.find { it.id == userListId } ?: return

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_list, null)
        val editName = view.findViewById<EditText>(R.id.editListName)
        val editDesc = view.findViewById<EditText>(R.id.editListDescription)
        val switchPublic = view.findViewById<SwitchMaterial>(R.id.switchPublic)
        val textHint = view.findViewById<TextView>(R.id.textPublicHint)

        editName.setText(currentList.name)
        editDesc.setText(currentList.description ?: "")
        
        switchPublic.isChecked = currentList.isPublic
        updateSwitchUI(switchPublic, textHint, currentList.isPublic)

        switchPublic.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchUI(switchPublic, textHint, isChecked)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar Lista")
            .setView(view)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = editName.text.toString().trim()
                val newDesc = editDesc.text.toString().trim()
                val isPublic = switchPublic.isChecked
                
                if (newName.isNotEmpty()) {
                    watchlistRepository.updateUserList(
                        currentList.id, 
                        newName, 
                        if (newDesc.isEmpty()) null else newDesc,
                        isPublic
                    )
                    refreshListData()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateSwitchUI(switch: SwitchMaterial, hint: TextView, isPublic: Boolean) {
        if (isPublic) {
            switch.text = "Lista Pública"
            hint.text = "Otros usuarios podrán ver esta lista en tu perfil."
        } else {
            switch.text = "Lista Privada"
            hint.text = "Solo tú puedes ver esta lista."
        }
    }

    private fun showDeleteConfirmDialog() {
        val lists = watchlistRepository.getUserLists()
        val currentList = lists.find { it.id == userListId } ?: return

        MaterialAlertDialogBuilder(this)
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
