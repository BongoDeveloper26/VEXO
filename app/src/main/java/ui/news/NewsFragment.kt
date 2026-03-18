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
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerNews = view.findViewById(R.id.recyclerNews)
        progressNews = view.findViewById(R.id.progressNews)
        
        setupRecyclerView()
        loadNews()
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

    private fun loadNews() {
        progressNews.visibility = View.VISIBLE
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        val service = retrofit.create(NewsService::class.java)
        
        lifecycleScope.launch {
            try {
                val response = service.getMovieNews()
                if (response.isSuccessful) {
                    val articles = response.body()?.articles?.filter { it.urlToImage != null } ?: emptyList()
                    newsAdapter.submitList(articles)
                } else {
                    Toast.makeText(requireContext(), "Error al cargar noticias", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Sin conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                progressNews.visibility = View.GONE
            }
        }
    }
}
