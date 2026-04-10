package com.vexo.app

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.model.DiaryEntry

class DiaryGroupedAdapter(
    private val items: List<DiaryListItem>,
    private val showTimeline: Boolean = true,
    private val onEntryClick: (DiaryEntry) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ENTRY = 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textHeader: TextView = view.findViewById(R.id.textHeaderTitle)
    }

    class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root: View = view.findViewById(R.id.itemDiaryRoot)
        val card: View = view.findViewById(R.id.cardDiaryContainer)
        val imgPoster: ImageView = view.findViewById(R.id.imgDiaryPoster)
        val textTitle: TextView = view.findViewById(R.id.textDiaryMovieTitle)
        val textDate: TextView = view.findViewById(R.id.textDiaryDate)
        val starsContainer: LinearLayout = view.findViewById(R.id.containerDiaryStars)
        val textReview: TextView = view.findViewById(R.id.textDiaryReview)
        val layoutTimeline: View = view.findViewById(R.id.layoutTimeline)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DiaryListItem.Header -> TYPE_HEADER
            is DiaryListItem.Entry -> TYPE_ENTRY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diary_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_diary_entry, parent, false)
            EntryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DiaryListItem.Header -> {
                (holder as HeaderViewHolder).textHeader.text = item.title
            }

            is DiaryListItem.Entry -> {
                val entry = item.diaryEntry
                val vh = holder as EntryViewHolder

                vh.textTitle.text = entry.movieTitle
                vh.textDate.text = entry.date
                
                // Control dinámico de visibilidad
                vh.layoutTimeline.visibility = if (showTimeline) View.VISIBLE else View.GONE

                // Mostrar u ocultar la reseña
                if (!entry.review.isNullOrEmpty()) {
                    vh.textReview.visibility = View.VISIBLE
                    vh.textReview.text = entry.review
                } else {
                    vh.textReview.visibility = View.GONE
                }

                Glide.with(vh.itemView.context)
                    .load(entry.moviePosterPath)
                    .placeholder(android.R.drawable.progress_horizontal)
                    .error(android.R.drawable.ic_menu_report_image)
                    .centerCrop()
                    .into(vh.imgPoster)

                vh.starsContainer.removeAllViews()

                repeat(5) { index ->
                    val star = ImageView(vh.itemView.context).apply {
                        layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                            marginEnd = 4
                        }

                        if (index < entry.rating) {
                            setImageResource(android.R.drawable.btn_star_big_on)
                            imageTintList = ColorStateList.valueOf(
                                context.getColor(R.color.primary)
                            )
                            alpha = 1.0f
                        } else {
                            setImageResource(android.R.drawable.btn_star_big_off)
                            imageTintList = ColorStateList.valueOf(
                                context.getColor(R.color.text_secondary)
                            )
                            alpha = 0.3f
                        }
                    }
                    vh.starsContainer.addView(star)
                }

                vh.itemView.setOnClickListener {
                    onEntryClick(entry)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}