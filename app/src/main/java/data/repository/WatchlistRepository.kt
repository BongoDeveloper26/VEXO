package data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import data.model.Movie
import data.model.UserList
import data.model.DiaryEntry
import java.text.SimpleDateFormat
import java.util.*

class WatchlistRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("vexo_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val FAVORITES_LIST_NAME = "Mis Favoritos"
        const val WATCHED_LIST_NAME = "Vistas"
        private const val KEY_VITRINA = "user_vitrina"
        private const val KEY_RATINGS = "user_movie_ratings"
        private const val KEY_RATED_MOVIES_DATA = "user_rated_movies_data"
        private const val KEY_CUSTOM_LISTS = "user_custom_lists"
        private const val KEY_DIARY = "user_diary"
        private const val KEY_PROFILE_IMAGE = "user_profile_image"
        private const val KEY_USER_NAME = "user_name"
    }

    // --- ESTADÍSTICAS ---
    data class UserStats(val totalMovies: Int, val averageRating: Float)

    fun getStats(): UserStats {
        val ratings = getRatingsMap()
        val total = ratings.size
        val avg = if (total > 0) ratings.values.average().toFloat() else 0f
        return UserStats(total, avg)
    }

    // --- PERFIL ---
    fun setProfileImageUri(uri: String?) {
        prefs.edit().putString(KEY_PROFILE_IMAGE, uri).apply()
    }

    fun getProfileImageUri(): String? {
        return prefs.getString(KEY_PROFILE_IMAGE, null)
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "Usuario VEXO") ?: "Usuario VEXO"
    }

    // --- VALORACIONES Y DIARIO ---
    fun setMovieRating(movie: Movie, rating: Float, review: String? = null) {
        val ratings = getRatingsMap().toMutableMap()
        val ratedMovies = getRatedMoviesList().toMutableList()
        val diary = getDiary().toMutableList()

        if (rating <= 0) {
            ratings.remove(movie.id)
            ratedMovies.removeAll { it.id == movie.id }
        } else {
            ratings[movie.id] = rating
            
            ratedMovies.removeAll { it.id == movie.id }
            ratedMovies.add(0, movie)

            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            
            diary.add(0, DiaryEntry(
                movieId = movie.id,
                movieTitle = movie.title,
                moviePosterPath = movie.posterPath,
                rating = rating.toInt(),
                date = currentDate,
                review = review,
                movie = movie
            ))
        }

        prefs.edit().putString(KEY_RATINGS, gson.toJson(ratings)).apply()
        prefs.edit().putString(KEY_RATED_MOVIES_DATA, gson.toJson(ratedMovies)).apply()
        prefs.edit().putString(KEY_DIARY, gson.toJson(diary)).apply()
    }

    fun getMovieRating(movieId: Int): Float = getRatingsMap()[movieId] ?: 0f
    
    fun getRecentActivity(): List<Movie> = getRatedMoviesList().take(5)
    
    fun getAllRatedMovies(): List<Movie> = getRatedMoviesList()

    fun getDiary(): List<DiaryEntry> {
        val json = prefs.getString(KEY_DIARY, null) ?: return emptyList()
        val type = object : TypeToken<List<DiaryEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun getRatingsMap(): Map<Int, Float> {
        val json = prefs.getString(KEY_RATINGS, null) ?: return emptyMap()
        val type = object : TypeToken<Map<Int, Float>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun getRatedMoviesList(): List<Movie> {
        val json = prefs.getString(KEY_RATED_MOVIES_DATA, null) ?: return emptyList()
        val type = object : TypeToken<List<Movie>>() {}.type
        return gson.fromJson(json, type)
    }

    // --- VITRINA ---
    fun getVitrinaMovies(): List<Movie?> {
        val json = prefs.getString(KEY_VITRINA, null) ?: return listOf(null, null, null, null)
        val type = object : TypeToken<List<Movie?>>() {}.type
        val list: List<Movie?> = gson.fromJson(json, type)
        return if (list.size == 4) list else listOf(null, null, null, null)
    }

    fun setVitrinaMovie(index: Int, movie: Movie?) {
        val vitrina = getVitrinaMovies().toMutableList()
        if (index in 0..3) {
            vitrina[index] = movie
            prefs.edit().putString(KEY_VITRINA, gson.toJson(vitrina)).apply()
        }
    }

    fun isMovieInVitrina(movieId: Int): Boolean {
        return getVitrinaMovies().any { it?.id == movieId }
    }

    fun addMovieToVitrinaAuto(movie: Movie): Int {
        val vitrina = getVitrinaMovies().toMutableList()
        if (vitrina.any { it?.id == movie.id }) return 1
        val emptyIndex = vitrina.indexOfFirst { it == null }
        if (emptyIndex != -1) {
            vitrina[emptyIndex] = movie
            prefs.edit().putString(KEY_VITRINA, gson.toJson(vitrina)).apply()
            return 0
        }
        return 2
    }

    fun removeFromVitrina(movieId: Int) {
        val vitrina = getVitrinaMovies().toMutableList()
        val index = vitrina.indexOfFirst { it?.id == movieId }
        if (index != -1) {
            vitrina[index] = null
            prefs.edit().putString(KEY_VITRINA, gson.toJson(vitrina)).apply()
        }
    }

    // --- GESTIÓN DE LISTAS ---
    fun getUserLists(): List<UserList> {
        val json = prefs.getString(KEY_CUSTOM_LISTS, null) ?: return emptyList()
        val type = object : TypeToken<List<UserList>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveUserLists(lists: List<UserList>) {
        prefs.edit().putString(KEY_CUSTOM_LISTS, gson.toJson(lists)).apply()
    }

    fun createUserList(name: String, description: String? = null): String {
        val lists = getUserLists().toMutableList()
        val id = UUID.randomUUID().toString()
        lists.add(UserList(id = id, name = name, description = description))
        saveUserLists(lists)
        return id
    }

    fun updateUserList(listId: String, name: String, description: String?) {
        val lists = getUserLists().toMutableList()
        val index = lists.indexOfFirst { it.id == listId }
        if (index != -1) {
            lists[index] = lists[index].copy(name = name, description = description)
            saveUserLists(lists)
        }
    }

    fun deleteUserList(listId: String) {
        val lists = getUserLists().toMutableList()
        lists.removeAll { it.id == listId }
        saveUserLists(lists)
    }

    fun addMovieToList(listId: String, movie: Movie) {
        val lists = getUserLists().toMutableList()
        val listIndex = lists.indexOfFirst { it.id == listId }
        if (listIndex != -1) {
            val list = lists[listIndex]
            if (!list.movies.any { it.id == movie.id }) {
                list.movies.add(0, movie)
                saveUserLists(lists)
            }
        }
    }

    fun removeMovieFromList(listId: String, movieId: Int) {
        val lists = getUserLists().toMutableList()
        val listIndex = lists.indexOfFirst { it.id == listId }
        if (listIndex != -1) {
            lists[listIndex].movies.removeAll { it.id == movieId }
            saveUserLists(lists)
        }
    }

    // --- FAVORITOS Y VISTAS ---
    fun isFavorite(movieId: Int): Boolean {
        return getUserLists().find { it.name == FAVORITES_LIST_NAME }?.movies?.any { it.id == movieId } ?: false
    }

    fun isWatched(movieId: Int): Boolean {
        return getUserLists().find { it.name == WATCHED_LIST_NAME }?.movies?.any { it.id == movieId } ?: false
    }

    fun isInWatchlist(movieId: Int): Boolean {
        return getUserLists().any { list -> list.movies.any { it.id == movieId } }
    }

    fun toggleFavorite(movie: Movie): Boolean = toggleInternalList(movie, FAVORITES_LIST_NAME)
    fun toggleWatched(movie: Movie): Boolean = toggleInternalList(movie, WATCHED_LIST_NAME)

    private fun toggleInternalList(movie: Movie, listName: String): Boolean {
        val lists = getUserLists().toMutableList()
        var targetList = lists.find { it.name == listName } ?: UserList(id = UUID.randomUUID().toString(), name = listName).also { lists.add(it) }
        
        val isAdded: Boolean
        if (targetList.movies.any { it.id == movie.id }) {
            targetList.movies.removeAll { it.id == movie.id }
            isAdded = false
        } else {
            targetList.movies.add(0, movie)
            isAdded = true
        }
        saveUserLists(lists)
        return isAdded
    }
}
