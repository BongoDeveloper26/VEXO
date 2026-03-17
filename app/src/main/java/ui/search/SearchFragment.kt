package ui.search

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.vexo.app.R
import data.model.Movie
import data.repository.TMDBRepository
import data.repository.PersonDTO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.detail.DetailActivity
import ui.explore.MovieAdapter
import ui.explore.MovieHorizontalAdapter

class SearchFragment : Fragment() {

    private val repository = TMDBRepository.getInstance()
    private lateinit var movieAdapter: MovieAdapter
    private lateinit var personAdapter: PersonSearchAdapter
    private lateinit var trendingAdapter: MovieHorizontalAdapter
    
    private var searchJob: Job? = null
    private val PREFS_NAME = "search_prefs"
    private val KEY_HISTORY = "search_history"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI(view)
        loadTrendingInitial(view)
        updateHistoryList(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { updateHistoryList(it) }
    }

    private fun setupUI(view: View) {
        val editSearch: EditText = view.findViewById(R.id.editSearch)
        val btnClear: ImageButton = view.findViewById(R.id.btnClearSearch)
        val btnBack: ImageButton = view.findViewById(R.id.btnBackSearch)
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayoutSearch)
        val recyclerResults: RecyclerView = view.findViewById(R.id.recyclerSearchResults)

        btnBack.setOnClickListener {
            if (editSearch.text.isNotEmpty()) {
                editSearch.text.clear()
                showInitialState(view)
            } else {
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }

        movieAdapter = MovieAdapter(emptyList())
        movieAdapter.onItemClick = { movie ->
            val query = editSearch.text.toString().trim()
            if (query.isNotEmpty()) saveSearchQuery(query)
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        
        personAdapter = PersonSearchAdapter(emptyList())
        
        recyclerResults.layoutManager = LinearLayoutManager(requireContext())
        recyclerResults.adapter = movieAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> recyclerResults.adapter = movieAdapter
                    1 -> recyclerResults.adapter = personAdapter
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnClear.setOnClickListener {
            editSearch.text.clear()
            showInitialState(view)
        }

        view.findViewById<TextView>(R.id.btnClearAllHistory).setOnClickListener {
            clearAllHistory(view)
        }

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                btnClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                if (query.isNotEmpty()) {
                    tabLayout.visibility = View.VISIBLE
                    view.findViewById<View>(R.id.layoutInitialState).visibility = View.GONE
                    performSearch(query, view)
                } else {
                    showInitialState(view)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String, view: View) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            delay(300) // Búsqueda más instantánea
            showLoading(view, true)
            
            // 1. Buscar películas
            val movies = repository.searchMovies(query).filter { it.posterPath != null }
            movieAdapter.updateMovies(movies)
            
            val smartPeopleMap = mutableMapOf<Int, PersonDTO>()
            
            // 2. Buscar por nombre (Prioridad para que aparezca "Ben Affleck" al poner "Ben")
            repository.searchPeople(query).forEach { p ->
                smartPeopleMap[p.id] = p
            }
            
            // 3. Lógica Multifilm: Analizar las 5 pelis más relevantes para el reparto
            if (movies.isNotEmpty()) {
                val topMovies = movies.take(5)
                topMovies.forEach { movie ->
                    val credits = repository.getMovieCredits(movie.id)
                    credits?.cast?.forEach { cast ->
                        // Si el personaje o el actor coinciden con la búsqueda, lo añadimos
                        if (cast.character.contains(query, ignoreCase = true) || 
                            cast.name.contains(query, ignoreCase = true) ||
                            cast.character.contains("Bruce Wayne", ignoreCase = true)) {
                            smartPeopleMap[cast.id] = PersonDTO(cast.id, cast.name, cast.profile_path, cast.character)
                        }
                    }
                }
            }
            
            // Ordenar para que los que tienen foto salgan primero
            val finalPeople = smartPeopleMap.values.toList().sortedByDescending { it.profile_path != null }
            personAdapter.updatePeople(finalPeople)
            
            showLoading(view, false)
            view.findViewById<View>(R.id.recyclerSearchResults).visibility = View.VISIBLE
        }
    }

    private fun saveSearchQuery(query: String) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val history = getSearchHistory().toMutableList()
        history.remove(query)
        history.add(0, query)
        prefs.edit().putString(KEY_HISTORY, history.take(15).joinToString("|")).apply()
    }

    private fun getSearchHistory(): List<String> = requireContext()
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_HISTORY, "")?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()

    private fun clearAllHistory(view: View) {
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        updateHistoryList(view)
    }

    private fun deleteSingleHistoryItem(query: String, view: View) {
        val history = getSearchHistory().toMutableList()
        history.remove(query)
        requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_HISTORY, history.joinToString("|")).apply()
        updateHistoryList(view)
    }

    private fun updateHistoryList(view: View) {
        val container: LinearLayout = view.findViewById(R.id.containerHistoryItems)
        container.removeAllViews()
        val history = getSearchHistory()
        view.findViewById<View>(R.id.layoutHistory).visibility = if (history.isNotEmpty()) View.VISIBLE else View.GONE
        
        history.forEach { query ->
            val row = LinearLayout(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(48, 20, 24, 20)
                background = requireContext().getDrawable(android.R.drawable.list_selector_background)
                isClickable = true
                setOnClickListener {
                    val edit = view.findViewById<EditText>(R.id.editSearch)
                    edit.setText(query)
                    edit.setSelection(query.length)
                    performSearch(query, view)
                }
            }

            val icon = ImageView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(36, 36)
                setImageResource(android.R.drawable.ic_menu_recent_history)
                imageTintList = ColorStateList.valueOf(requireContext().getColor(R.color.text_secondary))
                alpha = 0.4f
            }

            val text = TextView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                setPadding(32, 0, 0, 0)
                this.text = query
                setTextColor(requireContext().getColor(R.color.text_primary))
                textSize = 15f
            }

            val deleteBtn = ImageButton(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(80, 80)
                setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                background = requireContext().getDrawable(android.R.drawable.list_selector_background)
                imageTintList = ColorStateList.valueOf(requireContext().getColor(R.color.text_secondary))
                alpha = 0.4f
                setPadding(15, 15, 15, 15)
                setOnClickListener { deleteSingleHistoryItem(query, view) }
            }

            row.addView(icon)
            row.addView(text)
            row.addView(deleteBtn)
            container.addView(row)
        }
    }

    private fun loadTrendingInitial(view: View) {
        val recycler: RecyclerView = view.findViewById(R.id.recyclerInitial)
        lifecycleScope.launch {
            val trending = repository.getTrendingMovies()
            trendingAdapter = MovieHorizontalAdapter(trending.filter { it.posterPath != null })
            trendingAdapter.onItemClick = { movie ->
                val intent = Intent(requireContext(), DetailActivity::class.java)
                intent.putExtra("movie", movie)
                startActivity(intent)
            }
            recycler.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            recycler.adapter = trendingAdapter
        }
    }

    private fun showLoading(view: View, show: Boolean) {
        view.findViewById<ProgressBar>(R.id.progressSearch).visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showInitialState(view: View) {
        view.findViewById<View>(R.id.tabLayoutSearch).visibility = View.GONE
        view.findViewById<View>(R.id.recyclerSearchResults).visibility = View.GONE
        view.findViewById<View>(R.id.layoutInitialState).visibility = View.VISIBLE
        updateHistoryList(view)
        showLoading(view, false)
    }
}
