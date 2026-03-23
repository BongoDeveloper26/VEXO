package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import data.model.Movie
import data.repository.WatchlistRepository
import ui.detail.DetailActivity

class ProfileFragment : Fragment() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var imgSlots: List<ImageView>
    private lateinit var cardSlots: List<MaterialCardView>
    private lateinit var activityAdapter: RecentActivityAdapter
    private lateinit var diaryAdapter: DiaryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        watchlistRepository = WatchlistRepository(requireContext())

        setupUI(view)
        loadVitrina()
        loadRecentActivity(view)
        loadDiary(view)
        updateStats(view)
        
        return view
    }

    override fun onResume() {
        super.onResume()
        loadVitrina()
        view?.let {
            loadRecentActivity(it)
            loadDiary(it)
            updateStats(it)
        }
    }

    private fun setupUI(view: View) {
        val btnLogout: Button = view.findViewById(R.id.btnLogout)
        btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Cerrando sesión...", Toast.LENGTH_SHORT).show()
        }

        // Botón Ver todas las valoraciones
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

    private fun updateStats(view: View) {
        val stats = watchlistRepository.getStats()
        view.findViewById<TextView>(R.id.textMoviesCount).text = stats.totalMovies.toString()
        view.findViewById<TextView>(R.id.textAverageRating).text = String.format("%.1f", stats.averageRating)
    }

    private fun loadRecentActivity(view: View) {
        // Corregido: Usamos la función disponible en el repositorio
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
                val movie = watchlistRepository.getAllRatedMovies().find { it.id == entry.movieId }
                if (movie != null) {
                    val intent = Intent(requireContext(), DetailActivity::class.java)
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(), "No se pudo abrir la película", Toast.LENGTH_SHORT).show()
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
                Glide.with(this).load(movie.posterPath).centerCrop().into(imgView)
                imgView.imageTintList = null
            } else {
                imgView.setImageResource(android.R.drawable.ic_input_add)
                val padding = (40 * resources.displayMetrics.density).toInt()
                imgView.setPadding(padding, padding, padding, padding)
                imgView.imageTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.primary))
            }
        }
    }

    private fun showMoviePickerDialog(slotIndex: Int) {
        val favorites = watchlistRepository.getUserLists().find { it.name == WatchlistRepository.FAVORITES_LIST_NAME }?.movies ?: emptyList()
        if (favorites.isEmpty()) {
            Toast.makeText(requireContext(), "Añade primero alguna película a 'Mis Favoritos' para ponerla en tu vitrina", Toast.LENGTH_LONG).show()
            return
        }
        val options = favorites.map { it.title }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Selecciona para tu Vitrina")
            .setItems(options) { _, which ->
                watchlistRepository.setVitrinaMovie(slotIndex, favorites[which])
                loadVitrina()
            }.show()
    }
}
