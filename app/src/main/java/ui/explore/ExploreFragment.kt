package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.vexo.app.MainActivity
import com.vexo.app.R
import data.repository.TMDBRepository
import ui.detail.DetailActivity
import ui.recommendation.RecommendationActivity

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
        
        // El botón de ajustes se ha movido al Perfil

        // REVERTIDO: Abre directamente RecommendationActivity como antes
        view.findViewById<View>(R.id.btnRecommend).setOnClickListener {
            startActivity(Intent(requireContext(), RecommendationActivity::class.java))
        }
    }

    private fun setupTabs(view: View) {
        tabLayout = view.findViewById(R.id.tabLayoutExplore)
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
}
