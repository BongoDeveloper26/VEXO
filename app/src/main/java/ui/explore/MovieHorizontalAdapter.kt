package ui.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.model.Movie
import java.util.Locale

class MovieHorizontalAdapter(private val movies: List<Movie>) :
    RecyclerView.Adapter<MovieHorizontalAdapter.MovieViewHolder>() {

    var onItemClick: ((Movie) -> Unit)? = null

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPoster: ImageView = itemView.findViewById(R.id.imgPoster)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textRating: TextView = itemView.findViewById(R.id.textRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_horizontal, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.textTitle.text = movie.title
        
        // Formatear el rating con un decimal
        val formattedRating = String.format(Locale.US, "%.1f", movie.rating)
        holder.textRating.text = "★ $formattedRating"

        Glide.with(holder.itemView.context)
            .load(movie.posterPath)
            .into(holder.imgPoster)

        holder.itemView.setOnClickListener {
            val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.click_scale)
            holder.itemView.startAnimation(animation)
            
            holder.itemView.postDelayed({
                onItemClick?.invoke(movie)
            }, 150)
        }
    }

    override fun getItemCount(): Int = movies.size
}
