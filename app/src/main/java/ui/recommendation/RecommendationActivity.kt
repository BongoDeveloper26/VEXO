package ui.recommendation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vexo.app.R
import com.vexo.app.databinding.ActivityRecommendationBinding
import data.model.Movie
import ui.detail.DetailActivity
import java.util.Locale

class RecommendationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecommendationBinding
    private val viewModel: RecommendationViewModel by viewModels()
    private var isAnimating = false

    private val genresMap = mapOf(
        "Acción" to 28,
        "Aventura" to 12,
        "Animación" to 16,
        "Comedia" to 35,
        "Crimen" to 80,
        "Documental" to 99,
        "Drama" to 18,
        "Familia" to 10751,
        "Fantasía" to 14,
        "Historia" to 36,
        "Terror" to 27,
        "Música" to 10402,
        "Misterio" to 9648,
        "Romance" to 10749,
        "Ciencia ficción" to 878,
        "Suspense" to 53,
        "Bélica" to 10752,
        "Western" to 37
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnFilter.setOnClickListener {
            showGenreFilterDialog()
        }

        binding.btnLike.setOnClickListener {
            if (isAnimating) return@setOnClickListener
            val currentMovie = getCurrentMovie()
            if (currentMovie != null) {
                animateSwipe(true) {
                    viewModel.addMovieToDiscoveryList(currentMovie)
                }
            }
        }

        binding.btnDislike.setOnClickListener {
            if (isAnimating) return@setOnClickListener
            animateSwipe(false) {
                viewModel.nextMovie()
            }
        }

        binding.cardMovie.setOnClickListener {
            val currentMovie = getCurrentMovie()
            if (currentMovie != null) {
                val intent = Intent(this, DetailActivity::class.java)
                intent.putExtra("movie", currentMovie)
                startActivity(intent)
            }
        }
    }

    private fun showGenreFilterDialog() {
        val genresNames = genresMap.keys.toTypedArray()
        val checkedItems = BooleanArray(genresNames.size) { index ->
            viewModel.selectedGenres.value?.contains(genresMap[genresNames[index]]) == true
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Filtrar por Géneros")
            .setMultiChoiceItems(genresNames, checkedItems) { _, which, isChecked ->
                checkedItems[which] = isChecked
            }
            .setPositiveButton("Aplicar") { _, _ ->
                val selectedIds = mutableListOf<Int>()
                for (i in checkedItems.indices) {
                    if (checkedItems[i]) {
                        genresMap[genresNames[i]]?.let { selectedIds.add(it) }
                    }
                }
                viewModel.setGenres(selectedIds)
            }
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Limpiar") { _, _ ->
                viewModel.setGenres(emptyList())
            }
            .show()
    }

    private fun observeViewModel() {
        viewModel.movies.observe(this) { movies ->
            if (movies.isNotEmpty() && !isAnimating) {
                updateMovieUI(movies[viewModel.currentMovieIndex.value ?: 0])
            } else if (movies.isEmpty() && !isAnimating) {
                showEmptyState()
            }
        }

        viewModel.currentMovieIndex.observe(this) { index ->
            val movies = viewModel.movies.value
            if (movies != null && index < movies.size) {
                updateMovieUI(movies[index])
            }
        }

        viewModel.isLoadingRatings.observe(this) { isLoading ->
            if (isLoading) {
                binding.textIMDBRating.visibility = View.GONE
                binding.textRottenRating.visibility = View.GONE
            }
        }

        viewModel.currentOMDbData.observe(this) { data ->
            if (data != null) {
                if (!data.imdbRating.isNullOrEmpty() && data.imdbRating != "N/A") {
                    binding.textIMDBRating.text = "IMDb ${data.imdbRating}"
                    showRatingWithAnimation(binding.textIMDBRating)
                }
                val rtRating = data.Ratings?.find { it.Source == "Rotten Tomatoes" }?.Value
                if (!rtRating.isNullOrEmpty() && rtRating != "N/A") {
                    binding.textRottenRating.text = "RT $rtRating"
                    showRatingWithAnimation(binding.textRottenRating)
                }
            }
        }
    }

    private fun showEmptyState() {
        binding.textMovieTitle.text = "No hay resultados"
        binding.textMovieDescription.text = "Prueba a cambiar los filtros de género."
        binding.imgPoster.setImageResource(R.drawable.logo_vexo_app)
        binding.imgPoster.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
        binding.textTMDBRating.text = ""
        binding.textIMDBRating.visibility = View.GONE
        binding.textRottenRating.visibility = View.GONE
    }

    private fun showRatingWithAnimation(view: View) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.translationY = 10f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun updateMovieUI(movie: Movie) {
        binding.textMovieTitle.text = movie.title
        binding.textMovieDescription.text = movie.overview
        binding.textTMDBRating.text = String.format(Locale.getDefault(), "TMDB %.1f", movie.rating)

        binding.cardMovie.translationX = 0f
        binding.cardMovie.rotation = 0f
        binding.cardMovie.scaleX = 0.9f
        binding.cardMovie.scaleY = 0.9f
        binding.cardMovie.alpha = 0f
        
        binding.textIMDBRating.visibility = View.GONE
        binding.textRottenRating.visibility = View.GONE

        binding.imgPoster.scaleType = android.widget.ImageView.ScaleType.CENTER_CROP

        Glide.with(this)
            .load(movie.posterPath)
            .placeholder(R.drawable.logo_vexo_app)
            .error(R.drawable.logo_vexo_app)
            .into(binding.imgPoster)

        binding.cardMovie.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun getCurrentMovie(): Movie? {
        val movies = viewModel.movies.value
        val index = viewModel.currentMovieIndex.value ?: 0
        return if (movies != null && index < movies.size) movies[index] else null
    }

    private fun animateSwipe(isLike: Boolean, onComplete: () -> Unit) {
        isAnimating = true
        val endX = if (isLike) 1200f else -1200f
        val rotation = if (isLike) 30f else -30f
        
        binding.cardMovie.animate()
            .translationX(endX)
            .rotation(rotation)
            .alpha(0f)
            .setDuration(400)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                onComplete()
                isAnimating = false
            }
            .start()
    }
}