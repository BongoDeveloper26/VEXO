package data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import data.model.Movie
import data.model.UserList
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
    }

    // GESTIÓN DE VALORACIONES (1 a 5 estrellas)
    fun setMovieRating(movie: Movie, rating: Float) {
        val ratings = getRatingsMap().toMutableMap()
        val ratedMovies = getRatedMoviesList().toMutableList()

        if (rating <= 0) {
            ratings.remove(movie.id)
            ratedMovies.removeAll { it.id == movie.id }
        } else {
            ratings[movie.id] = rating
            // Añadir a la lista de actividad (historial)
            ratedMovies.removeAll { it.id == movie.id }
            ratedMovies.add(0, movie)
        }

        // Guardar mapa de notas
        prefs.edit().putString(KEY_RATINGS, gson.toJson(ratings)).apply()
        // Guardar historial de objetos Movie
        prefs.edit().putString(KEY_RATED_MOVIES_DATA, gson.toJson(ratedMovies)).apply()
    }

    fun getMovieRating(movieId: Int): Float {
        return getRatingsMap()[movieId] ?: 0f
    }

    fun getRecentActivity(): List<Movie> {
        return getRatedMoviesList().take(5)
    }

    fun getAllRatedMovies(): List<Movie> {
        return getRatedMoviesList()
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

    // VITRINA (VITRINA DE 4 PELÍCULAS)
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
            val json = gson.toJson(vitrina)
            prefs.edit().putString(KEY_VITRINA, json).apply()
        }
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

    fun isMovieInVitrina(movieId: Int): Boolean {
        return getVitrinaMovies().any { it?.id == movieId }
    }

    fun removeFromVitrina(movieId: Int) {
        val vitrina = getVitrinaMovies().toMutableList()
        val index = vitrina.indexOfFirst { it?.id == movieId }
        if (index != -1) {
            vitrina[index] = null
            prefs.edit().putString(KEY_VITRINA, gson.toJson(vitrina)).apply()
        }
    }

    // GESTIÓN DE LISTAS DE USUARIO
    fun createUserList(name: String): String {
        val lists = getUserLists().toMutableList()
        val id = UUID.randomUUID().toString()
        val newList = UserList(id = id, name = name)
        lists.add(newList)
        saveUserLists(lists)
        return id
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

    fun getUserLists(): List<UserList> {
        val json = prefs.getString("user_custom_lists", null) ?: return emptyList()
        val type = object : TypeToken<List<UserList>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveUserLists(lists: List<UserList>) {
        val json = gson.toJson(lists)
        prefs.edit().putString("user_custom_lists", json).apply()
    }

    // GESTIÓN DE FAVORITOS Y VISTAS
    fun toggleFavorite(movie: Movie): Boolean {
        return toggleInternalList(movie, FAVORITES_LIST_NAME)
    }

    fun toggleWatched(movie: Movie): Boolean {
        return toggleInternalList(movie, WATCHED_LIST_NAME)
    }

    private fun toggleInternalList(movie: Movie, listName: String): Boolean {
        val lists = getUserLists().toMutableList()
        var targetList = lists.find { it.name == listName }
        if (targetList == null) {
            targetList = UserList(id = UUID.randomUUID().toString(), name = listName)
            lists.add(targetList)
        }
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

    fun isFavorite(movieId: Int): Boolean {
        return getUserLists().find { it.name == FAVORITES_LIST_NAME }?.movies?.any { it.id == movieId } ?: false
    }

    fun isWatched(movieId: Int): Boolean {
        return getUserLists().find { it.name == WATCHED_LIST_NAME }?.movies?.any { it.id == movieId } ?: false
    }

    fun isInWatchlist(movieId: Int): Boolean {
        return getUserLists().any { list -> list.movies.any { it.id == movieId } }
    }
}
