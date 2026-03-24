package ui.explore

import android.content.Intent
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val layoutViewMore: View = itemView.findViewById(R.id.layoutViewMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.textTitle.text = category.title
        
        // Estilo Premium: Títulos con fuente Black y Subtítulos dinámicos
        holder.textTitle.typeface = Typeface.create("sans-serif-black", Typeface.NORMAL)
        
        holder.textSubtitle.visibility = View.VISIBLE
        val title = category.title
        holder.textSubtitle.text = when {
            position == 0 -> "TENDENCIAS GLOBALES"
            position == 1 -> "LAS MEJOR VALORADAS"
            position == 2 -> "PRÓXIMOS ESTRENOS"
            position == 3 -> "LO MÁS RECIENTE"
            title.contains("Acción", true) -> "ADRENALINA PURA"
            title.contains("Comedia", true) -> "DIVERSIÓN ASEGURADA"
            title.contains("Terror", true) -> "PESADILLAS REALES"
            title.contains("Drama", true) -> "HISTORIAS QUE EMOCIONAN"
            title.contains("Ciencia ficción", true) || title.contains("Sci-Fi", true) -> "MUNDOS INCREÍBLES"
            title.contains("Animación", true) -> "ARTE Y MAGIA VISUAL"
            title.contains("Aventura", true) -> "EPICIDAD SIN LÍMITES"
            title.contains("Crimen", true) || title.contains("Suspense", true) || title.contains("Thriller", true) -> "TENSIÓN AL MÁXIMO"
            title.contains("Documental", true) -> "CONOCE LA REALIDAD"
            title.contains("Fantasía", true) -> "MAGIA EN CADA ESCENA"
            title.contains("Romance", true) -> "HISTORIAS DE AMOR"
            title.contains("Misterio", true) -> "DESCUBRE EL SECRETO"
            title.contains("Familia", true) -> "PARA DISFRUTAR JUNTOS"
            title.contains("Bélica", true) || title.contains("War", true) -> "CRÓNICAS DE GUERRA"
            title.contains("Historia", true) -> "RELATOS DEL PASADO"
            title.contains("Música", true) || title.contains("Music", true) -> "RITMO Y ESPECTÁCULO"
            title.contains("Western", true) -> "DUELOS Y LEYENDAS"
            else -> "CONTENIDO SELECCIONADO PARA TI"
        }

        // El nuevo botón "VER TODO" (layoutViewMore)
        if (position in 0..3) {
            holder.layoutViewMore.visibility = View.GONE
        } else {
            holder.layoutViewMore.visibility = View.VISIBLE
            holder.layoutViewMore.setOnClickListener {
                val intent = Intent(holder.itemView.context, CategoryMoviesActivity::class.java)
                intent.putExtra("categoryTitle", category.title)
                intent.putParcelableArrayListExtra("movies", ArrayList(category.movies))
                holder.itemView.context.startActivity(intent)
            }
        }

        // Indicador lateral elegante
        holder.indicator.visibility = View.VISIBLE

        // Destacar visualmente las primeras filas
        if (position in 0..3) {
            holder.textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            holder.textSubtitle.alpha = 1.0f
        } else {
            holder.textTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            holder.textSubtitle.alpha = 0.7f
        }
        
        val horizontalAdapter = if (position in 0..3) {
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

        holder.badgeTop.visibility = if (categoryPos == 3) View.GONE else View.VISIBLE

        val imageToLoad = movie.backdropPath ?: movie.posterPath
        com.bumptech.glide.Glide.with(holder.itemView.context)
            .load(imageToLoad)
            .centerCrop()
            .into(holder.imgPoster)

        holder.itemView.setOnClickListener { onItemClick?.invoke(movie) }
    }

    override fun getItemCount(): Int = movies.size
}
