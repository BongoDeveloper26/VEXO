package ui.recommendation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import com.vexo.app.R
import com.vexo.app.databinding.ActivityRecommendationBinding
import data.model.Movie
import ui.detail.DetailActivity
import java.util.Locale

class RecommendationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecommendationBinding
    private val viewModel: RecommendationViewModel by viewModels()
    private var isAnimating = false

    private val allGenresMap = mapOf(
        28 to "Acción", 12 to "Aventura", 16 to "Animación",
        35 to "Comedia", 80 to "Crimen", 99 to "Doc",
        18 to "Drama", 10751 to "Familiar", 14 to "Fantasía",
        36 to "Historia", 27 to "Terror", 10402 to "Música",
        9648 to "Misterio", 10749 to "Romance", 878 to "Ciencia Ficción",
        53 to "Suspense", 10752 to "Bélica", 37 to "Western"
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

        binding.btnFilter.visibility = View.VISIBLE
        binding.btnFilter.setOnClickListener {
            showAdvancedFilters()
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

    private fun showAdvancedFilters() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_discover_filters, null)
        
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupGenres)
        val sliderYear = view.findViewById<RangeSlider>(R.id.sliderYear)
        val sliderRating = view.findViewById<Slider>(R.id.sliderRating)
        val textYear = view.findViewById<TextView>(R.id.textYearValue)
        val textRating = view.findViewById<TextView>(R.id.textRatingValue)
        val toggleType = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleContentType)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilters)

        allGenresMap.forEach { (id, name) ->
            val chip = Chip(this).apply {
                text = name
                isCheckable = true
                tag = id
                setChipBackgroundColorResource(R.color.background_app)
            }
            chipGroup.addView(chip)
        }

        sliderYear.addOnChangeListener { slider, _, _ -> 
            val values = slider.values
            textYear.text = "${values[0].toInt()} - ${values[1].toInt()}"
        }

        sliderRating.addOnChangeListener { _, value, _ -> 
            textRating.text = String.format("%.1f", value)
        }

        btnApply.setOnClickListener {
            val selectedGenres = mutableListOf<Int>()
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as Chip
                if (chip.isChecked) {
                    selectedGenres.add(chip.tag as Int)
                }
            }

            val yearValues = sliderYear.values
            viewModel.loadWithFilters(
                selectedGenres, 
                yearValues[0].toInt(), 
                yearValues[1].toInt(), 
                sliderRating.value, 
                toggleType.checkedButtonId == R.id.btnTypeTV
            )
            
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.movies.observe(this) { movies ->
            if (movies != null && movies.isNotEmpty() && !isAnimating) {
                updateMovieUI(movies[viewModel.currentMovieIndex.value ?: 0])
            } else if (movies != null && movies.isEmpty() && !isAnimating) {
                showEmptyState()
            }
        }

        viewModel.currentMovieIndex.observe(this) { index ->
            val movies = viewModel.movies.value
            if (movies != null && index < movies.size) {
                updateMovieUI(movies[index])
            } else if (movies != null && index >= movies.size && movies.isNotEmpty()) {
                showEmptyState()
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
        binding.textMovieDescription.text = "Prueba con otros filtros arriba a la derecha."
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
