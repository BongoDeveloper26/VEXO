package ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.model.Movie

class MovieSearchAdapter(private var movies: List<Movie>) :
    RecyclerView.Adapter<MovieSearchAdapter.ViewHolder>() {

    var onItemClick: ((Movie) -> Unit)? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.imgPosterSearch)
        val title: TextView = view.findViewById(R.id.textTitleSearch)
        val subtitle: TextView = view.findViewById(R.id.textSubtitleSearch)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.title.text = movie.title
        val type = if (movie.isTvShow) "Serie" else "Película"
        holder.subtitle.text = "$type • ★ ${String.format("%.1f", movie.rating)}"

        Glide.with(holder.itemView.context)
            .load(movie.posterPath)
            .centerCrop()
            .into(holder.img)

        holder.itemView.setOnClickListener { onItemClick?.invoke(movie) }
    }

    override fun getItemCount() = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
