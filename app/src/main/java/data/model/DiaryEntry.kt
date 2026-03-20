package data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiaryEntry(
    val movieId: Int,
    val movieTitle: String,
    val moviePosterPath: String?,
    val rating: Int,
    val date: String
) : Parcelable
