package com.vexo.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import data.model.Movie
import data.repository.WatchlistRepository
import ui.detail.DetailActivity

class ProfileFragment : Fragment() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var imgSlots: List<ImageView>
    private lateinit var cardSlots: List<MaterialCardView>
    private lateinit var activityAdapter: RecentActivityAdapter
    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var imgProfile: ImageView
    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView

    private val pickImageLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: Exception) {}
                watchlistRepository.setProfileImageUri(it.toString())
                loadProfileImage()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        watchlistRepository = WatchlistRepository(requireContext())
        auth = FirebaseAuth.getInstance()

        setupUI(view)
        loadProfileImage()
        loadUserName()
        loadUserEmail()
        loadVitrina()
        loadRecentActivity(view)
        loadDiary(view)
        updateStats(view)
        
        return view
    }

    override fun onResume() {
        super.onResume()
        loadProfileImage()
        loadUserName()
        loadUserEmail()
        loadVitrina()
        view?.let {
            loadRecentActivity(it)
            loadDiary(it)
            updateStats(it)
        }
    }

    private fun setupUI(view: View) {
        imgProfile = view.findViewById(R.id.imgProfile)
        textUserName = view.findViewById(R.id.textUserNameProfile)
        textUserEmail = view.findViewById(R.id.textUserEmailProfile)
        val cardProfileImage: MaterialCardView = view.findViewById(R.id.cardProfileImage)
        val btnEditName: ImageButton = view.findViewById(R.id.btnEditName)
        
        cardProfileImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnEditName.setOnClickListener {
            showEditNameDialog()
        }

        val btnLogout: Button = view.findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        view.findViewById<TextView>(R.id.btnViewAllRated).setOnClickListener {
            startActivity(Intent(requireContext(), AllRatedMoviesActivity::class.java))
        }

        view.findViewById<TextView>(R.id.btnViewAllDiary).setOnClickListener {
            startActivity(Intent(requireContext(), DiaryActivity::class.java))
        }

        imgSlots = listOf(
            view.findViewById(R.id.imgSlot1),
            view.findViewById(R.id.imgSlot2),
            view.findViewById(R.id.imgSlot3),
            view.findViewById(R.id.imgSlot4)
        )

        cardSlots = listOf(
            view.findViewById(R.id.slot1),
            view.findViewById(R.id.slot2),
            view.findViewById(R.id.slot3),
            view.findViewById(R.id.slot4)
        )

        cardSlots.forEachIndexed { index, card ->
            card.setOnClickListener {
                val movie = watchlistRepository.getVitrinaMovies()[index]
                if (movie != null) {
                    showMovieOptionsBottomSheet(index, movie)
                } else {
                    showMoviePickerDialog(index)
                }
            }
        }

        val recyclerActivity = view.findViewById<RecyclerView>(R.id.recyclerRecentActivity)
        recyclerActivity.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val recyclerDiary = view.findViewById<RecyclerView>(R.id.recyclerDiary)
        recyclerDiary.layoutManager = LinearLayoutManager(requireContext())
        recyclerDiary.isNestedScrollingEnabled = false
    }

    private fun loadProfileImage() {
        val uriString = watchlistRepository.getProfileImageUri()
        if (uriString != null) {
            imgProfile.setPadding(0, 0, 0, 0)
            imgProfile.imageTintList = null
            imgProfile.alpha = 1.0f
            Glide.with(this)
                .load(Uri.parse(uriString))
                .centerCrop()
                .placeholder(R.drawable.ic_nav_profile)
                .into(imgProfile)
        }
    }

    private fun loadUserName() {
        textUserName.text = watchlistRepository.getUserName()
    }

    private fun loadUserEmail() {
        val user = auth.currentUser
        textUserEmail.text = user?.email ?: getString(R.string.no_email_linked)
    }

    private fun showEditNameDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Cambiar nombre")
        
        val input = EditText(requireContext())
        input.filters = arrayOf(InputFilter.LengthFilter(15))
        input.setText(textUserName.text.toString())
        input.setSelection(input.text.length)
        input.isSingleLine = true
        
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val container = LinearLayout(requireContext())
        val margin = (24 * resources.displayMetrics.density).toInt()
        lp.setMargins(margin, 0, margin, 0)
        input.layoutParams = lp
        container.addView(input)
        
        builder.setView(container)
        
        builder.setPositiveButton("Guardar") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                // 1. Guardar localmente
                watchlistRepository.setUserName(newName)
                textUserName.text = newName
                
                // 2. Sincronizar con Firebase Auth para que persista al cambiar de pantalla
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                
                user?.updateProfile(profileUpdates)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Nombre actualizado en la nube", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error al sincronizar con Firebase", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun updateStats(view: View) {
        val stats = watchlistRepository.getStats()
        view.findViewById<TextView>(R.id.textMoviesCount).text = stats.totalMovies.toString()
        view.findViewById<TextView>(R.id.textAverageRating).text = String.format("%.1f", stats.averageRating)
    }

    private fun loadRecentActivity(view: View) {
        val recentMovies = watchlistRepository.getAllRatedMovies().take(5)
        val layoutActivity = view.findViewById<View>(R.id.layoutActivity)
        
        if (recentMovies.isNotEmpty()) {
            layoutActivity.visibility = View.VISIBLE
            activityAdapter = RecentActivityAdapter(recentMovies, watchlistRepository) { movie ->
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("movie", movie)
                startActivity(intent)
            }
            view.findViewById<RecyclerView>(R.id.recyclerRecentActivity).adapter = activityAdapter
        } else {
            layoutActivity.visibility = View.GONE
        }
    }

    private fun loadDiary(view: View) {
        val diaryEntries = watchlistRepository.getDiary().take(3)
        val layoutDiary = view.findViewById<View>(R.id.layoutDiary)

        if (diaryEntries.isNotEmpty()) {
            layoutDiary.visibility = View.VISIBLE

            diaryAdapter = DiaryAdapter(diaryEntries) { entry ->
                val movieToOpen = entry.movie ?: watchlistRepository.getAllRatedMovies().find { it.id == entry.movieId }
                
                if (movieToOpen != null) {
                    val intent = Intent(requireContext(), DetailActivity::class.java)
                    intent.putExtra("movie", movieToOpen)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "No se pudo abrir la ficha", Toast.LENGTH_SHORT).show()
                }
            }

            view.findViewById<RecyclerView>(R.id.recyclerDiary).adapter = diaryAdapter
        } else {
            layoutDiary.visibility = View.GONE
        }
    }

    private fun showMovieOptionsBottomSheet(slotIndex: Int, movie: Movie) {
        val dialog = BottomSheetDialog(requireContext())
        val menuView = layoutInflater.inflate(R.layout.layout_vitrina_menu, null)
        
        menuView.findViewById<TextView>(R.id.vitrinaMenuTitle).text = movie.title

        menuView.findViewById<View>(R.id.btnVitrinaDetails).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }

        menuView.findViewById<View>(R.id.btnVitrinaRemove).setOnClickListener {
            dialog.dismiss()
            watchlistRepository.setVitrinaMovie(slotIndex, null)
            loadVitrina()
            Toast.makeText(requireContext(), "Eliminada de la vitrina", Toast.LENGTH_SHORT).show()
        }

        dialog.setContentView(menuView)
        dialog.show()
    }

    private fun loadVitrina() {
        val vitrina = watchlistRepository.getVitrinaMovies()
        vitrina.forEachIndexed { index, movie ->
            val imgView = imgSlots[index]
            if (movie != null) {
                imgView.setPadding(0, 0, 0, 0)
                imgView.alpha = 1.0f
                imgView.imageTintList = null
                Glide.with(this).load(movie.posterPath).centerCrop().into(imgView)
            } else {
                imgView.setImageDrawable(null)
                imgView.alpha = 1.0f
                imgView.setPadding(0, 0, 0, 0)
            }
        }
    }

    private fun showMoviePickerDialog(slotIndex: Int) {
        val favorites = watchlistRepository.getUserLists().find { it.name == WatchlistRepository.FAVORITES_LIST_NAME }?.movies ?: emptyList()
        if (favorites.isEmpty()) {
            Toast.makeText(requireContext(), "Añade películas a favoritos primero", Toast.LENGTH_SHORT).show()
            return
        }
        val options = favorites.map { it.title }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Selecciona para tu Vitrina")
            .setItems(options) { _, which ->
                watchlistRepository.setVitrinaMovie(slotIndex, favorites[which])
                loadVitrina()
            }.show()
    }
}
