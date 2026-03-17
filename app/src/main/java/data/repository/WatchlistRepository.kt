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

    // GESTIÓN DE FAVORITOS Y VISTAS (Automática)
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
            val id = UUID.randomUUID().toString()
            targetList = UserList(id = id, name = listName)
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
