package ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.vexo.app.R
import data.repository.CastDTO

class CastAdapter(
    private val castList: List<CastDTO>,
    private val onActorClick: (Int, String) -> Unit
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgCast: ImageView = itemView.findViewById(R.id.imgCast)
        val textName: TextView = itemView.findViewById(R.id.textCastName)
        val textCharacter: TextView = itemView.findViewById(R.id.textCastCharacter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val actor = castList[position]
        holder.textName.text = actor.name
        holder.textCharacter.text = actor.character

        val profileUrl = if (actor.profile_path != null) {
            "https://image.tmdb.org/t/p/w185${actor.profile_path}"
        } else {
            null
        }

        // Optimización de Glide para los actores
        Glide.with(holder.itemView.context)
            .load(profileUrl)
            .thumbnail(0.1f) // Carga una versión ligera primero para evitar huecos blancos
            .placeholder(R.drawable.vexo_logo)
            .error(R.drawable.vexo_logo)
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Cachear para que al volver atrás no descargue de nuevo
            .centerCrop()
            .into(holder.imgCast)

        holder.itemView.setOnClickListener {
            onActorClick(actor.id, actor.name)
        }
    }

    override fun getItemCount(): Int = castList.size
}
