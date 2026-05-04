package ui.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.chip.ChipGroup
import com.vexo.app.R
import data.api.NewsService
import data.model.NewsArticle
import data.repository.WatchlistRepository
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsFragment : Fragment() {

    private lateinit var recyclerNews: RecyclerView
    private lateinit var progressNews: ProgressBar
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var swipeRefreshNews: SwipeRefreshLayout
    private lateinit var searchNews: SearchView
    private lateinit var layoutEmptyNews: LinearLayout
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var watchlistRepository: WatchlistRepository

    private val movieDomains = "espinof.com,fotogramas.es,sensacine.com,ecartelera.com,elseptimoarte.net,cinematomania.es,decine21.com,accioncine.es,objetivocine.es"
    private var currentQuery: String = ""
    private var isSearching: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        watchlistRepository = WatchlistRepository(requireContext())
        
        recyclerNews = view.findViewById(R.id.recyclerNews)
        progressNews = view.findViewById(R.id.progressNews)
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters)
        swipeRefreshNews = view.findViewById(R.id.swipeRefreshNews)
        searchNews = view.findViewById(R.id.searchNews)
        layoutEmptyNews = view.findViewById(R.id.layoutEmptyNews)
        
        setupRecyclerView()
        setupFilters()
        setupSwipeRefresh()
        setupSearchView()
        
        // Carga inicial
        currentQuery = getFilterQuery(R.id.chipLatest)
        loadNews(currentQuery, isGeneral = true)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(
            onItemClick = { article ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                startActivity(intent)
            },
            onShareClick = { article ->
                shareArticle(article)
            },
            onBookmarkClick = { article, save ->
                watchlistRepository.toggleSaveNews(article)
                if (chipGroupFilters.checkedChipId == R.id.chipSaved) {
                    showSavedNews()
                }
            },
            isBookmarked = { article ->
                watchlistRepository.isNewsSaved(article.url)
            }
        )
        recyclerNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds.first()
                layoutEmptyNews.visibility = View.GONE
                
                if (chipId == R.id.chipSaved) {
                    isSearching = false
                    searchNews.setQuery("", false)
                    showSavedNews()
                } else {
                    isSearching = false
                    searchNews.setQuery("", false)
                    searchNews.clearFocus()
                    currentQuery = getFilterQuery(chipId)
                    loadNews(currentQuery, isGeneral = (chipId == R.id.chipLatest))
                }
            }
        }
    }

    private fun showSavedNews() {
        val saved = watchlistRepository.getSavedNews()
        newsAdapter.submitList(saved)
        if (saved.isEmpty()) {
            layoutEmptyNews.visibility = View.VISIBLE
        } else {
            layoutEmptyNews.visibility = View.GONE
        }
        swipeRefreshNews.isRefreshing = false
        progressNews.visibility = View.GONE
    }

    private fun setupSwipeRefresh() {
        swipeRefreshNews.setColorSchemeResources(R.color.primary)
        swipeRefreshNews.setOnRefreshListener {
            if (chipGroupFilters.checkedChipId == R.id.chipSaved) {
                showSavedNews()
            } else {
                loadNews(currentQuery, isGeneral = !isSearching && chipGroupFilters.checkedChipId == R.id.chipLatest)
            }
        }
    }

    private fun setupSearchView() {
        searchNews.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    isSearching = true
                    currentQuery = query
                    loadNews(query, isGeneral = false)
                    searchNews.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank() && isSearching) {
                    isSearching = false
                    val checkedId = chipGroupFilters.checkedChipId
                    if (checkedId == R.id.chipSaved) {
                        showSavedNews()
                    } else {
                        currentQuery = getFilterQuery(checkedId)
                        loadNews(currentQuery, isGeneral = (checkedId == R.id.chipLatest))
                    }
                }
                return true
            }
        })
    }

    private fun getFilterQuery(chipId: Int): String {
        val noiseExclusion = " -bolsa -acciones -política -economía -sucesos -videojuegos -gaming -smartphone -móvil -tecnología -fútbol -deporte -oferta -barato -amazon -unboxing -gadget -pc -laptop -iphone -samsung -android"
        
        return when (chipId) {
            R.id.chipPopular -> "(taquilla OR \"más vista\" OR \"éxito de crítica\" OR popular) AND (cine OR película OR actor OR actriz)$noiseExclusion"
            R.id.chipPremieres -> "(estreno OR cartelera OR tráiler OR \"nueva película\") AND (cine OR cines)$noiseExclusion"
            R.id.chipAwards -> "(Oscar OR Goya OR \"Cannes\" OR \"Golden Globes\" OR \"premios cine\")$noiseExclusion"
            R.id.chipSeries -> "(serie OR temporada OR Netflix OR \"HBO Max\" OR \"Disney Plus\" OR \"Amazon Prime\") AND NOT (película)$noiseExclusion"
            else -> "(cine OR película OR estreno OR tráiler)$noiseExclusion"
        }
    }

    private fun loadNews(query: String, isGeneral: Boolean) {
        if (!swipeRefreshNews.isRefreshing) progressNews.visibility = View.VISIBLE
        layoutEmptyNews.visibility = View.GONE
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val service = retrofit.create(NewsService::class.java)
        
        lifecycleScope.launch {
            try {
                val searchScope = if (isGeneral) null else "title"
                val domains = if (isGeneral) null else movieDomains
                val sortBy = if (isGeneral) "publishedAt" else "relevancy"

                val response = service.getMovieNews(
                    query = query,
                    searchIn = searchScope,
                    domains = domains,
                    sortBy = sortBy
                )
                
                if (response.isSuccessful) {
                    val articles = response.body()?.articles?.filter { 
                        it.urlToImage != null && !it.title.contains("[Removed]") &&
                        isValidMovieContent(it.title, it.description ?: "")
                    } ?: emptyList()
                    
                    newsAdapter.submitList(articles)
                    
                    if (articles.isEmpty()) {
                        if (!isGeneral && !isSearching) {
                            loadNews(query, isGeneral = true)
                        } else {
                            layoutEmptyNews.visibility = View.VISIBLE
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al cargar noticias", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Sin conexión", Toast.LENGTH_SHORT).show()
            } finally {
                progressNews.visibility = View.GONE
                swipeRefreshNews.isRefreshing = false
            }
        }
    }

    private fun isValidMovieContent(title: String, description: String): Boolean {
        val keywords = listOf("cine", "película", "estreno", "actor", "actriz", "director", "serie", "tráiler", "hollywood", "filme", "reparto", "taquilla", "oscar", "goya")
        val content = (title + description).lowercase()
        val blacklisted = listOf("smartphone", "oferta", "descuento", "chollos", "móvil", "fútbol", "fichajes")
        if (blacklisted.any { content.contains(it) }) return false
        return keywords.any { content.contains(it) }
    }

    private fun shareArticle(article: NewsArticle) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, article.title)
            putExtra(Intent.EXTRA_TEXT, "${article.title}\n\nLéelo en Vexo: ${article.url}")
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir noticia"))
    }
}
