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
import android.view.Window
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.vexo.app.R
import data.model.Movie
import data.repository.TMDBRepository
import data.repository.WatchlistRepository
import data.repository.WatchProviderItem
import data.repository.OMDbRepository
import kotlinx.coroutines.launch
import ui.explore.MovieHorizontalAdapter
import ui.genre.GenreActivity
import java.text.NumberFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private val omdbRepository = OMDbRepository.getInstance()
    private lateinit var watchlistRepository: WatchlistRepository
    private var currentMovie: Movie? = null

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
            
            if (movie.isTvShow) {
                loadFullTVDetails(movie.id)
                loadTVCredits(movie.id)
                loadTVRecommendations(movie.id)
                loadTVTrailers(movie.id)
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
        textRating.text = "★ ${String.format("%.1f", movie.rating)}"
        textOverview.text = movie.overview

        btnBack.setOnClickListener { finish() }

        Glide.with(this)
            .load(movie.backdropPath ?: movie.posterPath)
            .into(imgPoster)

        imgPoster.setOnClickListener { showFullPoster(movie) }
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

        val userRating = watchlistRepository.getMovieRating(movieId)
        val layoutUserRating: View? = findViewById(R.id.layoutUserRatingDisplay)
        
        if (userRating > 0) {
            layoutUserRating?.visibility = View.VISIBLE
            val userStars = listOf<ImageView?>(
                findViewById(R.id.userStar1), findViewById(R.id.userStar2),
                findViewById(R.id.userStar3), findViewById(R.id.userStar4),
                findViewById(R.id.userStar5)
            )
            
            userStars.forEachIndexed { index, imageView ->
                if (imageView != null) {
                    if (index < userRating) {
                        imageView.setImageResource(android.R.drawable.btn_star_big_on)
                        imageView.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.primary))
                        imageView.alpha = 1.0f
                    } else {
                        imageView.setImageResource(android.R.drawable.btn_star_big_off)
                        imageView.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.text_secondary))
                        imageView.alpha = 0.3f
                    }
                }
            }
            findViewById<TextView>(R.id.textUserRatingValue)?.text = "${userRating.toInt()} de 5"
        } else {
            layoutUserRating?.visibility = View.GONE
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
        
        pegiTextView.text = "PEGI $pegi"
        pegiTextView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    private fun loadFullMovieDetails(movieId: Int) {
        lifecycleScope.launch {
            val details = repository.getMovieDetails(movieId)
            if (details != null) {
                currentMovie = currentMovie?.copy(releaseDate = details.release_date)
                val year = details.release_date.take(4)
                findViewById<TextView>(R.id.textYearDetail)?.text = year
                findViewById<TextView>(R.id.textRuntimeDetail)?.text = "${details.runtime} min"
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
                findViewById<TextView>(R.id.textBudget)?.text = if (details.budget > 0) currencyFormat.format(details.budget) else "--"
                findViewById<TextView>(R.id.textRevenue)?.text = if (details.revenue > 0) currencyFormat.format(details.revenue) else "--"
                
                findViewById<TextView>(R.id.textStatus)?.text = details.status
                findViewById<TextView>(R.id.textReleaseDateFull)?.text = details.release_date
                findViewById<TextView>(R.id.textOriginalTitle)?.text = details.original_title
                findViewById<TextView>(R.id.textOriginalLanguage)?.text = details.original_language.uppercase()
                findViewById<TextView>(R.id.textProductionCountries)?.text = details.production_countries.joinToString(", ") { it.name }
                findViewById<TextView>(R.id.textProductionCompanies)?.text = details.production_companies.joinToString(", ") { it.name }

                val providers = details.watchProviders?.results?.get("ES")?.flatrate
                if (!providers.isNullOrEmpty()) {
                    findViewById<View>(R.id.layoutWatchProviders).visibility = View.VISIBLE
                    val recycler: RecyclerView = findViewById(R.id.recyclerWatchProviders)
                    recycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    recycler.adapter = WatchProviderAdapter(providers)
                }
            }
        }
    }

    private fun loadFullTVDetails(tvId: Int) {
        lifecycleScope.launch {
            val details = repository.getTVDetails(tvId)
            if (details != null) {
                currentMovie = currentMovie?.copy(releaseDate = details.first_air_date)
                val year = details.first_air_date.take(4)
                findViewById<TextView>(R.id.textYearDetail)?.text = year
                findViewById<TextView>(R.id.textTagline)?.text = details.tagline ?: ""
                
                // OCULTAR TEXTO PEQUEÑO Y MOSTRAR CARD ELEGANTE
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

                findViewById<View>(R.id.labelBudget)?.visibility = View.GONE
                findViewById<View>(R.id.textBudget)?.visibility = View.GONE
                findViewById<View>(R.id.labelRevenue)?.visibility = View.GONE
                findViewById<View>(R.id.textRevenue)?.visibility = View.GONE
                
                val imdbId = details.external_ids?.imdb_id
                if (!imdbId.isNullOrEmpty()) {
                    loadExternalRatingsAndAwards(imdbId, true, null)
                } else {
                    loadExternalRatingsAndAwards(details.original_name, false, year)
                }

                setupGenres(details.genres)
                
                findViewById<TextView>(R.id.textStatus)?.text = details.status
                findViewById<TextView>(R.id.textReleaseDateFull)?.text = details.first_air_date
                findViewById<TextView>(R.id.textOriginalTitle)?.text = details.name
                findViewById<TextView>(R.id.textOriginalLanguage)?.text = details.original_language.uppercase()
                findViewById<TextView>(R.id.textProductionCountries)?.text = details.production_countries.joinToString(", ") { it.name }
                findViewById<TextView>(R.id.textProductionCompanies)?.text = details.production_companies.joinToString(", ") { it.name }

                val providers = details.watchProviders?.results?.get("ES")?.flatrate
                if (!providers.isNullOrEmpty()) {
                    findViewById<View>(R.id.layoutWatchProviders).visibility = View.VISIBLE
                    val recycler: RecyclerView = findViewById(R.id.recyclerWatchProviders)
                    recycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    recycler.adapter = WatchProviderAdapter(providers)
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
        val isSpanish = repository.getLanguage() == "es-ES"

        btnMenu.setOnClickListener {
            val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = layoutInflater.inflate(R.layout.layout_movie_menu, null)
            
            view.findViewById<TextView>(R.id.menuTitle).text = if (isSpanish) "Opciones" else "Options"
            
            val isFav = watchlistRepository.isFavorite(movie.id)
            val isWatched = watchlistRepository.isWatched(movie.id)
            val isInVitrina = watchlistRepository.isMovieInVitrina(movie.id)

            view.findViewById<TextView>(R.id.textMenuVitrina)?.text = if (isInVitrina) "Quitar de mi vitrina" else "Destacar en mi vitrina"
            view.findViewById<ImageView>(R.id.imgMenuHeart)?.setImageResource(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
            view.findViewById<TextView>(R.id.textMenuFavorite)?.text = if (isFav) "Quitar de favoritos" else "Dar corazón"
            view.findViewById<TextView>(R.id.textMenuWatched)?.text = if (isWatched) "Quitar de vistas" else "Marcar como vista"

            view.findViewById<View>(R.id.optionVitrina).setOnClickListener {
                bottomSheet.dismiss()
                if (isInVitrina) {
                    watchlistRepository.removeFromVitrina(movie.id)
                } else {
                    watchlistRepository.addMovieToVitrinaAuto(currentMovie ?: movie)
                }
                updateStatusIcons(movie.id)
            }

            view.findViewById<View>(R.id.optionAddList).setOnClickListener {
                bottomSheet.dismiss()
                showListSelectionDialog(currentMovie ?: movie) { setupWatchlistButton(currentMovie ?: movie) }
            }
            
            view.findViewById<View>(R.id.optionMarkWatched).setOnClickListener {
                bottomSheet.dismiss()
                watchlistRepository.toggleWatched(currentMovie ?: movie)
                updateStatusIcons(movie.id)
            }
            
            view.findViewById<View>(R.id.optionFavorite).setOnClickListener {
                bottomSheet.dismiss()
                watchlistRepository.toggleFavorite(currentMovie ?: movie)
                updateStatusIcons(movie.id)
                setupWatchlistButton(currentMovie ?: movie)
            }
            
            view.findViewById<View>(R.id.optionRate).setOnClickListener {
                bottomSheet.dismiss()
                showRatingBottomSheet(currentMovie ?: movie)
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
                btnAdd.text = "EN MIS LISTAS"
                btnAdd.setIconResource(android.R.drawable.checkbox_on_background)
                btnAdd.setBackgroundColor(getColor(R.color.surface_app))
                btnAdd.setTextColor(getColor(R.color.primary))
                btnAdd.setStrokeColorResource(R.color.primary)
                btnAdd.strokeWidth = 4
            } else {
                btnAdd.text = "AÑADIR A MI LISTA"
                btnAdd.setIconResource(android.R.drawable.ic_input_add)
                btnAdd.setBackgroundColor(getColor(R.color.primary))
                btnAdd.setTextColor(Color.WHITE)
                btnAdd.strokeWidth = 0
            }
        }
        updateButtonState()
        btnAdd.setOnClickListener { showListSelectionDialog(currentMovie ?: movie) { updateButtonState() } }
    }

    private fun setupTabs() {
        val tabCast: TextView = findViewById(R.id.tabCast)
        val tabCrew: TextView = findViewById(R.id.tabCrew)
        val tabGenres: TextView = findViewById(R.id.tabGenres)
        val tabDetails: TextView = findViewById(R.id.tabDetails)

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
        var selectedRating = watchlistRepository.getMovieRating(movie.id)
        
        fun updateStarsUI(rating: Float) {
            stars.forEachIndexed { index, img ->
                if (index < rating) {
                    img.setImageResource(android.R.drawable.btn_star_big_on)
                    img.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.primary))
                } else {
                    img.setImageResource(android.R.drawable.btn_star_big_off)
                    img.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.text_secondary))
                }
            }
        }
        updateStarsUI(selectedRating)
        stars.forEachIndexed { index, img -> img.setOnClickListener { selectedRating = (index + 1).toFloat(); updateStarsUI(selectedRating) } }
        
        view.findViewById<View>(R.id.btnSaveRating).setOnClickListener {
            watchlistRepository.setMovieRating(movie, selectedRating)
            updateStatusIcons(movie.id)
            bottomSheet.dismiss()
        }

        view.findViewById<View>(R.id.btnRemoveRating).setOnClickListener {
            watchlistRepository.setMovieRating(movie, 0f)
            updateStatusIcons(movie.id)
            bottomSheet.dismiss()
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun showListSelectionDialog(movie: Movie, onComplete: () -> Unit) {
        val lists = watchlistRepository.getUserLists()
        if (lists.isEmpty()) {
            showCreateListDialog(movie, onComplete)
            return
        }
        val listNames = lists.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(lists.size) { i -> lists[i].movies.any { it.id == movie.id } }

        AlertDialog.Builder(this).setTitle("Selecciona Listas").setMultiChoiceItems(listNames, checkedItems) { _, which, isChecked ->
            if (isChecked) watchlistRepository.addMovieToList(lists[which].id, movie)
            else watchlistRepository.removeMovieFromList(lists[which].id, movie.id)
        }.setPositiveButton("Listo") { _, _ -> onComplete() }
        .setNeutralButton("+ Nueva Lista") { _, _ -> showCreateListDialog(movie, onComplete) }.show()
    }

    private fun showCreateListDialog(movie: Movie? = null, onComplete: (() -> Unit)? = null) {
        val input = EditText(this).apply { hint = "Nombre de la lista" }
        AlertDialog.Builder(this).setTitle("Nueva Colección").setView(input).setPositiveButton("Crear") { _, _ ->
            val name = input.text.toString().trim()
            if (name.isNotEmpty()) {
                watchlistRepository.createUserList(name)
                movie?.let { watchlistRepository.addMovieToList(watchlistRepository.getUserLists().last().id, it) }
                onComplete?.invoke()
            }
        }.show()
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
            h.card.strokeWidth = if (selectedPos == p) 6 else 0
            h.card.strokeColor = getColor(R.color.primary)
            h.itemView.setOnClickListener { onClick(p) }
        }
        override fun getItemCount() = urls.size
        fun setSelected(pos: Int) { val old = selectedPos; selectedPos = pos; notifyItemChanged(old); notifyItemChanged(selectedPos) }
    }
}
