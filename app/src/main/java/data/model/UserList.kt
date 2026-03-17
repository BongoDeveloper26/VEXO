package data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserList(
    val id: String,
    val name: String,
    val movies: MutableList<Movie> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
