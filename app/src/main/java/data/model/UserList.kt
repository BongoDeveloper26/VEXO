package data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserList(
    val id: String,
    var name: String,
    var description: String? = null,
    val movies: MutableList<Movie> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
