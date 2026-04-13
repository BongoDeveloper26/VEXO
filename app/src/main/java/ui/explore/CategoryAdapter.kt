package ui.explore

import android.content.Intent
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
        val layoutViewMore: View = itemView.findViewById(R.id.layoutViewMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        val title = category.title
        val titleLower = title.lowercase()

        val isFeatured = titleLower.contains("tendencia") || titleLower.contains("trending") || 
                         titleLower.contains("valoradas") || titleLower.contains("top rated") || 
                         titleLower.contains("cines") || titleLower.contains("now playing")

        holder.textTitle.text = title
        holder.textTitle.typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
        
        holder.textSubtitle.visibility = View.VISIBLE
        holder.textSubtitle.text = when {
            titleLower.contains("tendencia") || titleLower.contains("trending") -> "TENDENCIAS GLOBALES"
            titleLower.contains("valoradas") || titleLower.contains("top rated") -> "LAS MEJOR VALORADAS"
            titleLower.contains("cines") || titleLower.contains("now playing") -> "EN CARTELERA"
            titleLower.contains("acción", true) -> "ADRENALINA PURA"
            titleLower.contains("comedia", true) -> "DIVERSIÓN ASEGURADA"
            titleLower.contains("terror", true) -> "PESADILLAS REALES"
            titleLower.contains("drama", true) -> "HISTORIAS QUE EMOCIONAN"
            titleLower.contains("ciencia ficción", true) || titleLower.contains("sci-fi", true) -> "MUNDOS INCREÍBLES"
            titleLower.contains("animación", true) -> "ARTE Y MAGIA VISUAL"
            titleLower.contains("aventura", true) -> "EPICIDAD SIN LÍMITES"
            titleLower.contains("crimen", true) || titleLower.contains("suspense", true) || titleLower.contains("thriller", true) -> "TENSIÓN AL MÁXIMO"
            titleLower.contains("documental", true) -> "CONOCE LA REALIDAD"
            titleLower.contains("fantasía", true) -> "MAGIA EN CADA ESCENA"
            titleLower.contains("romance", true) -> "HISTORIAS DE AMOR"
            titleLower.contains("misterio", true) -> "DESCUBRE EL SECRETO"
            titleLower.contains("familia", true) -> "PARA DISFRUTAR JUNTOS"
            titleLower.contains("bélica", true) || titleLower.contains("war", true) -> "CRÓNICAS DE GUERRA"
            titleLower.contains("historia", true) -> "RELATOS DEL PASADO"
            titleLower.contains("música", true) || titleLower.contains("music", true) -> "RITMO Y ESPECTÁCULO"
            titleLower.contains("western", true) -> "DUELOS Y LEYENDAS"
            else -> "CONTENIDO SELECCIONADO PARA TI"
        }

        if (isFeatured) {
            holder.layoutViewMore.visibility = View.GONE
            holder.textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            holder.textSubtitle.alpha = 1.0f
        } else {
            holder.layoutViewMore.visibility = View.VISIBLE
            holder.layoutViewMore.setOnClickListener {
                val intent = Intent(holder.itemView.context, CategoryMoviesActivity::class.java)
                intent.putExtra("categoryTitle", category.title)
                intent.putParcelableArrayListExtra("movies", ArrayList(category.movies))
                holder.itemView.context.startActivity(intent)
            }
            holder.textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            holder.textSubtitle.alpha = 0.7f
        }

        holder.indicator.visibility = View.VISIBLE

        val horizontalAdapter = if (isFeatured) {
            val mockPos = if (titleLower.contains("cines") || titleLower.contains("now playing")) 3 else 0
            MovieFeaturedAdapter(category.movies, mockPos).apply { onItemClick = onMovieClick }
        } else {
            MovieHorizontalAdapter(category.movies).apply { onItemClick = onMovieClick }
        }
        
        holder.recyclerHorizontal.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = horizontalAdapter as? RecyclerView.Adapter<*>
            setHasFixedSize(true)
            setItemViewCacheSize(10) // Cache de vistas para scroll horizontal suave
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

        holder.badgeTop.visibility = if (categoryPos == 3) View.GONE else View.VISIBLE

        val imageToLoad = movie.backdropPath ?: movie.posterPath
        
        // Optimización de Glide para contenido destacado
        Glide.with(holder.itemView.context)
            .load(imageToLoad)
            .thumbnail(0.1f)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
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
