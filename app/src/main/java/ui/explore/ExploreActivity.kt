package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.vexo.app.R
import data.repository.TMDBRepository
import ui.detail.DetailActivity
import ui.recommendation.RecommendationActivity
import ui.search.SearchActivity

class ExploreActivity : AppCompatActivity() {

    private val viewModel: ExploreViewModel by viewModels()
    private val repository = TMDBRepository.getInstance()
    
    private lateinit var recyclerMovies: RecyclerView
    private lateinit var recyclerTVShows: RecyclerView
    private lateinit var movieAdapter: CategoryAdapter
    private lateinit var tvAdapter: CategoryAdapter
    private lateinit var tabLayout: TabLayout

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
        
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showSettingsMenu()
        }

        findViewById<View>(R.id.btnRecommend).setOnClickListener {
            startActivity(Intent(this, RecommendationActivity::class.java))
        }
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
        // Refresh strings in case of language change
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

    private fun showSettingsMenu() {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_explore_menu, null)

        view.findViewById<TextView>(R.id.textOptionLanguage).text = getString(R.string.change_language)
        view.findViewById<TextView>(R.id.textOptionAbout).text = getString(R.string.about_us)

        view.findViewById<View>(R.id.optionLanguage).setOnClickListener {
            bottomSheet.dismiss()
            showLanguageDialog()
        }

        view.findViewById<View>(R.id.optionAbout).setOnClickListener {
            bottomSheet.dismiss()
            try {
                startActivity(Intent(this, AboutActivity::class.java))
            } catch (e: Exception) {}
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Español", "English")
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        val currentLang = if (currentLocales.toLanguageTags().contains("es")) 0 else 1

        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languages, currentLang) { dialog, which ->
                val langTag = if (which == 0) "es" else "en"
                
                if ((which == 0 && !currentLocales.toLanguageTags().contains("es")) || 
                    (which == 1 && !currentLocales.toLanguageTags().contains("en"))) {
                    
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langTag))
                    repository.clearCache()
                }
                dialog.dismiss()
            }
            .show()
    }
}
