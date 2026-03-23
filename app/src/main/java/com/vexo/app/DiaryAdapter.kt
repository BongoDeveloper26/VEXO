package com.vexo.app

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

class DiaryAdapter(
    private val entries: List<DiaryEntry>,
    private val onEntryClick: (DiaryEntry) -> Unit
) : RecyclerView.Adapter<DiaryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPoster: ImageView = view.findViewById(R.id.imgDiaryPoster)
        val textTitle: TextView = view.findViewById(R.id.textDiaryMovieTitle)
        val textDate: TextView = view.findViewById(R.id.textDiaryDate)
        val starsContainer: LinearLayout = view.findViewById(R.id.containerDiaryStars)
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

        Glide.with(holder.itemView.context)
            .load(entry.moviePosterPath)
            .placeholder(android.R.drawable.progress_horizontal)
            .error(android.R.drawable.ic_menu_report_image)
            .centerCrop()
            .into(holder.imgPoster)

        holder.starsContainer.removeAllViews()

        repeat(5) { index ->
            val star = ImageView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(24, 24).apply {
                    marginEnd = 4
                }

                if (index < entry.rating) {
                    setImageResource(android.R.drawable.btn_star_big_on)
                    imageTintList = android.content.res.ColorStateList.valueOf(
                        context.getColor(R.color.primary)
                    )
                    alpha = 1.0f
                } else {
                    setImageResource(android.R.drawable.btn_star_big_off)
                    imageTintList = android.content.res.ColorStateList.valueOf(
                        context.getColor(R.color.text_secondary)
                    )
                    alpha = 0.3f
                }
            }
            holder.starsContainer.addView(star)
        }

        holder.itemView.setOnClickListener {
            onEntryClick(entry)
        }
    }

    override fun getItemCount(): Int = entries.size
}