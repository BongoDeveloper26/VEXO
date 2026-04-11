package com.vexo.app

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import com.vexo.app.R
import data.model.DiaryEntry

class DiaryAdapter(
    private val entries: List<DiaryEntry>,
    private val showTimeline: Boolean = true,
    private val isFavorite: ((Int) -> Boolean)? = null,
    private val onEntryClick: (DiaryEntry) -> Unit
) : RecyclerView.Adapter<DiaryAdapter.ViewHolder>() {

    private var isThemedMode: Boolean = false
    private var accentColor: Int = Color.parseColor("#00E5FF")
    private var cardBackgroundColor: Int = Color.parseColor("#E61A1A1A")
    private var strokeColor: Int = Color.parseColor("#3300E5FF")

    fun updateTheme(isThemed: Boolean, accent: Int = Color.parseColor("#00E5FF"), cardBg: Int = Color.parseColor("#E61A1A1A"), stroke: Int = Color.parseColor("#3300E5FF")) {
        this.isThemedMode = isThemed
        this.accentColor = accent
        this.cardBackgroundColor = cardBg
        this.strokeColor = stroke
        notifyDataSetChanged()
    }

    // Mantener por compatibilidad si se usa en otros sitios
    fun setFuturisticMode(enabled: Boolean) {
        updateTheme(enabled)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPoster: ImageView = view.findViewById(R.id.imgDiaryPoster)
        val textTitle: TextView = view.findViewById(R.id.textDiaryMovieTitle)
        val textDate: TextView = view.findViewById(R.id.textDiaryDate)
        val starsContainer: LinearLayout = view.findViewById(R.id.containerDiaryStars)
        val textReview: TextView = view.findViewById(R.id.textDiaryReview)
        val layoutTimeline: View = view.findViewById(R.id.layoutTimeline)
        val imgHeart: ImageView = view.findViewById(R.id.imgDiaryHeart)
        val cardContainer: MaterialCardView = view.findViewById(R.id.cardDiaryContainer)
        val dotTimeline: View? = (layoutTimeline as? ViewGroup)?.getChildAt(1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diary_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = entries[position]

        holder.textTitle.text = entry.movieTitle
        holder.textDate.text = entry.date
        
        holder.layoutTimeline.visibility = if (showTimeline) View.VISIBLE else View.GONE

        if (isThemedMode) {
            // Ajustes Modo Temático (Futurista, Espacio, Cine, etc.)
            holder.cardContainer.setCardBackgroundColor(cardBackgroundColor)
            holder.cardContainer.strokeWidth = 2
            holder.cardContainer.strokeColor = strokeColor
            
            holder.dotTimeline?.backgroundTintList = ColorStateList.valueOf(accentColor)
            holder.textTitle.setTextColor(Color.WHITE)
            holder.textDate.setTextColor(Color.WHITE)
            holder.textDate.alpha = 0.7f
            holder.textReview.setTextColor(Color.LTGRAY)
        } else {
            // Ajustes Modo Predeterminado
            holder.cardContainer.setCardBackgroundColor(holder.itemView.context.getColor(R.color.surface_app))
            holder.cardContainer.strokeWidth = 0
            
            holder.dotTimeline?.backgroundTintList = null
            holder.textTitle.setTextColor(holder.itemView.context.getColor(R.color.text_primary))
            holder.textDate.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
            holder.textDate.alpha = 0.6f
            holder.textReview.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
        }

        if (isFavorite != null && isFavorite.invoke(entry.movieId)) {
            holder.imgHeart.visibility = View.VISIBLE
            holder.imgHeart.imageTintList = ColorStateList.valueOf(if (isThemedMode) accentColor else holder.itemView.context.getColor(R.color.primary))
        } else {
            holder.imgHeart.visibility = View.GONE
        }

        if (!entry.review.isNullOrEmpty()) {
            holder.textReview.visibility = View.VISIBLE
            holder.textReview.text = entry.review
        } else {
            holder.textReview.visibility = View.GONE
        }

        Glide.with(holder.itemView.context)
            .load(entry.moviePosterPath)
            .placeholder(android.R.drawable.progress_horizontal)
            .centerCrop()
            .into(holder.imgPoster)

        holder.starsContainer.removeAllViews()
        repeat(5) { index ->
            val star = ImageView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24).apply { marginEnd = 4 }
                if (index < entry.rating) {
                    setImageResource(android.R.drawable.btn_star_big_on)
                    imageTintList = ColorStateList.valueOf(if (isThemedMode) accentColor else context.getColor(R.color.primary))
                    alpha = 1.0f
                } else {
                    setImageResource(android.R.drawable.btn_star_big_off)
                    imageTintList = ColorStateList.valueOf(if (isThemedMode) Color.GRAY else context.getColor(R.color.text_secondary))
                    alpha = 0.3f
                }
            }
            holder.starsContainer.addView(star)
        }

        holder.itemView.setOnClickListener { onEntryClick(entry) }
    }

    override fun getItemCount(): Int = entries.size
}
