package com.vexo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import data.model.Movie
import data.repository.WatchlistRepository

class RecentActivityAdapter(
    private val movies: List<Movie>,
    private val watchlistRepository: WatchlistRepository,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPoster: ImageView = view.findViewById(R.id.imgActivityPoster)
        val layoutStars: LinearLayout = view.findViewById(R.id.layoutActivityStars)
        val stars = listOf<ImageView>(
            view.findViewById(R.id.actStar1),
            view.findViewById(R.id.actStar2),
            view.findViewById(R.id.actStar3),
            view.findViewById(R.id.actStar4),
            view.findViewById(R.id.actStar5)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity_movie, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        val userRating = watchlistRepository.getMovieRating(movie.id)

        Glide.with(holder.itemView.context)
            .load(movie.posterPath)
            .centerCrop()
            .into(holder.imgPoster)

        // Actualizar estrellas según la nota que puso el usuario
        holder.stars.forEachIndexed { index, imageView ->
            if (index < userRating) {
                imageView.setImageResource(android.R.drawable.btn_star_big_on)
                imageView.alpha = 1.0f
            } else {
                imageView.setImageResource(android.R.drawable.btn_star_big_off)
                imageView.alpha = 0.2f
            }
        }

        holder.itemView.setOnClickListener { onMovieClick(movie) }
    }

    override fun getItemCount() = movies.size
}
