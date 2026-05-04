package ui.news

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.model.NewsArticle

class NewsAdapter(
    private val onItemClick: (NewsArticle) -> Unit,
    private val onShareClick: (NewsArticle) -> Unit,
    private val onBookmarkClick: (NewsArticle, Boolean) -> Unit,
    private val isBookmarked: (NewsArticle) -> Boolean
) : RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

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
        holder.bind(articles[position])
    }

    override fun getItemCount() = articles.size

    inner class NewsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgNews = view.findViewById<ImageView>(R.id.imgNews)
        private val textSource = view.findViewById<TextView>(R.id.textNewsSource)
        private val textTitle = view.findViewById<TextView>(R.id.textNewsTitle)
        private val textDesc = view.findViewById<TextView>(R.id.textNewsDescription)
        private val textDate = view.findViewById<TextView>(R.id.textNewsDate)
        private val btnShare = view.findViewById<ImageButton>(R.id.btnShareNews)
        private val btnBookmark = view.findViewById<ImageButton>(R.id.btnBookmarkNews)

        fun bind(article: NewsArticle) {
            textSource.text = article.source.name
            textTitle.text = article.title
            textDesc.text = article.description ?: ""
            
            // Formateo simple de fecha
            textDate.text = try {
                article.publishedAt.substring(0, 10)
            } catch (e: Exception) {
                article.publishedAt
            }
            
            Glide.with(itemView.context)
                .load(article.urlToImage)
                .placeholder(R.drawable.vexo_logo)
                .centerCrop()
                .into(imgNews)

            // Estado del bookmark
            val bookmarked = isBookmarked(article)
            btnBookmark.setImageResource(if (bookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_border)
            btnBookmark.setOnClickListener {
                onBookmarkClick(article, !bookmarked)
                notifyItemChanged(adapterPosition)
            }

            btnShare.setOnClickListener { onShareClick(article) }
            itemView.setOnClickListener { onItemClick(article) }
        }
    }
}
