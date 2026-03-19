package data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Movie(
    val id: Int,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String? = null,
    val rating: Double,
    val genreIds: List<Int> = emptyList(),
    val releaseDate: String? = "" 
) : Parcelable {
    fun getReleaseYear(): String {
        return if (!releaseDate.isNullOrEmpty() && releaseDate.length >= 4) {
            releaseDate.substring(0, 4)
        } else {
            ""
        }
    }
}
