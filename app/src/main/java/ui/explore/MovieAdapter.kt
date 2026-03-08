package ui.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.model.Movie

class MovieAdapter(private var movies: List<Movie>) :
    RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    var onItemClick: ((Movie) -> Unit)? = null

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPoster: ImageView = itemView.findViewById(R.id.imgPoster)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textRating: TextView = itemView.findViewById(R.id.textRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]

        holder.textTitle.text = movie.title
        holder.textRating.text = "Rating: ${movie.rating}"

        Glide.with(holder.itemView.context)
            .load(movie.posterPath)
            .into(holder.imgPoster)

        holder.itemView.setOnClickListener {
            onItemClick?.invoke(movie)
        }
    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
