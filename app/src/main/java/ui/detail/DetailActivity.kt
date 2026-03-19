package ui.detail

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
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
            loadFullMovieDetails(movie.id)
            loadCastAndCrew(movie.id)
            loadSagaIfAvailable(movie.id)
            loadRecommendations(movie.id)
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

        // Mostrar valoración del usuario en el apartado Letterboxd
        val userRating = watchlistRepository.getMovieRating(movieId)
        val layoutUserRating: View? = findViewById(R.id.layoutUserRatingDisplay)
        
        if (userRating > 0) {
            layoutUserRating?.visibility = View.VISIBLE
            val userStars = listOf<ImageView?>(
                findViewById(R.id.userStar1),
                findViewById(R.id.userStar2),
                findViewById(R.id.userStar3),
                findViewById(R.id.userStar4),
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

    private fun showCustomToast(message: String, iconRes: Int) {
        try {
            val layout = layoutInflater.inflate(R.layout.layout_custom_toast, null)
            layout.findViewById<TextView>(R.id.textToastMessage).text = message
            val iconView = layout.findViewById<ImageView>(R.id.imgToastIcon)
            iconView.setImageResource(iconRes)
            
            val colorRes = if (message.contains("Favoritos") || message.contains("Favorites")) R.color.primary else R.color.accent
            iconView.imageTintList = android.content.res.ColorStateList.valueOf(getColor(colorRes))

            val toast = Toast(applicationContext)
            toast.duration = Toast.LENGTH_SHORT
            toast.view = layout
            toast.setGravity(Gravity.BOTTOM, 0, 100)
            toast.show()
        } catch (e: Exception) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

            // Configurar botón de Vitrina
            val textVitrina = view.findViewById<TextView>(R.id.textMenuVitrina)
            val isInVitrina = watchlistRepository.isMovieInVitrina(movie.id)
            textVitrina?.text = if (isInVitrina) "Quitar de mi vitrina" else "Destacar en mi vitrina"

            val imgHeart = view.findViewById<ImageView>(R.id.imgMenuHeart)
            val textFav = view.findViewById<TextView>(R.id.textMenuFavorite)
            imgHeart?.setImageResource(if (isFav) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline)
            textFav?.text = if (isFav) (if (isSpanish) "Quitar de favoritos" else "Remove from favorites") 
                           else (if (isSpanish) "Dar corazón" else "Add to favorites")

            val imgWatched = view.findViewById<ImageView>(R.id.imgMenuWatched)
            val textWatched = view.findViewById<TextView>(R.id.textMenuWatched)
            textWatched?.text = if (isWatched) (if (isSpanish) "Quitar de vistas" else "Mark as unwatched") 
                               else (if (isSpanish) "Marcar como vista" else "Mark as watched")

            // Lógica opción Vitrina
            view.findViewById<View>(R.id.optionVitrina).setOnClickListener {
                bottomSheet.dismiss()
                if (isInVitrina) {
                    watchlistRepository.removeFromVitrina(movie.id)
                    showCustomToast("Quitada de tu vitrina", android.R.drawable.ic_menu_gallery)
                } else {
                    val result = watchlistRepository.addMovieToVitrinaAuto(movie)
                    when (result) {
                        0 -> showCustomToast("Añadida a tu vitrina", android.R.drawable.ic_menu_gallery)
                        1 -> showCustomToast("Ya está en tu vitrina", android.R.drawable.ic_menu_gallery)
                        2 -> Toast.makeText(this, "Tu vitrina está llena (máximo 4 películas)", Toast.LENGTH_LONG).show()
                    }
                }
            }

            view.findViewById<View>(R.id.optionAddList).setOnClickListener {
                bottomSheet.dismiss()
                showListSelectionDialog(movie) { setupWatchlistButton(movie) }
            }
            
            view.findViewById<View>(R.id.optionViewPoster).setOnClickListener {
                bottomSheet.dismiss()
                showFullPoster(movie)
            }
            
            view.findViewById<View>(R.id.optionMarkWatched).setOnClickListener {
                bottomSheet.dismiss()
                val added = watchlistRepository.toggleWatched(movie)
                updateStatusIcons(movie.id)
                showCustomToast(
                    if (added) (if (isSpanish) "Marcada como vista" else "Marked as watched") 
                    else (if (isSpanish) "Quitada de vistas" else "Removed from watched"),
                    android.R.drawable.ic_menu_view
                )
            }
            
            view.findViewById<View>(R.id.optionFavorite).setOnClickListener {
                bottomSheet.dismiss()
                val added = watchlistRepository.toggleFavorite(movie)
                updateStatusIcons(movie.id)
                showCustomToast(
                    if (added) (if (isSpanish) "Añadida a Favoritos" else "Added to Favorites")
                    else (if (isSpanish) "Quitada de Favoritos" else "Removed from Favorites"),
                    if (added) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                )
                setupWatchlistButton(movie)
            }
            
            view.findViewById<View>(R.id.optionRate).setOnClickListener {
                bottomSheet.dismiss()
                showRatingBottomSheet(movie)
            }
            
            bottomSheet.setContentView(view)
            bottomSheet.show()
        }
    }

    private fun showRatingBottomSheet(movie: Movie) {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.dialog_rating, null)
        
        view.findViewById<TextView>(R.id.textMovieNameRating).text = movie.title
        
        val stars = listOf<ImageView>(
            view.findViewById(R.id.star1),
            view.findViewById(R.id.star2),
            view.findViewById(R.id.star3),
            view.findViewById(R.id.star4),
            view.findViewById(R.id.star5)
        )

        var selectedRating = watchlistRepository.getMovieRating(movie.id)
        
        fun updateStarsUI(rating: Float) {
            stars.forEachIndexed { index, imageView ->
                if (index < rating) {
                    imageView.setImageResource(android.R.drawable.btn_star_big_on)
                    imageView.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.primary))
                } else {
                    imageView.setImageResource(android.R.drawable.btn_star_big_off)
                    imageView.imageTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.text_secondary))
                }
            }
        }

        updateStarsUI(selectedRating)

        stars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                selectedRating = (index + 1).toFloat()
                updateStarsUI(selectedRating)
            }
        }

        view.findViewById<View>(R.id.btnSaveRating).setOnClickListener {
            if (selectedRating > 0) {
                watchlistRepository.setMovieRating(movie, selectedRating)
                updateStatusIcons(movie.id)
                bottomSheet.dismiss()
                showCustomToast("¡Valoración guardada!", android.R.drawable.btn_star_big_on)
            }
        }

        view.findViewById<View>(R.id.btnRemoveRating).setOnClickListener {
            watchlistRepository.setMovieRating(movie, 0f)
            updateStatusIcons(movie.id)
            bottomSheet.dismiss()
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun showFullPoster(movie: Movie) {
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = layoutInflater.inflate(R.layout.dialog_full_poster, null)
        dialog.setContentView(view)

        val viewPager: ViewPager2 = view.findViewById(R.id.viewPagerPosters)
        val btnClose: View = view.findViewById(R.id.btnClosePoster)
        val btnPrev: View = view.findViewById(R.id.btnPrevPoster)
        val btnNext: View = view.findViewById(R.id.btnNextPoster)
        val textCount: TextView = view.findViewById(R.id.textPosterCount)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBarPosters)
        val recyclerThumbnails: RecyclerView = view.findViewById(R.id.recyclerThumbnails)

        val posterUrls = mutableListOf<String>()
        movie.posterPath?.let { posterUrls.add(it.replace("w500", "original")) }
        
        val adapter = PosterPagerAdapter(posterUrls)
        viewPager.adapter = adapter
        
        val thumbAdapter = ThumbnailAdapter(posterUrls) { position ->
            viewPager.currentItem = position
        }
        recyclerThumbnails.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerThumbnails.adapter = thumbAdapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                textCount.text = "${position + 1} / ${posterUrls.size}"
                thumbAdapter.setSelected(position)
                recyclerThumbnails.smoothScrollToPosition(position)
                btnPrev.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
                btnNext.visibility = if (position == posterUrls.size - 1) View.INVISIBLE else View.VISIBLE
            }
        })

        btnPrev.setOnClickListener { if (viewPager.currentItem > 0) viewPager.currentItem -= 1 }
        btnNext.setOnClickListener { if (viewPager.currentItem < posterUrls.size - 1) viewPager.currentItem += 1 }
        btnClose.setOnClickListener { dialog.dismiss() }

        dialog.show()

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            val images = repository.getMovieImages(movie.id)
            if (images.isNotEmpty()) {
                posterUrls.clear()
                posterUrls.addAll(images)
                adapter.notifyDataSetChanged()
                thumbAdapter.notifyDataSetChanged()
                textCount.text = "1 / ${posterUrls.size}"
            }
            progressBar.visibility = View.GONE
        }
    }

    private fun setupWatchlistButton(movie: Movie) {
        val btnAdd: MaterialButton = findViewById(R.id.btnAddList)
        val isSpanish = repository.getLanguage() == "es-ES"
        
        fun updateButtonState() {
            val isInAnyList = watchlistRepository.isInWatchlist(movie.id)
            if (isInAnyList) {
                btnAdd.text = if (isSpanish) "EN MIS LISTAS" else "IN MY LISTS"
                btnAdd.setIconResource(android.R.drawable.checkbox_on_background)
                btnAdd.setBackgroundColor(getColor(R.color.surface_app))
                btnAdd.setTextColor(getColor(R.color.primary))
                btnAdd.setStrokeColorResource(R.color.primary)
                btnAdd.strokeWidth = 4
            } else {
                btnAdd.text = if (isSpanish) "AÑADIR A MI LISTA" else "ADD TO MY LIST"
                btnAdd.setIconResource(android.R.drawable.ic_input_add)
                btnAdd.setBackgroundColor(getColor(R.color.primary))
                btnAdd.setTextColor(getColor(android.R.color.white))
                btnAdd.strokeWidth = 0
            }
        }

        updateButtonState()

        btnAdd.setOnClickListener {
            showListSelectionDialog(movie) {
                updateButtonState()
            }
        }
    }

    private fun showListSelectionDialog(movie: Movie, onComplete: () -> Unit) {
        val lists = watchlistRepository.getUserLists()
        val isSpanish = repository.getLanguage() == "es-ES"

        if (lists.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle(if (isSpanish) "No tienes listas" else "No lists found")
                .setMessage(if (isSpanish) "¿Quieres crear tu primera lista?" else "Do you want to create your first list?")
                .setPositiveButton(if (isSpanish) "Crear" else "Create") { _, _ ->
                    showCreateListDialog(movie, onComplete)
                }
                .setNegativeButton(if (isSpanish) "Cancelar" else "Cancel", null)
                .show()
            return
        }

        val listNames = lists.map { it.name }.toTypedArray()
        val checkedItems = BooleanArray(lists.size) { i -> 
            lists[i].movies.any { it.id == movie.id }
        }

        AlertDialog.Builder(this)
            .setTitle(if (isSpanish) "Selecciona Listas" else "Select Lists")
            .setMultiChoiceItems(listNames, checkedItems) { _, which, isChecked ->
                if (isChecked) {
                    watchlistRepository.addMovieToList(lists[which].id, movie)
                } else {
                    watchlistRepository.removeMovieFromList(lists[which].id, movie.id)
                }
            }
            .setPositiveButton(if (isSpanish) "Listo" else "Done") { _, _ ->
                onComplete()
            }
            .setNeutralButton(if (isSpanish) "+ Nueva Lista" else "+ New List") { _, _ ->
                showCreateListDialog(movie, onComplete)
            }
            .show()
    }

    private fun showCreateListDialog(movie: Movie? = null, onComplete: (() -> Unit)? = null) {
        val isSpanish = repository.getLanguage() == "es-ES"
        val input = EditText(this).apply { 
            hint = if (isSpanish) "Nombre de la lista" else "List name"
        }
        
        AlertDialog.Builder(this)
            .setTitle(if (isSpanish) "Nueva Colección" else "New Collection")
            .setView(input)
            .setPositiveButton(if (isSpanish) "Crear" else "Create") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    watchlistRepository.createUserList(name)
                    if (movie != null) {
                        val newLists = watchlistRepository.getUserLists()
                        newLists.lastOrNull()?.let { newList ->
                            watchlistRepository.addMovieToList(newList.id, movie)
                        }
                    }
                    onComplete?.invoke()
                    Toast.makeText(this, if (isSpanish) "Lista creada" else "List created", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(if (isSpanish) "Cancelar" else "Cancel", null)
            .show()
    }

    private fun setupTabs() {
        val isSpanish = repository.getLanguage() == "es-ES"
        val tabCast: TextView = findViewById(R.id.tabCast)
        val tabCrew: TextView = findViewById(R.id.tabCrew)
        val tabGenres: TextView = findViewById(R.id.tabGenres)
        val tabDetails: TextView = findViewById(R.id.tabDetails)

        tabCast.text = if (isSpanish) "REPARTO" else "CAST"
        tabCrew.text = if (isSpanish) "EQUIPO" else "CREW"
        tabGenres.text = if (isSpanish) "GÉNEROS" else "GENRES"
        tabDetails.text = if (isSpanish) "DETALLES" else "DETAILS"

        val recyclerCast: RecyclerView = findViewById(R.id.recyclerCastTab)
        val containerCrew: LinearLayout = findViewById(R.id.containerCrewTab)
        val groupGenres: ChipGroup = findViewById(R.id.chipGroupGenresTab)
        val containerDetails: LinearLayout = findViewById(R.id.containerDetailsTab)

        val tabs = listOf(tabCast, tabCrew, tabGenres, tabDetails)
        val contents = listOf(recyclerCast, containerCrew, groupGenres, containerDetails)

        tabs.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                tabs.forEach { it.setTextColor(getColor(R.color.text_secondary)) }
                textView.setTextColor(getColor(R.color.primary))
                
                contents.forEach { it.visibility = View.GONE }
                contents[index].visibility = View.VISIBLE
            }
        }
    }

    private fun loadFullMovieDetails(movieId: Int) {
        lifecycleScope.launch {
            val details = repository.getMovieDetails(movieId)
            if (details != null) {
                val year = details.release_date.take(4)
                findViewById<TextView>(R.id.textYearDetail)?.text = year
                findViewById<TextView>(R.id.textRuntimeDetail)?.text = "${details.runtime} min"
                findViewById<TextView>(R.id.textTagline)?.text = details.tagline ?: ""
                
                val externalId = details.imdb_id
                if (!externalId.isNullOrEmpty()) {
                    loadExternalRatingsAndAwards(externalId, true, year)
                } else {
                    loadExternalRatingsAndAwards(details.title, false, year)
                }

                val rawPegi = details.release_dates?.results?.find { it.iso_3166_1 == "ES" }?.release_dates?.firstOrNull()?.certification
                    ?: details.release_dates?.results?.find { it.iso_3166_1 == "US" }?.release_dates?.firstOrNull()?.certification
                
                if (!rawPegi.isNullOrEmpty()) {
                    findViewById<View>(R.id.dotPegi).visibility = View.VISIBLE
                    val textPegi: TextView = findViewById(R.id.textPegiDetail)
                    textPegi.visibility = View.VISIBLE
                    
                    val formattedPegi = if (rawPegi.all { it.isDigit() }) "+$rawPegi" else rawPegi
                    textPegi.text = formattedPegi

                    val pegiColor = when {
                        rawPegi.contains("18") -> "#EF4444"
                        rawPegi.contains("16") -> "#F59E0B"
                        rawPegi.contains("12") -> "#EAB308"
                        rawPegi.contains("7") -> "#10B981"
                        else -> "#3B82F6"
                    }
                    textPegi.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(pegiColor))
                }

                val providers = details.watchProviders?.results?.get("ES")?.flatrate
                if (!providers.isNullOrEmpty()) {
                    findViewById<View>(R.id.layoutWatchProviders).visibility = View.VISIBLE
                    val recycler: RecyclerView = findViewById(R.id.recyclerWatchProviders)
                    recycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    recycler.adapter = WatchProviderAdapter(providers)
                }
                
                findViewById<TextView>(R.id.textOriginalTitle)?.text = details.original_title
                findViewById<TextView>(R.id.textStatus)?.text = details.status
                findViewById<TextView>(R.id.textReleaseDateFull)?.text = details.release_date
                findViewById<TextView>(R.id.textOriginalLanguage)?.text = details.original_language.uppercase()
                
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                findViewById<TextView>(R.id.textBudget)?.text = if (details.budget > 0) currencyFormat.format(details.budget) else "--"
                findViewById<TextView>(R.id.textRevenue)?.text = if (details.revenue > 0) currencyFormat.format(details.revenue) else "--"
                
                findViewById<TextView>(R.id.textProductionCountries)?.text = details.production_countries.joinToString(", ") { it.name }
                findViewById<TextView>(R.id.textProductionCompanies)?.text = details.production_companies.joinToString(", ") { it.name }

                val groupGenres: ChipGroup = findViewById(R.id.chipGroupGenresTab)
                groupGenres.removeAllViews()
                details.genres.forEach { genre ->
                    val chip = Chip(this@DetailActivity).apply {
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
        }
    }

    private fun translateAwards(awards: String): String {
        return awards
            .replace("Won", "Ganadora de", ignoreCase = true)
            .replace("Nominated for", "Nominada a", ignoreCase = true)
            .replace("Another", "Otras", ignoreCase = true)
            .replace("wins", "victorias", ignoreCase = true)
            .replace("win", "victoria", ignoreCase = true)
            .replace("nominations", "nominaciones", ignoreCase = true)
            .replace("nomination", "nominación", ignoreCase = true)
            .replace("total", "en total", ignoreCase = true)
            .replace("Oscars", "Oscars", ignoreCase = true)
            .replace("Oscar", "Oscar", ignoreCase = true)
            .replace("Golden Globe", "Globo de Oro", ignoreCase = true)
            .replace("Golden Globes", "Globos de Oro", ignoreCase = true)
            .replace("&", "y")
            .trim()
    }

    private fun loadExternalRatingsAndAwards(query: String, isId: Boolean, year: String?) {
        lifecycleScope.launch {
            val omdbData = if (isId) omdbRepository.getMovieRatingsById(query) 
                           else omdbRepository.getMovieRatingsByTitle(query, year)
            
            if (omdbData != null) {
                var hasAnyRating = false
                val isSpanish = repository.getLanguage() == "es-ES"
                
                val imdbVal = omdbData.imdbRating
                if (!imdbVal.isNullOrEmpty() && imdbVal != "N/A") {
                    findViewById<View>(R.id.layoutImdb).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.textImdbRating).text = "$imdbVal/10"
                    hasAnyRating = true
                }

                omdbData.Ratings?.forEach { rating ->
                    when (rating.Source) {
                        "Rotten Tomatoes" -> {
                            findViewById<View>(R.id.layoutRotten).visibility = View.VISIBLE
                            findViewById<TextView>(R.id.textRottenRating).text = rating.Value
                            hasAnyRating = true
                        }
                        "Metacritic" -> {
                            findViewById<View>(R.id.layoutMetacritic).visibility = View.VISIBLE
                            val mcValue = rating.Value.split("/")[0]
                            findViewById<TextView>(R.id.textMetacriticRating).text = mcValue
                            hasAnyRating = true
                        }
                    }
                }

                val awards = omdbData.Awards
                if (!awards.isNullOrEmpty() && awards != "N/A") {
                    findViewById<View>(R.id.layoutAwards).visibility = View.VISIBLE
                    val textAwards: TextView = findViewById(R.id.textAwards)
                    textAwards.text = if (isSpanish) translateAwards(awards) else awards
                }

                if (hasAnyRating) {
                    findViewById<View>(R.id.layoutExternalRatings).visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadCastAndCrew(movieId: Int) {
        val recyclerCast: RecyclerView = findViewById(R.id.recyclerCastTab)
        val containerCrew: LinearLayout = findViewById(R.id.containerCrewTab)
        lifecycleScope.launch {
            val credits = repository.getMovieCredits(movieId)
            if (credits != null) {
                recyclerCast.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                recyclerCast.adapter = CastAdapter(credits.cast) { id, name -> openPersonMovies(id, name) }

                containerCrew.removeAllViews()
                credits.crew.filter { it.job in listOf("Director", "Writer", "Producer") }.forEach { member ->
                    val view = LayoutInflater.from(this@DetailActivity).inflate(R.layout.item_director, containerCrew, false)
                    view.findViewById<TextView>(R.id.textDirectorName).text = member.name
                    view.findViewById<TextView>(R.id.textCrewRole).text = member.job
                    Glide.with(this@DetailActivity).load("https://image.tmdb.org/t/p/w185${member.profile_path}").into(view.findViewById(R.id.imgDirector))
                    view.setOnClickListener { openPersonMovies(member.id, member.name) }
                    containerCrew.addView(view)
                }
            }
        }
    }

    private fun loadSagaIfAvailable(movieId: Int) {
        lifecycleScope.launch {
            val details = repository.getMovieDetails(movieId)
            details?.belongs_to_collection?.id?.let { collectionId ->
                val sagaMovies = repository.getCollectionMovies(collectionId).filter { it.id != movieId }
                if (sagaMovies.isNotEmpty()) {
                    findViewById<View>(R.id.cardSaga)?.visibility = View.VISIBLE
                    val recycler: RecyclerView = findViewById(R.id.recyclerSagaMovies)
                    recycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                    recycler.adapter = MovieHorizontalAdapter(sagaMovies).apply {
                        onItemClick = { refreshDetailWithMovie(it) }
                    }
                }
            }
        }
    }

    private fun loadRecommendations(movieId: Int) {
        lifecycleScope.launch {
            val recs = repository.getMovieRecommendations(movieId).filter { it.posterPath != null }
            if (recs.isNotEmpty()) {
                val recycler: RecyclerView = findViewById(R.id.recyclerSimilarMovies)
                recycler.layoutManager = LinearLayoutManager(this@DetailActivity, LinearLayoutManager.HORIZONTAL, false)
                recycler.adapter = MovieHorizontalAdapter(recs).apply {
                    onItemClick = { refreshDetailWithMovie(it) }
                }
            }
        }
    }

    private fun openPersonMovies(id: Int, name: String) {
        startActivity(Intent(this, PersonMoviesActivity::class.java).apply {
            putExtra("personId", id)
            putExtra("personName", name)
        })
    }

    private fun refreshDetailWithMovie(movie: Movie) {
        startActivity(Intent(this, DetailActivity::class.java).apply { putExtra("movie", movie) })
        finish()
    }

    inner class WatchProviderAdapter(private val providers: List<WatchProviderItem>) : RecyclerView.Adapter<WatchProviderAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val img: ImageView = view.findViewById(R.id.imgProviderLogo)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_watch_provider, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.context).load("https://image.tmdb.org/t/p/w154${providers[position].logo_path}").into(holder.img)
        }
        override fun getItemCount() = providers.size
    }

    inner class PosterPagerAdapter(private val posterUrls: List<String>) : RecyclerView.Adapter<PosterPagerAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.imgPosterSlide)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_poster_slide, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.context).load(posterUrls[position]).fitCenter().into(holder.imageView)
        }
        override fun getItemCount() = posterUrls.size
    }

    inner class ThumbnailAdapter(
        private val urls: List<String>,
        private val onThumbClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ThumbnailAdapter.ViewHolder>() {
        private var selectedPos = 0
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: MaterialCardView = view.findViewById(R.id.cardThumbnail)
            val img: ImageView = view.findViewById(R.id.imgThumbnail)
            val overlay: View = view.findViewById(R.id.viewOverlay)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_poster_thumbnail, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            Glide.with(holder.itemView.context).load(urls[position]).centerCrop().into(holder.img)
            val isSelected = position == selectedPos
            holder.overlay.visibility = if (isSelected) View.GONE else View.VISIBLE
            holder.card.strokeColor = if (isSelected) getColor(R.color.primary) else getColor(android.R.color.transparent)
            holder.itemView.setOnClickListener { onThumbClick(position) }
        }
        override fun getItemCount() = urls.size
        fun setSelected(position: Int) {
            val old = selectedPos
            selectedPos = position
            notifyItemChanged(old)
            notifyItemChanged(selectedPos)
        }
    }
}
