package com.vexo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vexo.app.R

data class VexoList(
    val id: String,
    val name: String,
    val description: String,
    val imageRes: Int
)

class VexoListAdapter(
    private val lists: List<VexoList>,
    private val onListClick: (VexoList) -> Unit
) : RecyclerView.Adapter<VexoListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textListName)
        val count: TextView = view.findViewById(R.id.textMovieCount)
        val btnDelete: View = view.findViewById(R.id.btnDeleteList)
        val img1: ImageView = view.findViewById(R.id.imgPreview1)
        val img2: ImageView = view.findViewById(R.id.imgPreview2)
        val img3: ImageView = view.findViewById(R.id.imgPreview3)
        val img4: ImageView = view.findViewById(R.id.imgPreview4)
        val textMore: TextView = view.findViewById(R.id.textMoreMovies)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.name.text = list.name
        holder.count.text = list.description
        
        // Para las listas de Vexo, ocultamos el botón borrar
        holder.btnDelete.visibility = View.GONE
        
        // Usamos el logo de Vexo para la preview principal
        holder.img1.visibility = View.VISIBLE
        holder.img1.setImageResource(list.imageRes)
        holder.img1.setPadding(20, 20, 20, 20) // Un poco de padding para que el logo no pegue a los bordes
        
        // Ocultamos el resto de previews
        holder.img2.visibility = View.GONE
        holder.img3.visibility = View.GONE
        holder.img4.visibility = View.GONE
        holder.textMore.visibility = View.GONE

        holder.itemView.setOnClickListener { onListClick(list) }
    }

    override fun getItemCount() = lists.size
}
