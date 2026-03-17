package ui.explore

import android.content.Intent
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vexo.app.R
import data.model.Category
import data.model.Movie

class CategoryAdapter(
    private var categories: List<Category>,
    private val onMovieClick: (Movie) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textTitle: TextView = itemView.findViewById(R.id.textCategoryTitle)
        val textSubtitle: TextView = itemView.findViewById(R.id.textCategorySubtitle)
        val recyclerHorizontal: RecyclerView = itemView.findViewById(R.id.recyclerMoviesHorizontal)
        val indicator: View = itemView.findViewById(R.id.viewIndicator)
        val btnViewMore: ImageButton = itemView.findViewById(R.id.btnViewMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.textTitle.text = category.title
        
        // El botón ">" (ver más) solo aparecerá a partir de la cuarta fila (géneros)
        if (position in 0..2) {
            holder.btnViewMore.visibility = View.GONE
        } else {
            holder.btnViewMore.visibility = View.VISIBLE
            holder.btnViewMore.setOnClickListener {
                val intent = Intent(holder.itemView.context, CategoryMoviesActivity::class.java)
                intent.putExtra("categoryTitle", category.title)
                intent.putParcelableArrayListExtra("movies", ArrayList(category.movies))
                holder.itemView.context.startActivity(intent)
            }
        }

        // Estilo diferencial para las 3 primeras categorías
        if (position in 0..2) {
            holder.indicator.visibility = View.VISIBLE
            holder.textSubtitle.visibility = View.VISIBLE
            
            holder.textSubtitle.text = when(position) {
                0 -> if (categories[0].title.contains("Semana", true)) "Tendencias de hoy" else "Weekly Hits"
                1 -> if (categories[1].title.contains("Valoradas", true)) "Joyas del cine" else "Masterpieces"
                2 -> if (categories[2].title.contains("Cines", true)) "Cartelera actual" else "Now on theaters"
                else -> ""
            }

            holder.textTitle.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
                setTypeface(null, Typeface.BOLD)
                letterSpacing = 0.05f
                setTextColor(context.getColor(R.color.text_primary))
            }
            holder.textSubtitle.setTextColor(holder.itemView.context.getColor(R.color.primary_dark))
            
        } else {
            holder.indicator.visibility = View.GONE
            holder.textSubtitle.visibility = View.GONE
            
            holder.textTitle.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 19f)
                setTypeface(null, Typeface.BOLD)
                letterSpacing = 0.02f
                setTextColor(context.getColor(R.color.text_primary))
                alpha = 0.8f
            }
        }
        
        val horizontalAdapter = if (position in 0..2) {
            MovieFeaturedAdapter(category.movies, position).apply { onItemClick = onMovieClick }
        } else {
            MovieHorizontalAdapter(category.movies).apply { onItemClick = onMovieClick }
        }
        
        holder.recyclerHorizontal.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = horizontalAdapter as? RecyclerView.Adapter<*>
            setHasFixedSize(true)
        }
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}

class MovieFeaturedAdapter(
    private val movies: List<Movie>,
    private val categoryPos: Int = -1
) : RecyclerView.Adapter<MovieFeaturedAdapter.MovieViewHolder>() {

    var onItemClick: ((Movie) -> Unit)? = null

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPoster: android.widget.ImageView = itemView.findViewById(R.id.imgPoster)
        val textTitle: android.widget.TextView = itemView.findViewById(R.id.textTitle)
        val textRating: android.widget.TextView = itemView.findViewById(R.id.textRating)
        val badgeTop: android.widget.TextView = itemView.findViewById(R.id.textTopBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_featured, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.textTitle.text = movie.title
        holder.textRating.text = "★ ${String.format("%.1f", movie.rating)}"

        holder.badgeTop.visibility = if (categoryPos == 2) View.GONE else View.VISIBLE

        val imageToLoad = movie.backdropPath ?: movie.posterPath
        com.bumptech.glide.Glide.with(holder.itemView.context)
            .load(imageToLoad)
            .centerCrop()
            .into(holder.imgPoster)

        holder.itemView.setOnClickListener { onItemClick?.invoke(movie) }
    }

    override fun getItemCount(): Int = movies.size
}
