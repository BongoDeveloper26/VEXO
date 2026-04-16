package ui.detail

import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vexo.app.R
import com.vexo.app.AchievementsActivity
import com.vexo.app.Achievement
import data.model.Movie
import data.model.UserList
import data.repository.TMDBRepository
import data.repository.WatchlistRepository
import data.repository.WatchProviderItem
import data.repository.OMDbRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.explore.MovieHorizontalAdapter
import ui.genre.GenreActivity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private val omdbRepository = OMDbRepository.getInstance()
    private lateinit var watchlistRepository: WatchlistRepository
    private var currentMovie: Movie? = null
    private var totalSeasons: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        watchlistRepository = WatchlistRepository(this)
        currentMovie = intent.getParcelableExtra<Movie>("movie")

        currentMovie?.let { movie ->
            setupInitialViews(movie)
            setupTabs()
            setupWatchlistButton(movie)
            setupTopMenu(movie)
            updateStatusIcons(movie.id)
            loadUserReview(movie.id)
            
            if (movie.isTvShow) {
                loadFullTVDetails(movie.id)
                loadTVCredits(movie.id)
                loadTVRecommendations(movie.id)
                loadTVTrailers(movie.id)
                
                findViewById<View>(R.id.cardSeasonsHighlight).setOnClickListener {
                    val intent = Intent(this, TVSeasonsActivity::class.java)
                    intent.putExtra("seriesId", movie.id)
                    intent.putExtra("seriesName", movie.title)
                    intent.putExtra("totalSeasons", totalSeasons)
                    startActivity(intent)
                }
            } else {
                loadFullMovieDetails(movie.id)
                loadMovieCredits(movie.id)
                loadMovieRecommendations(movie.id)
                loadSagaIfAvailable(movie.id)
                loadMovieTrailers(movie.id)
            }
        } ?: finish()
    }

    private fun setupInitialViews(movie: Movie) {
        val imgPoster: ImageView = findViewById(R.id.imgPosterDetail)
        val textTitle: TextView = findViewById(R.id.textTitleDetail)
        val textRating: TextView = findViewById(R.id.textRatingDetail)
        val textOverview: TextView = findViewById(R.id.textOverviewDetail)
        val btnBack: ImageButton = findViewById(R.id.btnBackDetail)

        textTitle.text = movie.title
        textRating.text = getString(R.string.rating_format, movie.rating)
        textOverview.text = movie.overview

        btnBack.setOnClickListener { finish() }

        Glide.with(this)
            .load(movie.backdropPath ?: movie.posterPath)
            .into(imgPoster)

        imgPoster.setOnClickListener { showFullPoster(movie) }
        
        // Animación elegante de entrada
        textTitle.alpha = 0f
        textTitle.translationY = 30f
        val tagline = findViewById<View>(R.id.textTagline)
        
        tagline.alpha = 0f
        tagline.animate().alpha(0.85f).translationY(0f).setDuration(800).setStartDelay(100).start()
        textTitle.animate().alpha(1f).translationY(0f).setDuration(800).setStartDelay(250).start()
    }

    private fun updateStatusIcons(movieId: Int) {
        findViewById<ImageView>(R.id.statusHeart)?.let {
            it.visibility = if (watchlistRepository.isFavorite(movieId)) View.VISIBLE else View.GONE
            it.setImageResource(R.drawable.ic_heart_filled)
        }
        
        findViewById<ImageView>(R.id.statusWatched)?.let {
            it.visibility = if (watchlistRepository.isWatched(movieId)) View.VISIBLE else View.GONE
            it.setImageResource(R.drawable.ic_watched_modern)
        }
    }

    private fun loadUserReview(movieId: Int) {
        val entry = watchlistRepository.getDiary().find { it.movieId == movieId }
        val layoutReview = findViewById<View>(R.id.layoutUserReviewDetail)
        val textReview = findViewById<TextView>(R.id.textUserReviewValue)
        
        if (entry != null && (entry.rating > 0 || !entry.review.isNullOrEmpty())) {
            layoutReview.visibility = View.VISIBLE
            textReview.text = if (!entry.review.isNullOrEmpty()) "${entry.review}" else getString(R.string.rated_content)
            
            // Actualizar las estrellas dentro de la tarjeta de reseña
            val reviewStars = listOf<ImageView>(
                findViewById(R.id.revStar1), findViewById(R.id.revStar2),
                findViewById(R.id.revStar3), findViewById(R.id.revStar4),
                findViewById(R.id.revStar5)
            )
            
            reviewStars.forEachIndexed { index, imageView ->
                if (index < entry.rating) {
                    imageView.setImageResource(R.drawable.ic_star_active)
                    imageView.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
                    imageView.alpha = 1.0f
                } else {
                    imageView.setImageResource(R.drawable.ic_star_border)
                    imageView.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
                    imageView.alpha = 0.3f
                }
            }
        } else {
            layoutReview.visibility = View.GONE
        }
    }

    private fun updatePegiUI(pegi: String) {
        val pegiTextView = findViewById<TextView>(R.id.textPegiDetail) ?: return
        val dotView = findViewById<View>(R.id.dotPegi)
        
        dotView?.visibility = View.VISIBLE
        pegiTextView.visibility = View.VISIBLE
        
        val cleanPegi = pegi.replace("+", "").replace("-", "").uppercase().trim()
        
        val color = when {
            cleanPegi == "L" || cleanPegi == "3" || cleanPegi == "7" || cleanPegi == "G" || cleanPegi == "PG" -> "#4CAF50" // Verde
            cleanPegi == "12" || cleanPegi == "PG13" -> "#FFC107" // Amarillo
            cleanPegi == "16" -> "#FF9800" // Naranja
            cleanPegi == "18" || cleanPegi == "R" || cleanPegi == "NC17" -> "#F44336" // Rojo
            else -> "#FF5252"
        }
        
        pegiTextView.text = getString(R.string.pegi_label, pegi)
        pegiTextView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    private fun loadFullMovieDetails(movieId: Int) {
        lifecycleScope.launch {
            val details = repository.getMovieDetails(movieId)
            if (details != null) {
                currentMovie = currentMovie?.copy(releaseDate = details.release_date)
                val year = details.release_date.take(4)
                findViewById<TextView>(R.id.textYearDetail)?.text = year
                findViewById<TextView>(R.id.textRuntimeDetail)?.text = getString(R.string.runtime_format, details.runtime)
                findViewById<TextView>(R.id.textTagline)?.text = details.tagline ?: ""
                findViewById<View>(R.id.cardSeasonsHighlight)?.visibility = View.GONE
                
                val pegi = details.release_dates?.results?.find { it.iso_3166_1 == "ES" }?.release_dates?.firstOrNull { it.certification.isNotEmpty() }?.certification
                    ?: details.release_dates?.results?.find { it.iso_3166_1 == "US" }?.release_dates?.firstOrNull { it.certification.isNotEmpty() }?.certification
                
                if (!pegi.isNullOrEmpty()) {
                    updatePegiUI(pegi)
                }

                if (!details.imdb_id.isNullOrEmpty()) {
                    loadExternalRatingsAndAwards(details.imdb_id, true, null)
                } else {
                    loadExternalRatingsAndAwards(details.title, false, year)
                }

                setupGenres(details.genres)
                
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                if (details.budget > 0) {
                    findViewById<View>(R.id.layoutBudgetData)?.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.textBudget)?.text = currencyFormat.format(details.budget)
                }
                if (details.revenue > 0) {
                    findViewById<View>(R.id.layoutRevenueData)?.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.textRevenue)?.text = currencyFormat.format(details.revenue)
                }
                
                findViewById<TextView>(R.id.textStatus)?.text = details.status
                findViewById<TextView>(R.id.textReleaseDateFull)?.text = details.release_date
                findViewById<TextView>(R.id.textOriginalTitle)?.text = details.original_title
                findViewById<TextView>(R.id.textOriginalLanguage)?.text = details.original_language.uppercase()
                findViewById<TextView>(R.id.textProductionCountries)?.text = details.production_countries.joinToString(", ") { it.name }
                findViewById<TextView>(R.id.textProductionCompanies)?.text = details.production_companies.joinToString(", ") { it.name }

                val providersFlat = details.watchProviders?.results?.get("ES")?.flatrate ?: emptyList()
                val providersBuy = details.watchProviders?.results?.get("ES")?.buy ?: emptyList()
                val providersRent = details.watchProviders?.results?.get("ES")?.rent ?: emptyList()
                val allProviders = (providersFlat + providersBuy + providersRent).distinctBy { it.provider_id }

                if (allProviders.isNotEmpty()) {
                    val layoutProviders = findViewById<View>(R.id.layoutWatchProviders)
                    layoutProviders.visibility = View.VISIBLE
                    val recycler: RecyclerView = findViewById(R.id.recyclerWatchProviders)
                    recycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    recycler.adapter = WatchProviderAdapter(allProviders)
                    
                    layoutProviders.setOnClickListener {
                        val intent = Intent(this@DetailActivity, WatchProvidersActivity::class.java)
                        intent.putParcelableArrayListExtra("providers", ArrayList(allProviders))
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun loadFullTVDetails(tvId: Int) {
        lifecycleScope.launch {
            val details = repository.getTVDetails(tvId)
            if (details != null) {
                totalSeasons = details.number_of_seasons
                currentMovie = currentMovie?.copy(releaseDate = details.first_air_date)
                val year = details.first_air_date.take(4)
                findViewById<TextView>(R.id.textYearDetail)?.text = year
                findViewById<TextView>(R.id.textTagline)?.text = details.tagline ?: ""
                
                findViewById<TextView>(R.id.textRuntimeDetail)?.visibility = View.GONE
                findViewById<View>(R.id.cardSeasonsHighlight)?.apply {
                    visibility = View.VISIBLE
                    findViewById<TextView>(R.id.textSeasonsValue).text = details.number_of_seasons.toString()
                    findViewById<TextView>(R.id.textEpisodesValue).text = details.number_of_episodes.toString()
                }
                
                val pegi = details.content_ratings?.results?.find { it.iso_3166_1 == "ES" }?.rating
                    ?: details.content_ratings?.results?.find { it.iso_3166_1 == "US" }?.rating
                
                if (!pegi.isNullOrEmpty()) {
                    updatePegiUI(pegi)
                }

                findViewById<View>(R.id.layoutBudgetData)?.visibility = View.GONE
                findViewById<View>(R.id.layoutRevenueData)?.visibility = View.GONE
                
                val imdbId = details.external_ids?.imdb_id
                if (!imdbId.isNullOrEmpty()) {
                    loadExternalRatingsAndAwards(imdbId, true, null)
                } else {
                    loadExternalRatingsAndAwards(details.original_name, false, year)
                }

                setupGenres(details.genres)
                
                findViewById<TextView>(R.id.textStatus)?.text = details.status
                findViewById<TextView>(R.id.textReleaseDateFull)?.text = details.first_air_date
                findViewById<TextView>(R.id.textOriginalTitle)?.text = details.original_name
                findViewById<TextView>(R.id.textOriginalLanguage)?.text = details.original_language.uppercase()
                findViewById<TextView>(R.id.textProductionCountries)?.text = details.production_countries.joinToString(", ") { it.name }
                findViewById<TextView>(R.id.textProductionCompanies)?.text = details.production_companies.joinToString(", ") { it.name }

                val providersFlat = details.watchProviders?.results?.get("ES")?.flatrate ?: emptyList()
                val providersBuy = details.watchProviders?.results?.get("ES")?.buy ?: emptyList()
                val providersRent = details.watchProviders?.results?.get("ES")?.rent ?: emptyList()
                val allProviders = (providersFlat + providersBuy + providersRent).distinctBy { it.provider_id }

                if (allProviders.isNotEmpty()) {
                    val layoutProviders = findViewById<View>(R.id.layoutWatchProviders)
                    layoutProviders.visibility = View.VISIBLE
                    val recycler: RecyclerView = findViewById(R.id.recyclerWatchProviders)
                    recycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    recycler.adapter = WatchProviderAdapter(allProviders)
                    
                    layoutProviders.setOnClickListener {
                        val intent = Intent(this@DetailActivity, WatchProvidersActivity::class.java)
                        intent.putParcelableArrayListExtra("providers", ArrayList(allProviders))
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun setupGenres(genres: List<data.repository.GenreDTO>) {
        val groupGenres: ChipGroup = findViewById(R.id.chipGroupGenresTab)
        groupGenres.removeAllViews()
        genres.forEach { genre ->
            val chip = Chip(this).apply {
                text = genre.name
                setTextColor(getColor(android.R.color.white))
                setChipBackgroundColorResource(R.color.primary)
                setOnClickListener {
                    val intent = Intent(this@DetailActivity, GenreActivity::class.java)
                    intent.putExtra("genreId", genre.id)
                    intent.putExtra("genreName", genre.name)
                    startActivity(intent)
                }
            }
            groupGenres.addView(chip)
        }
    }

    private fun loadMovieCredits(movieId: Int) {
        lifecycleScope.launch {
            val credits = repository.getMovieCredits(movieId)
            if (credits != null) {
                setupCreditsUI(credits.cast, credits.crew)
            }
        }
    }

    private fun loadTVCredits(tvId: Int) {
        lifecycleScope.launch {
            val credits = repository.getTVCredits(tvId)
            if (credits != null) {
                setupCreditsUI(credits.cast, credits.crew)
            }
        }
    }

    private fun setupCreditsUI(cast: List<data.repository.CastDTO>, crew: List<data.repository.CrewDTO>) {
        val recyclerCast: RecyclerView = findViewById(R.id.recyclerCastTab)
        recyclerCast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerCast.adapter = CastAdapter(cast) { id, name -> 
            val intent = Intent(this, PersonMoviesActivity::class.java)
            intent.putExtra("personId", id)
            intent.putExtra("personName", name)
            startActivity(intent)
        }

        val containerCrew: LinearLayout = findViewById(R.id.containerCrewTab)
        containerCrew.removeAllViews()
        crew.filter { it.job in listOf("Director", "Writer", "Producer", "Executive Producer") }.forEach { member ->
            val view = LayoutInflater.from(this).inflate(R.layout.item_director, containerCrew, false)
            view.findViewById<TextView>(R.id.textDirectorName).text = member.name
            view.findViewById<TextView>(R.id.textCrewRole).text = member.job
            Glide.with(this).load("https://image.tmdb.org/t/p/w185${member.profile_path}").into(view.findViewById(R.id.imgDirector))
            view.setOnClickListener { 
                val intent = Intent(this, PersonMoviesActivity::class.java)
                intent.putExtra("personId", member.id)
                intent.putExtra("personName", member.name)
                startActivity(intent)
            }
            containerCrew.addView(view)
        }
    }

    private fun loadMovieRecommendations(movieId: Int) {
        lifecycleScope.launch {
            val recs = repository.getMovieRecommendations(movieId)
            setupRecommendationsUI(recs)
        }
    }

    private fun loadTVRecommendations(tvId: Int) {
        lifecycleScope.launch {
            val recs = repository.getTVRecommendations(tvId)
            setupRecommendationsUI(recs)
        }
    }

    private fun setupRecommendationsUI(recs: List<Movie>) {
        if (recs.isNotEmpty()) {
            val recycler: RecyclerView = findViewById(R.id.recyclerSimilarMovies)
            recycler.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recycler.adapter = MovieHorizontalAdapter(recs).apply {
                onItemClick = { movie ->
                    val intent = Intent(this@DetailActivity, DetailActivity::class.java)
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun setupTopMenu(movie: Movie) {
        val btnMenu: ImageButton = findViewById(R.id.btnMenuDetail)

        btnMenu.setOnClickListener {
            val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = layoutInflater.inflate(R.layout.layout_movie_menu, null)
            
            val textMovieName = view.findViewById<TextView>(R.id.menuMovieName)
            val textRatingDate = view.findViewById<TextView>(R.id.menuRatingDate)
            val stars = listOf<ImageView>(
                view.findViewById(R.id.menuStar1), view.findViewById(R.id.menuStar2),
                view.findViewById(R.id.menuStar3), view.findViewById(R.id.menuStar4),
                view.findViewById(R.id.menuStar5)
            )
            val imgHeartQuick: ImageView = view.findViewById(R.id.imgMenuHeartQuick)
            
            // Traer el nombre y la fecha (En español)
            textMovieName.text = movie.title
            val isSpanish = repository.getLanguage() == "es-ES"
            val locale = if (isSpanish) Locale("es", "ES") else Locale("en", "US")
            val currentDate = SimpleDateFormat("d 'de' MMMM, yyyy", locale).format(Date())
            textRatingDate.text = currentDate

            var currentRating = watchlistRepository.getMovieRating(movie.id)
            var isFav = watchlistRepository.isFavorite(movie.id)
            val isWatched = watchlistRepository.isWatched(movie.id)
            val isInVitrina = watchlistRepository.isMovieInVitrina(movie.id)

            // --- LÓGICA DE VALORACIÓN RÁPIDA CON ANIMACIONES ---
            fun updateMenuStarsUI(rating: Float, animated: Boolean = false) {
                stars.forEachIndexed { index, img ->
                    val isActive = index < rating
                    if (animated) {
                        img.animate()
                            .scaleX(if (isActive) 1.3f else 0.8f)
                            .scaleY(if (isActive) 1.3f else 0.8f)
                            .setDuration(120)
                            .setStartDelay(index * 20L)
                            .withEndAction {
                                if (isActive) {
                                    img.setImageResource(R.drawable.ic_star_active)
                                    img.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
                                } else {
                                    img.setImageResource(R.drawable.ic_star_border)
                                    img.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
                                }
                                img.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setInterpolator(OvershootInterpolator(2f))
                                    .setDuration(250)
                                    .setStartDelay(0)
                                    .start()
                            }
                            .start()
                    } else {
                        if (isActive) {
                            img.setImageResource(R.drawable.ic_star_active)
                            img.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
                        } else {
                            img.setImageResource(R.drawable.ic_star_border)
                            img.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
                        }
                    }
                }
            }

            fun updateMenuHeartUI(animated: Boolean = false) {
                if (isFav) {
                    imgHeartQuick.setImageResource(R.drawable.ic_heart_filled)
                    imgHeartQuick.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
                    if (animated) {
                        imgHeartQuick.scaleX = 0.7f
                        imgHeartQuick.scaleY = 0.7f
                        imgHeartQuick.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction {
                            imgHeartQuick.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(OvershootInterpolator()).setDuration(200).start()
                        }.start()
                    }
                } else {
                    imgHeartQuick.setImageResource(R.drawable.ic_heart_outline)
                    imgHeartQuick.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
                }
            }

            updateMenuStarsUI(currentRating)
            updateMenuHeartUI()

            // Animación de entrada inicial de estrellas
            view.postDelayed({ updateMenuStarsUI(currentRating, true) }, 150)

            stars.forEachIndexed { index, img ->
                img.setOnClickListener {
                    val newRating = (index + 1).toFloat()
                    watchlistRepository.setMovieRating(movie, newRating)
                    updateMenuStarsUI(newRating, true)
                    updateStatusIcons(movie.id)
                    loadUserReview(movie.id)
                    checkNewAchievements()
                }
            }

            imgHeartQuick.setOnClickListener {
                isFav = !isFav
                watchlistRepository.toggleFavorite(movie)
                updateMenuHeartUI(true)
                updateStatusIcons(movie.id)
                setupWatchlistButton(movie)
                checkNewAchievements()
            }

            // --- RESTO DE OPCIONES ---
            view.findViewById<View>(R.id.optionWriteReview).setOnClickListener {
                bottomSheet.dismiss()
                showRatingBottomSheet(movie)
            }

            view.findViewById<TextView>(R.id.textMenuVitrina)?.text = if (isInVitrina) getString(R.string.remove_from_vitrina) else getString(R.string.add_to_vitrina)
            view.findViewById<TextView>(R.id.textMenuWatched)?.text = if (isWatched) getString(R.string.remove_from_watched) else getString(R.string.mark_as_watched)

            view.findViewById<View>(R.id.optionVitrina).setOnClickListener {
                bottomSheet.dismiss()
                if (isInVitrina) {
                    watchlistRepository.removeFromVitrina(movie.id)
                    showDopamineSuccess(getString(R.string.removed), "")
                } else {
                    val result = watchlistRepository.addMovieToVitrinaAuto(currentMovie ?: movie)
                    when (result) {
                        0 -> {
                            showDopamineSuccess(getString(R.string.featured_title), getString(R.string.featured_msg))
                            checkNewAchievements()
                        }
                        1 -> Toast.makeText(this, "Esta película ya está en tu vitrina", Toast.LENGTH_SHORT).show()
                        2 -> Toast.makeText(this, "Tu vitrina está llena (máx. 4)", Toast.LENGTH_SHORT).show()
                    }
                }
                updateStatusIcons(movie.id)
            }

            view.findViewById<View>(R.id.optionAddList).setOnClickListener {
                bottomSheet.dismiss()
                showAddToListSheet(currentMovie ?: movie) { setupWatchlistButton(currentMovie ?: movie) }
            }
            
            view.findViewById<View>(R.id.optionMarkWatched).setOnClickListener {
                bottomSheet.dismiss()
                watchlistRepository.toggleWatched(currentMovie ?: movie)
                updateStatusIcons(movie.id)
                checkNewAchievements()
            }

            view.findViewById<View>(R.id.optionViewPoster).setOnClickListener {
                bottomSheet.dismiss()
                showFullPoster(currentMovie ?: movie)
            }
            
            bottomSheet.setContentView(view)
            bottomSheet.show()
        }
    }

    private fun setupWatchlistButton(movie: Movie) {
        val btnAdd: MaterialButton = findViewById(R.id.btnAddList)
        fun updateButtonState() {
            val isInAnyList = watchlistRepository.isInWatchlist(movie.id)
            if (isInAnyList) {
                btnAdd.text = getString(R.string.in_my_lists)
                btnAdd.setIconResource(android.R.drawable.checkbox_on_background)
                btnAdd.setBackgroundColor(getColor(R.color.surface_app))
                btnAdd.setTextColor(getColor(R.color.primary))
                btnAdd.setStrokeColorResource(R.color.primary)
                btnAdd.strokeWidth = 4
            } else {
                btnAdd.text = getString(R.string.add_to_list)
                btnAdd.setIconResource(android.R.drawable.ic_input_add)
                btnAdd.setBackgroundColor(getColor(R.color.primary))
                btnAdd.setTextColor(Color.WHITE)
                btnAdd.strokeWidth = 0
            }
        }
        updateButtonState()
        btnAdd.setOnClickListener { showAddToListSheet(currentMovie ?: movie) { updateButtonState() } }
    }

    private fun setupTabs() {
        val tabCast: TextView = findViewById(R.id.tabCast)
        val tabCrew: TextView = findViewById(R.id.tabCrew)
        val tabGenres: TextView = findViewById(R.id.tabGenres)
        val tabDetails: TextView = findViewById(R.id.tabDetails)

        tabCast.text = getString(R.string.cast)
        tabCrew.text = getString(R.string.crew)
        tabGenres.text = getString(R.string.genres_tab)
        tabDetails.text = getString(R.string.details_tab)

        val contents = listOf(
            findViewById<View>(R.id.recyclerCastTab),
            findViewById<View>(R.id.containerCrewTab),
            findViewById<View>(R.id.chipGroupGenresTab),
            findViewById<View>(R.id.containerDetailsTab)
        )
        val tabs = listOf(tabCast, tabCrew, tabGenres, tabDetails)

        tabs.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                tabs.forEach { it.setTextColor(getColor(R.color.text_secondary)) }
                textView.setTextColor(getColor(R.color.primary))
                contents.forEach { it.visibility = View.GONE }
                contents[index].visibility = View.VISIBLE
            }
        }
    }

    private fun loadExternalRatingsAndAwards(query: String, isId: Boolean, year: String?) {
        lifecycleScope.launch {
            val omdbData = if (isId) omdbRepository.getMovieRatingsById(query) 
                           else omdbRepository.getMovieRatingsByTitle(query, year)
            
            if (omdbData != null) {
                var hasAnyRating = false
                
                omdbData.imdbRating?.let { if (it != "N/A" && it.isNotEmpty()) {
                    findViewById<View>(R.id.layoutImdb).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.textImdbRating).text = "$it/10"
                    hasAnyRating = true
                }}

                omdbData.Ratings?.forEach { rating ->
                    when (rating.Source) {
                        "Rotten Tomatoes" -> {
                            findViewById<View>(R.id.layoutRotten).visibility = View.VISIBLE
                            findViewById<TextView>(R.id.textRottenRating).text = rating.Value
                            hasAnyRating = true
                        }
                        "Metacritic" -> {
                            findViewById<View>(R.id.layoutMetacritic).visibility = View.VISIBLE
                            findViewById<TextView>(R.id.textMetacriticRating).text = rating.Value.split("/")[0]
                            hasAnyRating = true
                        }
                    }
                }

                if (hasAnyRating) findViewById<View>(R.id.layoutExternalRatings).visibility = View.VISIBLE

                val awards = omdbData.Awards
                if (!awards.isNullOrEmpty() && awards != "N/A") {
                    findViewById<View>(R.id.layoutAwards).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.textAwards).text = awards
                }
            }
        }
    }

    private fun showRatingBottomSheet(movie: Movie) {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_rating, null)
        view.findViewById<TextView>(R.id.textMovieNameRating).text = movie.title
        val stars = listOf<ImageView>(
            view.findViewById(R.id.star1), view.findViewById(R.id.star2),
            view.findViewById(R.id.star3), view.findViewById(R.id.star4), view.findViewById(R.id.star5)
        )
        val imgHeart: ImageView = view.findViewById(R.id.imgHeartRating)
        val editReview: EditText = view.findViewById(R.id.editReview)
        val textDate: TextView = view.findViewById(R.id.textRatingDate)
        
        var selectedRating = watchlistRepository.getMovieRating(movie.id)
        var isFavorite = watchlistRepository.isFavorite(movie.id)
        
        // Cargar reseña existente si la hay
        val existingEntry = watchlistRepository.getDiary().find { it.movieId == movie.id }
        editReview.setText(existingEntry?.review ?: "")
        
        // Mostrar fecha actual (En español)
        val isSpanish = repository.getLanguage() == "es-ES"
        val locale = if (isSpanish) Locale("es", "ES") else Locale("en", "US")
        val currentDate = SimpleDateFormat("dd/MM/yyyy", locale).format(Date())
        textDate.text = currentDate
        
        fun updateStarsUI(rating: Float, animated: Boolean = false) {
            stars.forEachIndexed { index, img ->
                val isActive = index < rating
                
                if (animated) {
                    img.animate()
                        .scaleX(if (isActive) 1.3f else 0.8f)
                        .scaleY(if (isActive) 1.3f else 0.8f)
                        .setDuration(120)
                        .setStartDelay(index * 20L)
                        .withEndAction {
                            if (isActive) {
                                img.setImageResource(R.drawable.ic_star_active)
                                img.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
                            } else {
                                img.setImageResource(R.drawable.ic_star_border)
                                img.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
                            }
                            img.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setInterpolator(OvershootInterpolator(2f))
                                .setDuration(250)
                                .setStartDelay(0)
                                .start()
                        }
                        .start()
                } else {
                    if (isActive) {
                        img.setImageResource(R.drawable.ic_star_active)
                        img.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
                    } else {
                        img.setImageResource(R.drawable.ic_star_border)
                        img.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
                    }
                }
            }
        }

        fun updateHeartUI(animated: Boolean = false) {
            if (isFavorite) {
                imgHeart.setImageResource(R.drawable.ic_heart_filled)
                imgHeart.imageTintList = ColorStateList.valueOf(getColor(R.color.primary))
                if (animated) {
                    imgHeart.scaleX = 0.7f
                    imgHeart.scaleY = 0.7f
                    imgHeart.animate().scaleX(1.2f).scaleY(1.2f).setDuration(150).withEndAction {
                        imgHeart.animate().scaleX(1.0f).scaleY(1.0f).setInterpolator(OvershootInterpolator()).setDuration(200).start()
                    }.start()
                }
            } else {
                imgHeart.setImageResource(R.drawable.ic_heart_outline)
                imgHeart.imageTintList = ColorStateList.valueOf(getColor(R.color.text_secondary))
            }
        }

        updateStarsUI(selectedRating)
        updateHeartUI()

        view.postDelayed({ updateStarsUI(selectedRating, true) }, 200)

        stars.forEachIndexed { index, img -> 
            img.setOnClickListener { 
                selectedRating = (index + 1).toFloat()
                updateStarsUI(selectedRating, true) 
            } 
        }

        imgHeart.setOnClickListener {
            isFavorite = !isFavorite
            updateHeartUI(true)
        }
        
        view.findViewById<View>(R.id.btnSaveRating).setOnClickListener {
            val reviewText = editReview.text.toString().trim()
            
            // Guardar valoración
            watchlistRepository.setMovieRating(movie, selectedRating, if (reviewText.isEmpty()) null else reviewText)
            
            // Sincronizar corazón si ha cambiado
            if (isFavorite != watchlistRepository.isFavorite(movie.id)) {
                watchlistRepository.toggleFavorite(movie)
            }

            updateStatusIcons(movie.id)
            loadUserReview(movie.id)
            setupWatchlistButton(movie)
            bottomSheet.dismiss()
            showDopamineSuccess(getString(R.string.rating_saved_title), getString(R.string.rating_saved_msg))
            
            // Comprobar logros tras valoración
            view.postDelayed({ checkNewAchievements() }, 600)
        }

        view.findViewById<View>(R.id.btnRemoveRating).setOnClickListener {
            watchlistRepository.setMovieRating(movie, 0f)
            updateStatusIcons(movie.id)
            loadUserReview(movie.id)
            bottomSheet.dismiss()
            showDopamineSuccess(getString(R.string.deleted), getString(R.string.rating_removed_msg))
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun showDopamineSuccess(title: String, msg: String) {
        val rootLayout = findViewById<ViewGroup>(android.R.id.content)
        val dopamineView = layoutInflater.inflate(R.layout.layout_rating_success, rootLayout, false)
        
        dopamineView.findViewById<TextView>(R.id.textSuccessTitle).text = title
        dopamineView.findViewById<TextView>(R.id.textSuccessMsg).text = msg
        
        // Ajustamos los parámetros para que no se pegue arriba (StatusBar)
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        params.topMargin = 120 // Bajamos la notificación para que no tape la hora
        dopamineView.layoutParams = params
        
        rootLayout.addView(dopamineView)
        
        dopamineView.alpha = 0f
        dopamineView.translationY = -40f // Animación desde un poco más arriba
        dopamineView.scaleX = 0.8f
        dopamineView.scaleY = 0.8f
        
        dopamineView.animate()
            .alpha(1f)
            .translationY(0f)
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(400)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                dopamineView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
                lifecycleScope.launch {
                    delay(2200)
                    dopamineView.animate()
                        .alpha(0f)
                        .translationY(-30f)
                        .scaleX(0.9f)
                        .scaleY(0.9f)
                        .setDuration(300)
                        .withEndAction { rootLayout.removeView(dopamineView) }
                        .start()
                }
            }
            .start()
    }

    private fun showAddToListSheet(movie: Movie, onComplete: () -> Unit) {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_add_to_list, null)
        
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerLists)
        val btnCreate = view.findViewById<LinearLayout>(R.id.btnCreateNewList)
        
        val userLists = watchlistRepository.getUserLists()
        
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = ListSelectionAdapter(userLists, movie.id) { list: UserList, isChecked: Boolean ->
            if (isChecked) {
                watchlistRepository.addMovieToList(list.id, movie)
                showDopamineSuccess(getString(R.string.added), getString(R.string.in_list_msg, list.name))
                checkNewAchievements()
            } else {
                watchlistRepository.removeMovieFromList(list.id, movie.id)
                showDopamineSuccess(getString(R.string.removed), getString(R.string.from_list_msg, list.name))
            }
            onComplete()
        }

        btnCreate.setOnClickListener {
            bottomSheet.dismiss()
            showModernCreateListDialog(movie, onComplete)
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun showModernCreateListDialog(movie: Movie? = null, onComplete: (() -> Unit)? = null) {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.new_collection))
        
        val input = EditText(this)
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(64, 0, 64, 0)
        input.layoutParams = params
        input.hint = getString(R.string.nav_list)
        container.addView(input)
        
        builder.setView(container)
        builder.setPositiveButton(getString(R.string.create)) { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                watchlistRepository.createUserList(name)
                movie?.let { 
                    val allLists = watchlistRepository.getUserLists()
                    if (allLists.isNotEmpty()) {
                        watchlistRepository.addMovieToList(allLists.last().id, it)
                        showDopamineSuccess(getString(R.string.list_created), getString(R.string.added_to_list_msg, name))
                        checkNewAchievements()
                    }
                }
                onComplete?.invoke()
            }
        }
        builder.setNegativeButton(getString(R.string.cancel), null)
        builder.show()
    }

    private fun showFullPoster(movie: Movie) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_full_poster, null)
        dialog.setContentView(view)
        
        val posterUrls = mutableListOf<String>()
        movie.posterPath?.let { posterUrls.add(it.replace("w500", "original")) }
        
        val viewPager: ViewPager2 = view.findViewById(R.id.viewPagerPosters)
        val recyclerThumbnails: RecyclerView = view.findViewById(R.id.recyclerThumbnails)
        val textPosterCount: TextView = view.findViewById(R.id.textPosterCount)
        val btnPrev: ImageButton = view.findViewById(R.id.btnPrevPoster)
        val btnNext: ImageButton = view.findViewById(R.id.btnNextPoster)
        val btnClose: View = view.findViewById(R.id.btnClosePoster)

        val mainAdapter = PosterPagerAdapter(posterUrls)
        val thumbAdapter = ThumbnailAdapter(posterUrls) { position -> viewPager.currentItem = position }

        viewPager.adapter = mainAdapter
        recyclerThumbnails.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerThumbnails.adapter = thumbAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                textPosterCount.text = "${position + 1} / ${posterUrls.size}"
                thumbAdapter.setSelected(position)
                recyclerThumbnails.smoothScrollToPosition(position)
            }
        })

        btnPrev.setOnClickListener { if (viewPager.currentItem > 0) viewPager.currentItem -= 1 }
        btnNext.setOnClickListener { if (viewPager.currentItem < posterUrls.size - 1) viewPager.currentItem += 1 }
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()

        lifecycleScope.launch {
            val images = if (movie.isTvShow) repository.getTVImages(movie.id) else repository.getMovieImages(movie.id)
            if (images.isNotEmpty()) {
                posterUrls.clear()
                posterUrls.addAll(images)
                mainAdapter.notifyDataSetChanged()
                thumbAdapter.notifyDataSetChanged()
                textPosterCount.text = "1 / ${posterUrls.size}"
            }
        }
    }

    private fun loadMovieTrailers(movieId: Int) {
        lifecycleScope.launch {
            val videos = repository.getMovieTrailers(movieId)
            if (videos.isNotEmpty()) {
                setupTrailerUI(videos[0].key)
            }
        }
    }

    private fun loadTVTrailers(tvId: Int) {
        lifecycleScope.launch {
            val videos = repository.getTVTrailers(tvId)
            if (videos.isNotEmpty()) {
                setupTrailerUI(videos[0].key)
            }
        }
    }

    private fun setupTrailerUI(youtubeKey: String) {
        val layoutTrailer = findViewById<View>(R.id.layoutTrailer)
        val btnWatch = findViewById<MaterialButton>(R.id.btnWatchTrailer)
        
        layoutTrailer.visibility = View.VISIBLE
        btnWatch.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$youtubeKey"))
            startActivity(intent)
        }
    }

    private fun loadSagaIfAvailable(movieId: Int) {
        lifecycleScope.launch {
            val details = repository.getMovieDetails(movieId)
            details?.belongs_to_collection?.id?.let { id ->
                val saga = repository.getCollectionMovies(id).filter { it.id != movieId }
                if (saga.isNotEmpty()) {
                    findViewById<View>(R.id.cardSaga).visibility = View.VISIBLE
                    val recycler: RecyclerView = findViewById(R.id.recyclerSagaMovies)
                    recycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    recycler.adapter = MovieHorizontalAdapter(saga).apply { onItemClick = { refreshDetail(it) } }
                }
            }
        }
    }

    private fun refreshDetail(movie: Movie) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("movie", movie)
        startActivity(intent)
        finish()
    }

    private fun checkNewAchievements() {
        val allAchievements = AchievementsActivity.getAchievements(watchlistRepository)
        val seenCount = watchlistRepository.getSeenAchievementsCount()
        val completedAchievements = allAchievements.filter { it.currentProgress >= it.maxProgress }
        
        if (completedAchievements.size > seenCount) {
            // El usuario ha completado uno o varios logros nuevos
            // Mostramos el primero que no haya sido "visto"
            val newAchievement = completedAchievements.getOrNull(seenCount)
            if (newAchievement != null) {
                showAchievementNotification(newAchievement)
            }
            watchlistRepository.setSeenAchievementsCount(completedAchievements.size)
        }
    }

    private fun showAchievementNotification(achievement: Achievement) {
        val rootLayout = findViewById<ViewGroup>(android.R.id.content)
        val achievementView = layoutInflater.inflate(R.layout.layout_achievement_unlocked, rootLayout, false)
        
        achievementView.findViewById<TextView>(R.id.textAchievementTitle).text = achievement.title
        achievementView.findViewById<TextView>(R.id.textAchievementDescription).text = achievement.description
        
        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        params.topMargin = 280
        achievementView.layoutParams = params
        
        rootLayout.addView(achievementView)
        
        achievementView.alpha = 0f
        achievementView.translationY = -100f
        
        achievementView.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator())
            .withEndAction {
                lifecycleScope.launch {
                    delay(4500)
                    achievementView.animate()
                        .alpha(0f)
                        .translationY(-100f)
                        .setDuration(500)
                        .withEndAction { rootLayout.removeView(achievementView) }
                        .start()
                }
            }
            .start()
    }

    inner class WatchProviderAdapter(private val providers: List<WatchProviderItem>) : RecyclerView.Adapter<WatchProviderAdapter.ViewHolder>() {
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) { val img: ImageView = v.findViewById(R.id.imgProviderLogo) }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_watch_provider, p, false))
        override fun onBindViewHolder(h: ViewHolder, p: Int) { Glide.with(h.itemView).load("https://image.tmdb.org/t/p/w154${providers[p].logo_path}").into(h.img) }
        override fun getItemCount() = providers.size
    }

    inner class PosterPagerAdapter(private val urls: List<String>) : RecyclerView.Adapter<PosterPagerAdapter.ViewHolder>() {
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) { val img: ImageView = v.findViewById(R.id.imgPosterSlide) }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_poster_slide, p, false))
        override fun onBindViewHolder(h: ViewHolder, p: Int) { Glide.with(h.itemView).load(urls[p]).into(h.img) }
        override fun getItemCount() = urls.size
    }

    inner class ThumbnailAdapter(private val urls: List<String>, private val onClick: (Int) -> Unit) : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
        private var selectedPos = 0
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) { 
            val img: ImageView = v.findViewById(R.id.imgThumbnail)
            val card: MaterialCardView = v.findViewById(R.id.cardThumbnail)
        }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_poster_thumbnail, p, false))
        override fun onBindViewHolder(h: ViewHolder, p: Int) {
            Glide.with(h.itemView).load(urls[p].replace("original", "w185")).into(h.img)
            h.card.strokeWidth = if (selectedPos == p) { (6 * h.itemView.context.resources.displayMetrics.density).toInt() } else 0
            h.card.strokeColor = getColor(R.color.primary)
            h.itemView.setOnClickListener { onClick(p) }
        }
        override fun getItemCount() = urls.size
        fun setSelected(pos: Int) { val old = selectedPos; selectedPos = pos; notifyItemChanged(old); notifyItemChanged(selectedPos) }
    }
}
