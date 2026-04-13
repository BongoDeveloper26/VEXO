package com.vexo.app

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import data.model.Movie
import data.repository.WatchlistRepository

class RecentActivityAdapter(
    private val movies: List<Movie>,
    private val watchlistRepository: WatchlistRepository,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<RecentActivityAdapter.ViewHolder>() {

    private var isThemedMode: Boolean = false
    private var accentColor: Int = Color.parseColor("#00E5FF")

    fun updateTheme(isThemed: Boolean, accent: Int = Color.parseColor("#00E5FF")) {
        if (this.isThemedMode == isThemed && this.accentColor == accent) return
        this.isThemedMode = isThemed
        this.accentColor = accent
        notifyDataSetChanged()
    }

    // Por compatibilidad
    fun setFuturisticMode(enabled: Boolean) {
        updateTheme(enabled)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPoster: ImageView = view.findViewById(R.id.imgActivityPoster)
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
        val context = holder.itemView.context
        val userRating = watchlistRepository.getMovieRating(movie.id)

        val fullPosterPath = if (!movie.posterPath.isNullOrEmpty() && !movie.posterPath.startsWith("http")) {
            "https://image.tmdb.org/t/p/w342${movie.posterPath}" // Usamos un tamaño menor (w342) para optimizar
        } else {
            movie.posterPath
        }

        Glide.with(context)
            .load(fullPosterPath)
            .placeholder(android.R.drawable.progress_horizontal)
            .thumbnail(0.15f)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(holder.imgPoster)

        val primary = context.getColor(R.color.primary)

        holder.stars.forEachIndexed { index, imageView ->
            if (index < userRating) {
                imageView.setImageResource(android.R.drawable.star_big_on)
                imageView.imageTintList = ColorStateList.valueOf(
                    if (isThemedMode) accentColor else primary
                )
                imageView.alpha = 1.0f
            } else {
                imageView.setImageResource(android.R.drawable.star_big_off)
                imageView.imageTintList = ColorStateList.valueOf(
                    if (isThemedMode) Color.GRAY else context.getColor(R.color.text_secondary)
                )
                imageView.alpha = 0.3f
            }
        }

        holder.itemView.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(context, R.anim.click_scale)
            holder.itemView.startAnimation(animation)
            
            holder.itemView.postDelayed({
                onMovieClick(movie)
            }, 150)
        }
    }

    override fun getItemCount() = movies.size
}
