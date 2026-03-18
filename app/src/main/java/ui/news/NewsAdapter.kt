package ui.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.model.NewsArticle

class NewsAdapter(private val onItemClick: (NewsArticle) -> Unit) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    private var articles = listOf<NewsArticle>()

    fun submitList(newArticles: List<NewsArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(articles[position], onItemClick)
    }

    override fun getItemCount() = articles.size

    class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgNews = view.findViewById<ImageView>(R.id.imgNews)
        private val textSource = view.findViewById<TextView>(R.id.textNewsSource)
        private val textTitle = view.findViewById<TextView>(R.id.textNewsTitle)
        private val textDesc = view.findViewById<TextView>(R.id.textNewsDescription)
        private val textDate = view.findViewById<TextView>(R.id.textNewsDate)

        fun bind(article: NewsArticle, onClick: (NewsArticle) -> Unit) {
            textSource.text = article.source.name
            textTitle.text = article.title
            textDesc.text = article.description ?: ""
            textDate.text = article.publishedAt.substring(0, 10)
            
            Glide.with(itemView.context)
                .load(article.urlToImage)
                .into(imgNews)

            itemView.setOnClickListener { onClick(article) }
        }
    }
}
