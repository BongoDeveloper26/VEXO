package ui.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.ChipGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vexo.app.R
import data.api.NewsService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NewsFragment : Fragment() {

    private lateinit var recyclerNews: RecyclerView
    private lateinit var progressNews: ProgressBar
    private lateinit var chipGroupFilters: ChipGroup
    private lateinit var newsAdapter: NewsAdapter

    // Lista de dominios especializados en cine para garantizar calidad
    private val movieDomains = "espinof.com,fotogramas.es,sensacine.com,ecartelera.com,elseptimoarte.net,cinematomania.es,decine21.com,accioncine.es,objetivocine.es"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerNews = view.findViewById(R.id.recyclerNews)
        progressNews = view.findViewById(R.id.progressNews)
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters)
        
        setupRecyclerView()
        setupFilters()
        
        loadNews(getFilterQuery(R.id.chipLatest), isGeneral = true)
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter { article ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
            startActivity(intent)
        }
        recyclerNews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chipId = checkedIds.first()
                val query = getFilterQuery(chipId)
                loadNews(query, isGeneral = (chipId == R.id.chipLatest))
            }
        }
    }

    private fun getFilterQuery(chipId: Int): String {
        // Exclusiones agresivas para eliminar ruido tecnológico y de ofertas
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
        progressNews.visibility = View.VISIBLE
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val service = retrofit.create(NewsService::class.java)
        
        lifecycleScope.launch {
            try {
                // Si no es general, restringimos a dominios de cine y buscamos solo en el título
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
                        it.urlToImage != null && 
                        !it.title.contains("[Removed]") &&
                        // Filtro de seguridad adicional para asegurar relevancia temática
                        isValidMovieContent(it.title, it.description ?: "")
                    } ?: emptyList()
                    
                    newsAdapter.submitList(articles)
                    
                    if (articles.isEmpty() && !isGeneral) {
                        // Si no hay resultados con dominios específicos, intentamos búsqueda amplia pero en título
                        loadNews(query, isGeneral = true) 
                    }
                } else {
                    Toast.makeText(requireContext(), "Error al conectar con el servidor", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de red", Toast.LENGTH_SHORT).show()
            } finally {
                progressNews.visibility = View.GONE
            }
        }
    }

    private fun isValidMovieContent(title: String, description: String): Boolean {
        val keywords = listOf("cine", "película", "estreno", "actor", "actriz", "director", "serie", "tráiler", "hollywood", "filme", "reparto", "taquilla", "oscar", "goya")
        val content = (title + description).lowercase()
        
        // Evitar explícitamente contenido que hable de móviles u ofertas comerciales
        val blacklisted = listOf("smartphone", "oferta", "descuento", "chollos", "móvil", "móviles", "fútbol", "fichajes")
        if (blacklisted.any { content.contains(it) }) return false

        return keywords.any { content.contains(it) }
    }
}
