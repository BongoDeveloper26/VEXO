package com.vexo.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BackgroundAdapter(
    private val options: List<BackgroundOption>,
    private val currentId: String?,
    private val onSelected: (BackgroundOption) -> Unit
) : RecyclerView.Adapter<BackgroundAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgPreview: ImageView = view.findViewById(R.id.imgOptionPreview)
        val textName: TextView = view.findViewById(R.id.textOptionName)
        val imgCheck: ImageView = view.findViewById(R.id.imgCheckmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_background_option, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val option = options[position]
        holder.textName.text = option.name
        
        if (option.previewResId != 0) {
            holder.imgPreview.setImageResource(option.previewResId)
        } else {
            holder.imgPreview.setImageResource(R.drawable.bg_profile_content_gradient)
        }

        holder.imgCheck.visibility = if (option.id == currentId) View.VISIBLE else View.GONE
        
        holder.itemView.setOnClickListener { onSelected(option) }
    }

    override fun getItemCount() = options.size
}
