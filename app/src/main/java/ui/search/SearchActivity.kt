package ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.vexo.app.R
import data.model.Movie
import data.repository.TMDBRepository
import data.repository.PersonDTO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ui.detail.DetailActivity
import ui.explore.MovieHorizontalAdapter

class SearchActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private lateinit var movieSearchAdapter: MovieSearchAdapter
    private lateinit var personAdapter: PersonSearchAdapter
    private lateinit var trendingAdapter: MovieHorizontalAdapter
    
    private var searchJob: Job? = null
    private val PREFS_NAME = "search_prefs"
    private val KEY_HISTORY = "search_history"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        setupUI()
        loadTrendingInitial()
        updateHistoryList()
    }

    override fun onResume() {
        super.onResume()
        updateHistoryList()
    }

    private fun setupUI() {
        val editSearch: EditText = findViewById(R.id.editSearch)
        val btnBack: ImageButton = findViewById(R.id.btnBackSearch)
        val btnClear: ImageButton = findViewById(R.id.btnClearSearch)
        val btnClearAll: TextView = findViewById(R.id.btnClearAllHistory)
        val tabLayout: TabLayout = findViewById(R.id.tabLayoutSearch)
        val recyclerResults: RecyclerView = findViewById(R.id.recyclerSearchResults)

        btnBack.setOnClickListener { finish() }

        movieSearchAdapter = MovieSearchAdapter(emptyList())
        movieSearchAdapter.onItemClick = { movie ->
            saveSearchQuery(movie.title)
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        
        personAdapter = PersonSearchAdapter(emptyList())
        recyclerResults.layoutManager = LinearLayoutManager(this)
        recyclerResults.adapter = movieSearchAdapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> recyclerResults.adapter = movieSearchAdapter
                    1 -> recyclerResults.adapter = personAdapter
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        btnClear.setOnClickListener {
            editSearch.text.clear()
            showInitialState()
        }

        btnClearAll.setOnClickListener { clearAllHistory() }

        editSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = editSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    saveSearchQuery(query)
                    performSearch(query)
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
                true
            } else false
        }

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                btnClear.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
                
                if (query.isNotEmpty()) {
                    findViewById<View>(R.id.layoutInitialState).visibility = View.GONE
                    tabLayout.visibility = View.VISIBLE
                    recyclerResults.visibility = View.VISIBLE
                    performSearch(query)
                } else {
                    showInitialState()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        
        findViewById<View>(R.id.layoutInitialState).visibility = View.GONE
        findViewById<View>(R.id.tabLayoutSearch).visibility = View.VISIBLE
        findViewById<View>(R.id.recyclerSearchResults).visibility = View.VISIBLE
        
        searchJob = lifecycleScope.launch {
            delay(300) 
            showLoading(true)
            
            val queryLower = query.lowercase().trim()
            val results = repository.searchAll(query).filter { it.posterPath != null }
            movieSearchAdapter.updateMovies(results)
            
            val smartPeopleMap = mutableMapOf<Int, PersonDTO>()
            repository.searchPeople(query).forEach { p ->
                if (p.profile_path != null) smartPeopleMap[p.id] = p
            }
            personAdapter.updatePeople(smartPeopleMap.values.toList())
            
            showLoading(false)
            findViewById<View>(R.id.layoutNoResults).visibility = if (results.isEmpty() && smartPeopleMap.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun saveSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val history = getSearchHistory().toMutableList()
        history.remove(trimmed)
        history.add(0, trimmed)
        prefs.edit().putString(KEY_HISTORY, history.take(15).joinToString("|")).apply()
    }

    private fun getSearchHistory(): List<String> = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        .getString(KEY_HISTORY, "")?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()

    private fun clearAllHistory() {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply()
        updateHistoryList()
    }

    private fun deleteSingleHistoryItem(query: String) {
        val history = getSearchHistory().toMutableList()
        history.remove(query)
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_HISTORY, history.joinToString("|")).apply()
        updateHistoryList()
    }

    private fun updateHistoryList() {
        val container: LinearLayout = findViewById(R.id.containerHistoryItems)
        container.removeAllViews()
        val history = getSearchHistory()
        val layoutHistory: View = findViewById(R.id.layoutHistory)
        
        if (history.isEmpty()) {
            layoutHistory.visibility = View.GONE
            return
        }
        
        layoutHistory.visibility = View.VISIBLE
        
        history.forEach { query ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_search_history, container, false)
            
            itemView.findViewById<TextView>(R.id.textHistoryQuery).text = query
            
            itemView.setOnClickListener {
                findViewById<EditText>(R.id.editSearch).setText(query)
                findViewById<EditText>(R.id.editSearch).setSelection(query.length)
                performSearch(query)
            }
            
            itemView.findViewById<ImageButton>(R.id.btnDeleteHistory).setOnClickListener {
                deleteSingleHistoryItem(query)
            }
            
            container.addView(itemView)
        }
    }

    private fun loadTrendingInitial() {
        val recyclerInitial: RecyclerView = findViewById(R.id.recyclerInitial)
        lifecycleScope.launch {
            val trending = repository.getTrendingMovies()
            trendingAdapter = MovieHorizontalAdapter(trending.filter { it.posterPath != null })
            trendingAdapter.onItemClick = { movie ->
                val intent = Intent(this@SearchActivity, DetailActivity::class.java)
                intent.putExtra("movie", movie)
                startActivity(intent)
            }
            recyclerInitial.layoutManager = LinearLayoutManager(this@SearchActivity, LinearLayoutManager.HORIZONTAL, false)
            recyclerInitial.adapter = trendingAdapter
        }
    }

    private fun showLoading(show: Boolean) {
        findViewById<ProgressBar>(R.id.progressSearch).visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showInitialState() {
        searchJob?.cancel()
        findViewById<View>(R.id.layoutInitialState).visibility = View.VISIBLE
        findViewById<View>(R.id.tabLayoutSearch).visibility = View.GONE
        findViewById<View>(R.id.recyclerSearchResults).visibility = View.GONE
        findViewById<View>(R.id.layoutNoResults).visibility = View.GONE
        updateHistoryList()
    }
}
