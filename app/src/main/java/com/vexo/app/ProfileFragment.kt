package com.vexo.app

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import data.model.Movie
import data.repository.WatchlistRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.detail.DetailActivity

class ProfileFragment : Fragment() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var imgSlots: List<ImageView>
    private lateinit var cardSlots: List<MaterialCardView>
    private var activityAdapter: RecentActivityAdapter? = null
    private var diaryAdapter: DiaryAdapter? = null
    private var reviewAdapter: DiaryAdapter? = null
    private lateinit var imgProfile: ImageView
    private lateinit var imgHeaderBackground: ImageView
    private lateinit var imgContentBackground: ImageView
    private lateinit var viewBackgroundOverlay: View
    private lateinit var layoutContentProfile: LinearLayout
    private lateinit var scrollViewProfile: ScrollView
    private lateinit var textUserName: TextView
    private lateinit var textUserEmail: TextView
    
    private lateinit var dynamicTexts: List<TextView>
    private lateinit var dynamicSecondaryTexts: List<TextView>
    private lateinit var dynamicIcons: List<ImageView>
    private lateinit var dynamicCards: List<MaterialCardView>
    private lateinit var dynamicSeparators: List<View>
    private lateinit var btnViewAllList: List<TextView>

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
                checkNewAchievements()
            }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        watchlistRepository = WatchlistRepository(requireContext())
        auth = FirebaseAuth.getInstance()

        setupUI(view)
        loadProfileImage()
        loadBackgrounds()
        loadUserName()
        loadUserEmail()
        loadVitrina()
        loadRecentActivity(view)
        loadMyReviews(view)
        loadDiary(view)
        updateStats(view)
        
        return view
    }

    override fun onResume() {
        super.onResume()
        loadProfileImage()
        loadBackgrounds()
        loadUserName()
        loadUserEmail()
        loadVitrina()
        view?.let {
            loadRecentActivity(it)
            loadMyReviews(it)
            loadDiary(it)
            updateStats(it)
        }
    }

    private fun setupUI(view: View) {
        imgProfile = view.findViewById(R.id.imgProfile)
        imgHeaderBackground = view.findViewById(R.id.imgProfileHeaderBackground)
        imgContentBackground = view.findViewById(R.id.imgProfileContentBackground)
        viewBackgroundOverlay = view.findViewById(R.id.viewBackgroundOverlay)
        layoutContentProfile = view.findViewById(R.id.layoutContentProfile)
        scrollViewProfile = view.findViewById(R.id.scrollViewProfile)
        
        textUserName = view.findViewById(R.id.textUserNameProfile)
        textUserEmail = view.findViewById(R.id.textUserEmailProfile)
        val cardProfileImage: MaterialCardView = view.findViewById(R.id.cardProfileImage)
        val btnEditName: ImageButton = view.findViewById(R.id.btnEditName)
        
        val btnChangeBackground: View = view.findViewById(R.id.btnChangeBackground)
        val containerShare: View = view.findViewById(R.id.btnShareProfile)
        val btnShareIcon: View = view.findViewById(R.id.imgShareProfile)
        val btnAchievements: View = view.findViewById(R.id.btnAchievements)
        
        scrollViewProfile.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (imgContentBackground.visibility == View.VISIBLE) {
                imgContentBackground.translationY = -scrollY * 0.15f
            }
        }

        dynamicTexts = listOf(
            view.findViewById(R.id.textVitrinaTitle),
            view.findViewById(R.id.textActivityTitle),
            view.findViewById(R.id.textReviewsTitle),
            view.findViewById(R.id.textDiaryTitle),
            view.findViewById(R.id.textMoviesCount),
            view.findViewById(R.id.textAverageRating)
        )
        
        dynamicSecondaryTexts = listOf(
            view.findViewById(R.id.textVitrinaSub),
            view.findViewById(R.id.textActivitySub),
            view.findViewById(R.id.textReviewsSub),
            view.findViewById(R.id.textDiarySub),
            view.findViewById(R.id.textMoviesLabel),
            view.findViewById(R.id.textRatingLabel)
        )
        
        dynamicIcons = listOf(
            view.findViewById(R.id.iconVitrina),
            view.findViewById(R.id.iconActivity),
            view.findViewById(R.id.iconReviews),
            view.findViewById(R.id.iconDiary)
        )
        
        dynamicCards = listOf(
            view.findViewById(R.id.cardHeaderVitrina),
            view.findViewById(R.id.cardHeaderActivity),
            view.findViewById(R.id.cardHeaderReviews),
            view.findViewById(R.id.cardHeaderDiary),
            view.findViewById(R.id.cardStats)
        )
        
        dynamicSeparators = listOf(
            view.findViewById(R.id.sep1),
            view.findViewById(R.id.sep2),
            view.findViewById(R.id.sep3),
            view.findViewById(R.id.sepStats),
            view.findViewById(R.id.sepFinal)
        )
        
        btnViewAllList = listOf(
            view.findViewById(R.id.btnViewAllRated),
            view.findViewById(R.id.btnViewAllReviews),
            view.findViewById(R.id.btnViewAllDiary)
        )

        cardProfileImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        btnEditName.setOnClickListener { showEditNameDialog() }
        btnChangeBackground.setOnClickListener { showEnhancedBackgroundOptions() }
        
        val shareAction = View.OnClickListener { shareProfile() }
        containerShare.setOnClickListener(shareAction)
        btnShareIcon.setOnClickListener(shareAction)
        btnAchievements.setOnClickListener { startActivity(Intent(requireContext(), AchievementsActivity::class.java)) }

        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
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
        
        view.findViewById<TextView>(R.id.btnViewAllReviews).setOnClickListener {
            val intent = Intent(requireContext(), DiaryActivity::class.java)
            intent.putExtra("ONLY_REVIEWS", true)
            startActivity(intent)
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
                if (movie != null) showMovieOptionsBottomSheet(index, movie)
                else showMoviePickerDialog(index)
            }
        }

        view.findViewById<RecyclerView>(R.id.recyclerRecentActivity).layoutManager = 
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        
        view.findViewById<RecyclerView>(R.id.recyclerProfileReviews).apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        view.findViewById<RecyclerView>(R.id.recyclerDiary).apply {
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun showEnhancedBackgroundOptions() {
        val dialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.layout_background_selection, null)
        
        // Configurar Switch de Transparencia de Cabecera
        val switchTransparent = bottomSheetView.findViewById<SwitchMaterial>(R.id.switchHeaderTransparent)
        switchTransparent.isChecked = watchlistRepository.isHeaderTransparent()
        switchTransparent.setOnCheckedChangeListener { _, isChecked ->
            watchlistRepository.setHeaderTransparent(isChecked)
            loadBackgrounds()
        }

        // Configurar Colores de Cabecera
        val layoutColors = bottomSheetView.findViewById<LinearLayout>(R.id.layoutHeaderColors)
        val colors = listOf(
            null, // Predeterminado
            "#7C3AED", // Violeta VEXO
            "#FF71CE", // Vaporwave Pink
            "#01CDFE", // Vaporwave Blue
            "#05FFA1", // Neon Green
            "#B967FF", // Matte Purple
            "#FFFB96", // Pastel Yellow
            "#FF4500", // Sunset Orange
            "#FF1493", // Deep Pink
            "#00FA9A", // Spring Green
            "#2196F3", // Blue Material
            "#000000", // Negro Elegante
            "#FFFFFF"  // Blanco Puro
        )

        val currentColor = watchlistRepository.getHeaderColor()
        val density = resources.displayMetrics.density

        colors.forEach { colorHex ->
            val colorView = FrameLayout(requireContext())
            val params = LinearLayout.LayoutParams((46 * density).toInt(), (46 * density).toInt())
            params.setMargins((8 * density).toInt(), 0, (8 * density).toInt(), 0)
            colorView.layoutParams = params

            val circle = View(requireContext())
            val circleParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
            circle.layoutParams = circleParams
            
            val shape = GradientDrawable()
            shape.shape = GradientDrawable.OVAL
            if (colorHex == null) {
                val gradient = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(
                    Color.parseColor("#1E1B4B"),
                    Color.parseColor("#4C1D95"),
                    Color.parseColor("#7C3AED")
                ))
                gradient.shape = GradientDrawable.OVAL
                circle.background = gradient
            } else {
                shape.setColor(Color.parseColor(colorHex))
                circle.background = shape
            }
            
            if (colorHex == currentColor) {
                val stroke = GradientDrawable()
                stroke.shape = GradientDrawable.OVAL
                stroke.setStroke((3 * density).toInt(), ContextCompat.getColor(requireContext(), R.color.primary))
                colorView.background = stroke
                colorView.setPadding((4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt(), (4 * density).toInt())
            }

            colorView.addView(circle)
            colorView.setOnClickListener {
                watchlistRepository.setHeaderColor(colorHex)
                loadBackgrounds()
                dialog.dismiss()
            }
            layoutColors.addView(colorView)
        }

        val recycler = bottomSheetView.findViewById<RecyclerView>(R.id.recyclerBackgroundOptions)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        
        val options = listOf(
            BackgroundOption(null, "Predeterminado", 0),
            BackgroundOption("fondo_vexocine", "VEXO Cine", R.drawable.fondo_vexocine),
            BackgroundOption("fondo_futurista", "Futurista", R.drawable.fondo_futurista),
            BackgroundOption("fondo_espacio", "Espacio", R.drawable.fondo_espacio),
            BackgroundOption("fondo_salacine", "Sala Cine", R.drawable.fondo_salacine),
            BackgroundOption("fondo_cineclasico", "Cine Clásico", R.drawable.fondo_cineclasico),
            BackgroundOption("fondo_vaporwave", "Vaporwave", R.drawable.fondo_vaporwave),
            BackgroundOption("fondo_playa", "Playa", R.drawable.fondo_playa),
            BackgroundOption("fondo_callejerocine", "Callejero Cine", R.drawable.fondo_callejerocine)
        )
        
        val currentBg = watchlistRepository.getHeaderBackground()
        
        recycler.adapter = BackgroundAdapter(options, currentBg) { selected ->
            watchlistRepository.setHeaderBackground(selected.id)
            loadBackgrounds()
            dialog.dismiss()
        }
        
        dialog.setContentView(bottomSheetView)
        dialog.show()
    }

    private fun loadBackgrounds() {
        val bgName = watchlistRepository.getHeaderBackground()
        val isTransparent = watchlistRepository.isHeaderTransparent()
        val customColor = watchlistRepository.getHeaderColor()
        
        val headerShape = GradientDrawable()
        val cornerRadius = 100f * resources.displayMetrics.density
        headerShape.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, cornerRadius, cornerRadius, cornerRadius, cornerRadius)

        if (customColor != null) {
            imgHeaderBackground.setImageResource(0)
            headerShape.setColor(Color.parseColor(customColor))
            imgHeaderBackground.background = headerShape
        } else {
            imgHeaderBackground.setImageResource(R.drawable.bg_profile_header)
            imgHeaderBackground.background = null 
        }
        
        imgHeaderBackground.alpha = if (isTransparent) 0.5f else 1.0f

        when (bgName) {
            "fondo_vexocine" -> setThemeConfig(R.drawable.fondo_vexocine, "#7C3AED", "#CC1A1A1A", "#407C3AED")
            "fondo_futurista" -> setThemeConfig(R.drawable.fondo_futurista, "#00E5FF", "#CC1A1A1A", "#3300E5FF")
            "fondo_espacio" -> setThemeConfig(R.drawable.fondo_espacio, "#B0E0E6", "#CC0B1026", "#40B0E0E6")
            "fondo_salacine" -> setThemeConfig(R.drawable.fondo_salacine, "#FFD700", "#CC2B0000", "#40FFD700")
            "fondo_cineclasico" -> setThemeConfig(R.drawable.fondo_cineclasico, "#D2B48C", "#CC1A110D", "#40D2B48C")
            "fondo_vaporwave" -> setThemeConfig(R.drawable.fondo_vaporwave, "#FF71CE", "#CC2D1B4B", "#40FF71CE")
            "fondo_playa" -> setThemeConfig(R.drawable.fondo_playa, "#00BCD4", "#CC002F2F", "#4000BCD4")
            "fondo_callejerocine" -> setThemeConfig(R.drawable.fondo_callejerocine, "#FF9800", "#CC1A1A1A", "#40FF9800")
            else -> {
                imgContentBackground.visibility = View.GONE
                viewBackgroundOverlay.visibility = View.GONE
                layoutContentProfile.setBackgroundResource(R.drawable.bg_profile_content_gradient)
                scrollViewProfile.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_app))
                applyDefaultColors()
            }
        }
    }

    private fun setThemeConfig(resId: Int, accent: String, cardBg: String, stroke: String) {
        imgContentBackground.visibility = View.VISIBLE
        imgContentBackground.setImageResource(resId)
        imgContentBackground.alpha = 1.0f // Revertido a opacidad total
        
        viewBackgroundOverlay.visibility = View.VISIBLE
        viewBackgroundOverlay.setBackgroundColor(Color.parseColor("#99000000")) // Revertido a opacidad original

        layoutContentProfile.setBackgroundResource(0)
        scrollViewProfile.setBackgroundColor(Color.TRANSPARENT)
        applyThemedColors(Color.parseColor(accent), Color.parseColor(cardBg), Color.parseColor(stroke))
    }

    private fun applyThemedColors(accentColor: Int, cardBgColor: Int, strokeColor: Int) {
        val pureWhite = Color.WHITE
        val separatorColor = Color.parseColor("#40FFFFFF")

        dynamicTexts.forEach { it.setTextColor(accentColor) }
        dynamicSecondaryTexts.forEach { 
            it.setTextColor(pureWhite)
            it.alpha = 0.9f
        }
        dynamicIcons.forEach { it.imageTintList = ColorStateList.valueOf(accentColor) }
        dynamicCards.forEach { card ->
            card.setCardBackgroundColor(cardBgColor)
            card.strokeColor = strokeColor
            card.strokeWidth = 2
        }
        dynamicSeparators.forEach { it.setBackgroundColor(separatorColor) }
        btnViewAllList.forEach { it.setTextColor(accentColor) }
        
        activityAdapter?.updateTheme(true, accentColor)
        diaryAdapter?.updateTheme(true, accentColor, cardBgColor, strokeColor)
        reviewAdapter?.updateTheme(true, accentColor, cardBgColor, strokeColor)
    }

    private fun applyDefaultColors() {
        val primary = ContextCompat.getColor(requireContext(), R.color.primary)
        val textSecondary = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        val separator = Color.parseColor("#12000000")

        dynamicTexts.forEach { it.setTextColor(primary) }
        dynamicSecondaryTexts.forEach { 
            it.setTextColor(textSecondary)
            it.alpha = 0.85f
        }
        dynamicIcons.forEach { it.imageTintList = ColorStateList.valueOf(primary) }
        dynamicCards.forEach { card ->
            if (card.id == R.id.cardStats) {
                card.setCardBackgroundColor(Color.WHITE)
                card.strokeColor = Color.parseColor("#08000000")
            } else {
                card.setCardBackgroundColor(Color.parseColor("#0A7C3AED"))
                card.strokeColor = Color.parseColor("#157C3AED")
            }
            card.strokeWidth = 1
        }
        dynamicSeparators.forEach { it.setBackgroundColor(separator) }
        btnViewAllList.forEach { it.setTextColor(primary) }

        activityAdapter?.updateTheme(false)
        diaryAdapter?.updateTheme(false)
        reviewAdapter?.updateTheme(false)
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
            updateAdapterThemes()
            view.findViewById<RecyclerView>(R.id.recyclerRecentActivity).adapter = activityAdapter
        } else {
            layoutActivity.visibility = View.GONE
        }
    }

    private fun loadMyReviews(view: View) {
        val allEntries = watchlistRepository.getDiary()
        val reviewEntries = allEntries.filter { !it.review.isNullOrEmpty() }.take(3)
        val layoutReviews = view.findViewById<View>(R.id.layoutMyReviews)
        if (reviewEntries.isNotEmpty()) {
            layoutReviews.visibility = View.VISIBLE
            reviewAdapter = DiaryAdapter(reviewEntries, showTimeline = false, isFavorite = { movieId ->
                watchlistRepository.isFavorite(movieId)
            }) { entry ->
                val movieToOpen = entry.movie ?: watchlistRepository.getAllRatedMovies().find { it.id == entry.movieId }
                if (movieToOpen != null) {
                    val intent = Intent(requireContext(), DetailActivity::class.java)
                    intent.putExtra("movie", movieToOpen)
                    startActivity(intent)
                }
            }
            updateAdapterThemes()
            view.findViewById<RecyclerView>(R.id.recyclerProfileReviews).adapter = reviewAdapter
        } else {
            layoutReviews.visibility = View.GONE
        }
    }

    private fun loadDiary(view: View) {
        val diaryEntries = watchlistRepository.getDiary().take(3)
        val layoutDiary = view.findViewById<View>(R.id.layoutDiary)
        if (diaryEntries.isNotEmpty()) {
            layoutDiary.visibility = View.VISIBLE
            diaryAdapter = DiaryAdapter(diaryEntries, showTimeline = true, isFavorite = { movieId ->
                watchlistRepository.isFavorite(movieId)
            }) { entry ->
                val movieToOpen = entry.movie ?: watchlistRepository.getAllRatedMovies().find { it.id == entry.movieId }
                if (movieToOpen != null) {
                    val intent = Intent(requireContext(), DetailActivity::class.java)
                    intent.putExtra("movie", movieToOpen)
                    startActivity(intent)
                }
            }
            updateAdapterThemes()
            view.findViewById<RecyclerView>(R.id.recyclerDiary).adapter = diaryAdapter
        } else {
            layoutDiary.visibility = View.GONE
        }
    }

    private fun updateAdapterThemes() {
        val bgName = watchlistRepository.getHeaderBackground()
        when (bgName) {
            "fondo_vexocine" -> applyThemedColors(Color.parseColor("#7C3AED"), Color.parseColor("#CC1A1A1A"), Color.parseColor("#407C3AED"))
            "fondo_futurista" -> applyThemedColors(Color.parseColor("#00E5FF"), Color.parseColor("#CC1A1A1A"), Color.parseColor("#3300E5FF"))
            "fondo_espacio" -> applyThemedColors(Color.parseColor("#B0E0E6"), Color.parseColor("#CC0B1026"), Color.parseColor("#40B0E0E6"))
            "fondo_salacine" -> applyThemedColors(Color.parseColor("#FFD700"), Color.parseColor("#CC2B0000"), Color.parseColor("#40FFD700"))
            "fondo_cineclasico" -> applyThemedColors(Color.parseColor("#D2B48C"), Color.parseColor("#CC1A110D"), Color.parseColor("#40D2B48C"))
            "fondo_vaporwave" -> applyThemedColors(Color.parseColor("#FF71CE"), Color.parseColor("#CC2D1B4B"), Color.parseColor("#40FF71CE"))
            "fondo_playa" -> applyThemedColors(Color.parseColor("#00BCD4"), Color.parseColor("#CC002F2F"), Color.parseColor("#4000BCD4"))
            "fondo_callejerocine" -> applyThemedColors(Color.parseColor("#FF9800"), Color.parseColor("#CC1A1A1A"), Color.parseColor("#40FF9800"))
            else -> applyDefaultColors()
        }
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
            }
        }
    }

    private fun loadProfileImage() {
        val uriString = watchlistRepository.getProfileImageUri()
        if (uriString != null) {
            imgProfile.setPadding(0, 0, 0, 0)
            imgProfile.imageTintList = null
            imgProfile.alpha = 1.0f
            Glide.with(this).load(Uri.parse(uriString)).centerCrop().placeholder(R.drawable.ic_nav_profile).into(imgProfile)
        }
    }

    private fun loadUserName() { textUserName.text = watchlistRepository.getUserName() }
    private fun loadUserEmail() { textUserEmail.text = auth.currentUser?.email ?: getString(R.string.no_email_linked) }
    private fun updateStats(view: View) {
        val stats = watchlistRepository.getStats()
        view.findViewById<TextView>(R.id.textMoviesCount).text = stats.totalMovies.toString()
        view.findViewById<TextView>(R.id.textAverageRating).text = String.format("%.1f", stats.averageRating)
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
        }
        dialog.setContentView(menuView)
        dialog.show()
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
                checkNewAchievements()
            }.show()
    }

    private fun showEditNameDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Cambiar nombre")
        val input = EditText(requireContext())
        input.filters = arrayOf(InputFilter.LengthFilter(15))
        input.setText(textUserName.text.toString())
        input.isSingleLine = true
        val container = LinearLayout(requireContext())
        val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        val margin = (24 * resources.displayMetrics.density).toInt()
        lp.setMargins(margin, 0, margin, 0)
        input.layoutParams = lp
        container.addView(input)
        builder.setView(container)
        builder.setPositiveButton("Guardar") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                watchlistRepository.setUserName(newName)
                textUserName.text = newName
                val user = auth.currentUser
                val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(newName).build()
                user?.updateProfile(profileUpdates)
            }
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun shareProfile() {
        val name = watchlistRepository.getUserName()
        val stats = watchlistRepository.getStats()
        val profileLink = "vexo://profile/${watchlistRepository.userId}"
        val shareText = "🎬 Perfil de $name en VEXO\n📊 Películas: ${stats.totalMovies}\n⭐ Nota: ${String.format("%.1f", stats.averageRating)}\n🔗 $profileLink"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Compartir perfil"))
    }

    private fun checkNewAchievements() {
        val allAchievements = AchievementsActivity.getAchievements(watchlistRepository)
        val seenCount = watchlistRepository.getSeenAchievementsCount()
        val completedAchievements = allAchievements.filter { it.currentProgress >= it.maxProgress }
        
        if (completedAchievements.size > seenCount) {
            val newAchievement = completedAchievements.getOrNull(seenCount)
            if (newAchievement != null) {
                showAchievementNotification(newAchievement)
            }
            watchlistRepository.setSeenAchievementsCount(completedAchievements.size)
        }
    }

    private fun showAchievementNotification(achievement: Achievement) {
        val rootLayout = activity?.findViewById<ViewGroup>(android.R.id.content) ?: return
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
}
