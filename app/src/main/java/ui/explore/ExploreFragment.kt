package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.vexo.app.R
import data.repository.TMDBRepository
import ui.detail.DetailActivity

class ExploreFragment : Fragment() {

    private val viewModel: ExploreViewModel by viewModels()
    private val repository = TMDBRepository.getInstance()
    
    private lateinit var recyclerMovies: RecyclerView
    private lateinit var recyclerTVShows: RecyclerView
    private lateinit var movieAdapter: CategoryAdapter
    private lateinit var tvAdapter: CategoryAdapter
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader(view)
        setupTabs(view)
        setupRecyclerViews(view)
        setupFab(view)
        observeViewModel()

        viewModel.loadAllCategories()
    }

    private fun setupHeader(view: View) {
        view.findViewById<ImageButton>(R.id.btnBack).visibility = View.GONE
        
        view.findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showSettingsMenu()
        }
    }

    private fun setupTabs(view: View) {
        tabLayout = view.findViewById(R.id.tabLayoutExplore)
        val isSpanish = repository.getLanguage() == "es-ES"
        
        tabLayout.getTabAt(0)?.text = if (isSpanish) "PELÍCULAS" else "MOVIES"
        tabLayout.getTabAt(1)?.text = if (isSpanish) "SERIES" else "SERIES"

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

    private fun setupRecyclerViews(view: View) {
        recyclerMovies = view.findViewById(R.id.recyclerMovies)
        recyclerTVShows = view.findViewById(R.id.recyclerTVShows)

        movieAdapter = CategoryAdapter(emptyList()) { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }

        tvAdapter = CategoryAdapter(emptyList()) { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }

        recyclerMovies.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = movieAdapter
        }

        recyclerTVShows.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tvAdapter
        }
    }

    private fun setupFab(view: View) {
        view.findViewById<View>(R.id.fabSearch).visibility = View.GONE
    }

    private fun observeViewModel() {
        viewModel.movieCategories.observe(viewLifecycleOwner) { categories ->
            movieAdapter.updateCategories(categories)
        }
        viewModel.tvCategories.observe(viewLifecycleOwner) { categories ->
            tvAdapter.updateCategories(categories)
        }
    }

    private fun showSettingsMenu() {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_explore_menu, null)
        val isSpanish = repository.getLanguage() == "es-ES"

        view.findViewById<TextView>(R.id.textOptionLanguage).text = if (isSpanish) "Cambiar Idioma" else "Change Language"
        view.findViewById<TextView>(R.id.textOptionAbout).text = if (isSpanish) "Quiénes Somos" else "About Us"

        view.findViewById<View>(R.id.optionLanguage).setOnClickListener {
            bottomSheet.dismiss()
            showLanguageDialog()
        }

        view.findViewById<View>(R.id.optionAbout).setOnClickListener {
            bottomSheet.dismiss()
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Español", "English")
        val currentLang = if (repository.getLanguage() == "es-ES") 0 else 1

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (repository.getLanguage() == "es-ES") "Seleccionar Idioma" else "Select Language")
            .setSingleChoiceItems(languages, currentLang) { dialog, which ->
                val newLang = if (which == 0) "es-ES" else "en-US"
                if (newLang != repository.getLanguage()) {
                    repository.setLanguage(newLang)
                    activity?.recreate()
                }
                dialog.dismiss()
            }
            .show()
    }
}
