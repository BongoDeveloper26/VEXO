package ui.search

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.repository.PersonDTO
import ui.detail.PersonMoviesActivity

class PersonSearchAdapter(private var people: List<PersonDTO>) :
    RecyclerView.Adapter<PersonSearchAdapter.PersonViewHolder>() {

    fun updatePeople(newPeople: List<PersonDTO>) {
        people = newPeople
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_person_search, parent, false)
        return PersonViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(people[position])
    }

    override fun getItemCount() = people.size

    inner class PersonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imgPerson: ImageView = view.findViewById(R.id.imgPerson)
        private val textName: TextView = view.findViewById(R.id.textPersonName)
        private val textCharacter: TextView = view.findViewById(R.id.textCharacterName)

        fun bind(person: PersonDTO) {
            textName.text = person.name
            
            if (!person.character.isNullOrEmpty()) {
                textCharacter.text = person.character
                textCharacter.visibility = View.VISIBLE
            } else {
                textCharacter.visibility = View.GONE
            }

            val profilePath = person.profile_path?.let { "https://image.tmdb.org/t/p/w185$it" }
            
            Glide.with(itemView.context)
                .load(profilePath)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .into(imgPerson)

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, PersonMoviesActivity::class.java)
                intent.putExtra("personId", person.id)
                itemView.context.startActivity(intent)
            }
        }
    }
}
