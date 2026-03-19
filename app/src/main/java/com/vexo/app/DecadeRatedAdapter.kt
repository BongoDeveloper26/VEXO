package com.vexo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import data.model.Movie
import data.repository.WatchlistRepository

sealed class DecadeItem {
    data class Header(val decade: String) : DecadeItem()
    data class MovieItem(val movie: Movie) : DecadeItem()
}

class DecadeRatedAdapter(
    private val items: List<DecadeItem>,
    private val watchlistRepository: WatchlistRepository,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_MOVIE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DecadeItem.Header -> TYPE_HEADER
            is DecadeItem.MovieItem -> TYPE_MOVIE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_decade_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity_movie, parent, false)
            MovieViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is HeaderViewHolder && item is DecadeItem.Header) {
            holder.textTitle.text = item.decade
        } else if (holder is MovieViewHolder && item is DecadeItem.MovieItem) {
            val movie = item.movie
            val userRating = watchlistRepository.getMovieRating(movie.id)

            // CORRECCIÓN: Asegurar URL del póster
            val fullPosterPath = if (!movie.posterPath.isNullOrEmpty() && !movie.posterPath.startsWith("http")) {
                "https://image.tmdb.org/t/p/w500${movie.posterPath}"
            } else {
                movie.posterPath
            }

            Glide.with(holder.itemView.context)
                .load(fullPosterPath)
                .centerCrop()
                .placeholder(android.R.drawable.progress_horizontal)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.imgPoster)

            holder.stars.forEachIndexed { index, imageView ->
                if (index < userRating) {
                    imageView.setImageResource(android.R.drawable.star_big_on)
                    imageView.alpha = 1.0f
                } else {
                    imageView.setImageResource(android.R.drawable.star_big_off)
                    imageView.alpha = 0.3f
                }
            }
            holder.itemView.setOnClickListener { onMovieClick(movie) }
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textDecadeTitle)
    }

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPoster: ImageView = view.findViewById(R.id.imgActivityPoster)
        val stars = listOf<ImageView>(
            view.findViewById(R.id.actStar1),
            view.findViewById(R.id.actStar2),
            view.findViewById(R.id.actStar3),
            view.findViewById(R.id.actStar4),
            view.findViewById(R.id.actStar5)
        )
    }
}
