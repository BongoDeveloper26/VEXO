package com.vexo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R

data class VexoList(
    val id: String,
    val name: String,
    val description: String,
    val imageRes: Int,
    val countText: String, 
    val totalItems: Int,
    val previewPosters: List<String> = emptyList()
)

class VexoListAdapter(
    private var lists: List<VexoList>,
    private val onListClick: (VexoList) -> Unit
) : RecyclerView.Adapter<VexoListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textListName)
        val count: TextView = view.findViewById(R.id.textMovieCount)
        val img1: ImageView = view.findViewById(R.id.imgPreview1)
        val img2: ImageView = view.findViewById(R.id.imgPreview2)
        val img3: ImageView = view.findViewById(R.id.imgPreview3)
        val img4: ImageView = view.findViewById(R.id.imgPreview4)
        val textMore: TextView = view.findViewById(R.id.textMoreMovies)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_list_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        
        holder.name.text = if (list.name.length > 25) list.name.take(25) + "..." else list.name
        holder.count.text = list.countText 
        
        val imgs = listOf(holder.img1, holder.img2, holder.img3, holder.img4)
        
        // Reset
        imgs.forEach { 
            it.visibility = View.GONE
            it.setPadding(0, 0, 0, 0)
        }
        holder.textMore.visibility = View.GONE

        if (list.previewPosters.isEmpty()) {
            imgs[0].visibility = View.VISIBLE
            imgs[0].setImageResource(R.drawable.vexo_logo)
            imgs[0].setPadding(12, 12, 12, 12)
        } else {
            list.previewPosters.forEachIndexed { index, url ->
                if (index < imgs.size) {
                    imgs[index].visibility = View.VISIBLE
                    Glide.with(holder.itemView.context)
                        .load(url)
                        .centerCrop()
                        .placeholder(R.drawable.vexo_logo)
                        .into(imgs[index])
                }
            }
            
            // Ajustamos el "+" con la cantidad real de elementos restantes
            if (list.totalItems > 4) {
                holder.textMore.visibility = View.VISIBLE
                val remaining = list.totalItems - 4
                holder.textMore.text = "+$remaining"
            }
        }

        holder.itemView.setOnClickListener { onListClick(list) }
    }

    override fun getItemCount() = lists.size

    fun updateLists(newList: List<VexoList>) {
        lists = newList
        notifyDataSetChanged()
    }
}
