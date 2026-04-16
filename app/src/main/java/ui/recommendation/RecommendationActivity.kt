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
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.LabelFormatter
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

    private val providersMap = mapOf(
        8 to "Netflix",
        119 to "Prime Video",
        337 to "Disney+",
        350 to "Apple TV+",
        384 to "HBO Max",
        149 to "Movistar+",
        63 to "Filmin",
        2 to "Apple TV",
        35 to "Rakuten TV",
        11 to "MUBI",
        283 to "Crunchyroll",
        541 to "Atresplayer",
        543 to "RTVE Play"
    )

    private val keywordsMap = mapOf(
        9715 to "Superhéroes",
        9672 to "Hechos reales",
        9717 to "Venganza",
        6075 to "Deportes",
        9663 to "Slasher",
        9748 to "Asesinos",
        4565 to "Distopía",
        1009 to "Zombies",
        10084 to "Atracos",
        1612 to "Espionaje"
    )

    private val countriesMap = mapOf(
        "US" to "Estados Unidos",
        "ES" to "España",
        "JP" to "Japón",
        "KR" to "Corea del Sur",
        "GB" to "Reino Unido",
        "FR" to "Francia",
        "IT" to "Italia",
        "DE" to "Alemania",
        "MX" to "México",
        "IN" to "India"
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
        
        val chipGroupGenres = view.findViewById<ChipGroup>(R.id.chipGroupGenres)
        val chipGroupProviders = view.findViewById<ChipGroup>(R.id.chipGroupProviders)
        val chipGroupKeywords = view.findViewById<ChipGroup>(R.id.chipGroupKeywords)
        val chipGroupCountries = view.findViewById<ChipGroup>(R.id.chipGroupCountries)
        val sliderYear = view.findViewById<RangeSlider>(R.id.sliderYear)
        val sliderDuration = view.findViewById<RangeSlider>(R.id.sliderDuration)
        val sliderRating = view.findViewById<Slider>(R.id.sliderRating)
        
        val textYear = view.findViewById<TextView>(R.id.textYearValue)
        val textDuration = view.findViewById<TextView>(R.id.textDurationValue)
        val textRating = view.findViewById<TextView>(R.id.textRatingValue)
        val toggleType = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleContentType)
        val layoutDuration = view.findViewById<View>(R.id.layoutDurationFilter)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilters)

        val setupChip: (Chip, Any) -> Unit = { chip, id ->
            chip.isCheckable = true
            chip.tag = id
            chip.setChipBackgroundColorResource(R.color.toggle_bg_selector)
            chip.setTextColor(ContextCompat.getColorStateList(this, R.color.toggle_text_selector))
            chip.setChipStrokeColorResource(R.color.primary)
            chip.chipStrokeWidth = 1f
            chip.isCheckedIconVisible = false
        }

        countriesMap.forEach { (code, name) ->
            val chip = Chip(this).apply { text = name }
            setupChip(chip, code)
            chipGroupCountries.addView(chip)
        }

        allGenresMap.forEach { (id, name) ->
            val chip = Chip(this).apply { text = name }
            setupChip(chip, id)
            chipGroupGenres.addView(chip)
        }

        providersMap.forEach { (id, name) ->
            val chip = Chip(this).apply { text = name }
            setupChip(chip, id)
            chipGroupProviders.addView(chip)
        }

        keywordsMap.forEach { (id, name) ->
            val chip = Chip(this).apply { text = name }
            setupChip(chip, id)
            chipGroupKeywords.addView(chip)
        }

        // --- VALORES INICIALES ---
        val initialYearValues = sliderYear.values
        textYear.text = "${initialYearValues[0].toInt()} - ${initialYearValues[1].toInt()}"
        
        val initialDurationValues = sliderDuration.values
        textDuration.text = "${initialDurationValues[0].toInt()} - ${initialDurationValues[1].toInt()} min"
        
        textRating.text = String.format("%.1f", sliderRating.value)

        // --- LÓGICA DINÁMICA DE DURACIÓN ---
        toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                layoutDuration.visibility = if (checkedId == R.id.btnTypeMovie) View.VISIBLE else View.GONE
            }
        }
        // Estado inicial
        layoutDuration.visibility = if (toggleType.checkedButtonId == R.id.btnTypeMovie) View.VISIBLE else View.GONE

        // --- LISTENERS ---
        sliderYear.setLabelFormatter { value -> value.toInt().toString() }
        sliderYear.addOnChangeListener { slider, _, _ -> 
            val values = slider.values
            textYear.text = "${values[0].toInt()} - ${values[1].toInt()}"
        }

        sliderDuration.setLabelFormatter { value -> "${value.toInt()} min" }
        sliderDuration.addOnChangeListener { slider, _, _ -> 
            val values = slider.values
            textDuration.text = "${values[0].toInt()} - ${values[1].toInt()} min"
        }

        sliderRating.addOnChangeListener { _, value, _ -> 
            textRating.text = String.format("%.1f", value)
        }

        btnApply.setOnClickListener {
            val selectedGenres = getCheckedIds<Int>(chipGroupGenres)
            val selectedProviders = getCheckedIds<Int>(chipGroupProviders)
            val selectedKeywords = getCheckedIds<Int>(chipGroupKeywords)
            val selectedCountry = getCheckedIds<String>(chipGroupCountries).firstOrNull()

            val yearValues = sliderYear.values
            val durationValues = sliderDuration.values
            val isTv = toggleType.checkedButtonId == R.id.btnTypeTV
            
            viewModel.loadWithFilters(
                if (selectedGenres.isEmpty()) null else selectedGenres, 
                yearValues[0].toInt(), 
                yearValues[1].toInt(), 
                sliderRating.value, 
                if (selectedProviders.isEmpty()) null else selectedProviders,
                if (selectedKeywords.isEmpty()) null else selectedKeywords,
                isTv,
                selectedCountry,
                if (isTv) null else durationValues[0].toInt(),
                if (isTv) null else durationValues[1].toInt()
            )
            
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun <T> getCheckedIds(chipGroup: ChipGroup): List<T> {
        val ids = mutableListOf<T>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as Chip
            if (chip.isChecked) {
                @Suppress("UNCHECKED_CAST")
                ids.add(chip.tag as T)
            }
        }
        return ids
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
