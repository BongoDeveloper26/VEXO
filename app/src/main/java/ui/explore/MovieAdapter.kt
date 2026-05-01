package ui.explore

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.vexo.app.R
import data.model.Movie
import data.repository.WatchlistRepository

class MovieAdapter(private var movies: List<Movie>, private val isGridView: Boolean = false) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((Movie) -> Unit)? = null
    private var watchlistRepository: WatchlistRepository? = null

    private val allGenresMap = mapOf(
        28 to "Acción", 12 to "Aventura", 16 to "Animación",
        35 to "Comedia", 80 to "Crimen", 99 to "Doc",
        18 to "Drama", 10751 to "Familiar", 14 to "Fantasía",
        36 to "Historia", 27 to "Terror", 10402 to "Música",
        9648 to "Misterio", 10749 to "Romance", 878 to "Ciencia Ficción",
        53 to "Suspense", 10752 to "Bélica", 37 to "Western"
    )

    class ListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPoster: ImageView = itemView.findViewById(R.id.imgPoster)
        val textTitle: TextView = itemView.findViewById(R.id.textTitle)
        val textRating: TextView = itemView.findViewById(R.id.textRating)
        val textDescription: TextView? = itemView.findViewById(R.id.textDescription)
        val chipGroup: ChipGroup = itemView.findViewById(R.id.chipGroupItemGenres)
        val watchedOverlay: View? = itemView.findViewById(R.id.viewWatchedOverlay)
        val watchedBadge: ImageView? = itemView.findViewById(R.id.imgWatchedBadge)
        val favBadge: ImageView? = itemView.findViewById(R.id.imgFavoriteBadge)
    }

    class GridViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPoster: ImageView = itemView.findViewById(R.id.imgPosterGrid)
        val textRating: TextView = itemView.findViewById(R.id.textRatingGrid)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isGridView) 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (watchlistRepository == null) {
            watchlistRepository = WatchlistRepository(parent.context)
        }
        
        return if (viewType == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie_grid, parent, false)
            GridViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
            ListViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val movie = movies[position]
        val context = holder.itemView.context
        
        val isWatched = watchlistRepository?.isWatched(movie.id) ?: false
        val isFav = watchlistRepository?.isFavorite(movie.id) ?: false

        if (holder is GridViewHolder) {
            holder.textRating.text = "★ ${String.format("%.1f", movie.rating)}"
            
            Glide.with(context)
                .load(movie.posterPath)
                .thumbnail(0.1f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(holder.imgPoster)
                
        } else if (holder is ListViewHolder) {
            holder.textTitle.text = movie.title
            holder.textRating.text = "★ ${String.format("%.1f", movie.rating)}"
            holder.textDescription?.text = movie.overview
            
            holder.watchedOverlay?.visibility = if (isWatched) View.VISIBLE else View.GONE
            holder.watchedBadge?.visibility = if (isWatched) View.VISIBLE else View.GONE
            holder.favBadge?.visibility = if (isFav) View.VISIBLE else View.GONE

            holder.chipGroup.removeAllViews()
            movie.genreIds.take(2).forEach { id ->
                allGenresMap[id]?.let { name ->
                    val chip = Chip(context).apply {
                        text = name
                        textSize = 8f
                        setChipBackgroundColorResource(R.color.background_app)
                        setTextColor(context.getColor(R.color.text_secondary))
                        chipStrokeWidth = 0f
                        minHeight = 0
                        minimumHeight = 0
                        // Ajustamos la altura directamente en pixels para asegurar compacidad
                        val density = context.resources.displayMetrics.density
                        chipMinHeight = 18 * density
                        setPadding((8 * density).toInt(), 0, (8 * density).toInt(), 0)
                        isEnabled = false
                    }
                    holder.chipGroup.addView(chip)
                }
            }
            
            Glide.with(context)
                .load(movie.posterPath)
                .thumbnail(0.2f)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(holder.imgPoster)
        }

        holder.itemView.setOnClickListener { onItemClick?.invoke(movie) }
    }

    override fun getItemCount(): Int = movies.size

    fun updateMovies(newMovies: List<Movie>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = movies.size
            override fun getNewListSize(): Int = newMovies.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return movies[oldItemPosition].id == newMovies[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return movies[oldItemPosition] == newMovies[newItemPosition]
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        movies = newMovies
        diffResult.dispatchUpdatesTo(this)
    }
}
