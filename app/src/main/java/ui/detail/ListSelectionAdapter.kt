package ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vexo.app.R
import data.model.UserList

class ListSelectionAdapter(
    private val lists: List<UserList>,
    private val movieId: Int,
    private val onListClicked: (UserList, Boolean) -> Unit
) : RecyclerView.Adapter<ListSelectionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textName: TextView = view.findViewById(R.id.textListName)
        val checkBox: CheckBox = view.findViewById(R.id.checkBoxList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = lists[position]
        holder.textName.text = list.name
        
        val isInList = list.movies.any { it.id == movieId }
        holder.checkBox.isChecked = isInList

        holder.itemView.setOnClickListener {
            val newState = !holder.checkBox.isChecked
            holder.checkBox.isChecked = newState
            onListClicked(list, newState)
        }
        
        holder.checkBox.setOnClickListener {
            onListClicked(list, holder.checkBox.isChecked)
        }
    }

    override fun getItemCount() = lists.size
}
