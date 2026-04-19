package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.tabs.TabLayout
import com.google.android.material.button.MaterialButtonToggleGroup
import com.vexo.app.R
import data.repository.TMDBRepository
import ui.detail.DetailActivity
import ui.search.SearchActivity

class ExploreActivity : AppCompatActivity() {

    private val viewModel: ExploreViewModel by viewModels()
    private val repository = TMDBRepository.getInstance()
    
    private lateinit var recyclerMovies: RecyclerView
    private lateinit var recyclerTVShows: RecyclerView
    private lateinit var movieAdapter: CategoryAdapter
    private lateinit var tvAdapter: CategoryAdapter
    private lateinit var tabLayout: TabLayout

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
        setContentView(R.layout.activity_explore)

        setupHeader()
        setupTabs()
        setupRecyclerViews()
        setupFab()
        observeViewModel()

        viewModel.loadAllCategories()
    }

    private fun setupHeader() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Botón de ajustes eliminado ya que se movió al Perfil

        findViewById<View>(R.id.btnRecommend).setOnClickListener {
            showDiscoverFilters()
        }
    }

    private fun showDiscoverFilters() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_discover_filters, null)
        
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupGenres)
        val sliderYear = view.findViewById<Slider>(R.id.sliderYear)
        val sliderRating = view.findViewById<Slider>(R.id.sliderRating)
        val textYear = view.findViewById<TextView>(R.id.textYearValue)
        val textRating = view.findViewById<TextView>(R.id.textRatingValue)
        val toggleType = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleContentType)
        val btnApply = view.findViewById<Button>(R.id.btnApplyFilters)

        // Llenar géneros
        allGenresMap.forEach { (id, name) ->
            val chip = Chip(this).apply {
                text = name
                isCheckable = true
                tag = id
                setChipBackgroundColorResource(R.color.background_app)
            }
            chipGroup.addView(chip)
        }

        sliderYear.addOnChangeListener { _, value, _ -> 
            textYear.text = value.toInt().toString()
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

            val intent = Intent(this, DiscoverResultsActivity::class.java).apply {
                putIntegerArrayListExtra("genres", ArrayList(selectedGenres))
                putExtra("year", sliderYear.value.toInt())
                putExtra("rating", sliderRating.value)
                putExtra("isTv", toggleType.checkedButtonId == R.id.btnTypeTV)
            }
            startActivity(intent)
            dialog.dismiss()
        }

        dialog.setContentView(view)
        dialog.show()
    }

    private fun setupTabs() {
        tabLayout = findViewById(R.id.tabLayoutExplore)
        
        tabLayout.getTabAt(0)?.text = getString(R.string.movies)
        tabLayout.getTabAt(1)?.text = getString(R.string.tv_shows)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        recyclerMovies.visibility = View.VISIBLE
                        recyclerTVShows.visibility = View.GONE
                    }
                    1 -> {
                        recyclerMovies.visibility = View.GONE
                        recyclerTVShows.visibility = View.VISIBLE
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRecyclerViews() {
        recyclerMovies = findViewById(R.id.recyclerMovies)
        recyclerTVShows = findViewById(R.id.recyclerTVShows)

        movieAdapter = CategoryAdapter(emptyList()) { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }

        tvAdapter = CategoryAdapter(emptyList()) { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }

        recyclerMovies.apply {
            layoutManager = LinearLayoutManager(this@ExploreActivity)
            adapter = movieAdapter
        }

        recyclerTVShows.apply {
            layoutManager = LinearLayoutManager(this@ExploreActivity)
            adapter = tvAdapter
        }
    }

    private fun setupFab() {
        findViewById<View>(R.id.fabSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        tabLayout.getTabAt(0)?.text = getString(R.string.movies)
        tabLayout.getTabAt(1)?.text = getString(R.string.tv_shows)
    }

    private fun observeViewModel() {
        viewModel.movieCategories.observe(this) { categories ->
            movieAdapter.updateCategories(categories)
        }
        viewModel.tvCategories.observe(this) { categories ->
            tvAdapter.updateCategories(categories)
        }
    }
}
