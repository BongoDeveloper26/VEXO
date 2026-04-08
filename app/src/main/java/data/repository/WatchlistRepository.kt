package data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import data.model.Movie
import data.model.UserList
import data.model.DiaryEntry
import java.text.SimpleDateFormat
import java.util.*

class WatchlistRepository(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val gson = Gson()
    
    // IMPORTANTE: El userId debe obtenerse dinámicamente cada vez
    private val userId: String? get() = auth.currentUser?.uid

    // Referencia al documento del usuario en Firestore
    private fun getUserDoc() = userId?.let { firestore.collection("users").document(it) }

    // Preferencias locales específicas por usuario para evitar mezclas
    private fun getPrefs() = context.getSharedPreferences("vexo_prefs_${userId ?: "guest"}", Context.MODE_PRIVATE)

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
        private const val TAG = "WatchlistRepository"
    }

    // --- SINCRONIZACIÓN CON FIRESTORE ---

    fun downloadCloudData(onComplete: (Boolean) -> Unit = {}) {
        val userDoc = getUserDoc() ?: return
        userDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val editor = getPrefs().edit()
                document.data?.forEach { (key, value) ->
                    val jsonValue = gson.toJson(value)
                    editor.putString(key, jsonValue)
                }
                editor.apply()
                onComplete(true)
            } else {
                onComplete(false)
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    private fun saveDataCloud(key: String, value: Any) {
        val userDoc = getUserDoc() ?: return
        // Usamos set con merge para asegurar que el documento existe y solo actualiza ese campo
        userDoc.set(mapOf(key to value), SetOptions.merge())
            .addOnFailureListener { e -> Log.e(TAG, "Error guardando $key en nube", e) }
    }

    // --- PERFIL ---
    fun setProfileImageUri(uri: String?) {
        getPrefs().edit().putString(KEY_PROFILE_IMAGE, uri).apply()
        uri?.let { saveDataCloud(KEY_PROFILE_IMAGE, it) }
    }

    fun getProfileImageUri(): String? = getPrefs().getString(KEY_PROFILE_IMAGE, null)

    fun setUserName(name: String) {
        val sanitizedName = if (name.length > 15) name.substring(0, 15) else name
        getPrefs().edit().putString(KEY_USER_NAME, sanitizedName).apply()
        saveDataCloud(KEY_USER_NAME, sanitizedName)
    }

    fun getUserName(): String {
        val firebaseUser = auth.currentUser
        // Prioridad 1: Nombre en el perfil de Firebase Auth
        if (!firebaseUser?.displayName.isNullOrEmpty()) return firebaseUser?.displayName!!
        // Prioridad 2: Nombre guardado localmente/nube en Firestore
        return getPrefs().getString(KEY_USER_NAME, "Usuario VEXO") ?: "Usuario VEXO"
    }

    // --- VALORACIONES Y DIARIO ---
    fun setMovieRating(movie: Movie, rating: Float, review: String? = null) {
        val ratings = getRatingsMap().toMutableMap()
        val ratedMovies = getRatedMoviesList().toMutableList()
        val diary = getDiary().toMutableList()

        if (rating <= 0) {
            ratings.remove(movie.id.toString())
            ratedMovies.removeAll { it.id == movie.id }
        } else {
            ratings[movie.id.toString()] = rating
            ratedMovies.removeAll { it.id == movie.id }
            ratedMovies.add(0, movie)

            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            diary.removeAll { it.movieId == movie.id } // Evitar duplicados en el diario al actualizar
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

        getPrefs().edit().apply {
            putString(KEY_RATINGS, gson.toJson(ratings))
            putString(KEY_RATED_MOVIES_DATA, gson.toJson(ratedMovies))
            putString(KEY_DIARY, gson.toJson(diary))
        }.apply()

        saveDataCloud(KEY_RATINGS, ratings)
        saveDataCloud(KEY_RATED_MOVIES_DATA, ratedMovies)
        saveDataCloud(KEY_DIARY, diary)
    }

    fun getMovieRating(movieId: Int): Float = getRatingsMap()[movieId.toString()] ?: 0f
    
    fun getRecentActivity(): List<Movie> = getRatedMoviesList().take(5)
    
    fun getAllRatedMovies(): List<Movie> = getRatedMoviesList()

    fun getDiary(): List<DiaryEntry> {
        val json = getPrefs().getString(KEY_DIARY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<DiaryEntry>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyList() }
    }

    private fun getRatingsMap(): Map<String, Float> {
        val json = getPrefs().getString(KEY_RATINGS, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Float>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyMap() }
    }

    private fun getRatedMoviesList(): List<Movie> {
        val json = getPrefs().getString(KEY_RATED_MOVIES_DATA, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Movie>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) { emptyList() }
    }

    // --- VITRINA ---
    fun getVitrinaMovies(): List<Movie?> {
        val json = getPrefs().getString(KEY_VITRINA, null) ?: return listOf(null, null, null, null)
        val type = object : TypeToken<List<Movie?>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { listOf(null, null, null, null) }
    }

    fun setVitrinaMovie(index: Int, movie: Movie?) {
        val vitrina = getVitrinaMovies().toMutableList()
        if (index in 0..3) {
            vitrina[index] = movie
            val json = gson.toJson(vitrina)
            getPrefs().edit().putString(KEY_VITRINA, json).apply()
            saveDataCloud(KEY_VITRINA, vitrina)
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
            val json = gson.toJson(vitrina)
            getPrefs().edit().putString(KEY_VITRINA, json).apply()
            saveDataCloud(KEY_VITRINA, vitrina)
            return 0
        }
        return 2
    }

    fun removeFromVitrina(movieId: Int) {
        val vitrina = getVitrinaMovies().toMutableList()
        val index = vitrina.indexOfFirst { it?.id == movieId }
        if (index != -1) {
            vitrina[index] = null
            val json = gson.toJson(vitrina)
            getPrefs().edit().putString(KEY_VITRINA, json).apply()
            saveDataCloud(KEY_VITRINA, vitrina)
        }
    }

    // --- GESTIÓN DE LISTAS ---
    fun getUserLists(): List<UserList> {
        val json = getPrefs().getString(KEY_CUSTOM_LISTS, null) ?: return emptyList()
        val type = object : TypeToken<List<UserList>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptyList() }
    }

    private fun saveUserLists(lists: List<UserList>) {
        getPrefs().edit().putString(KEY_CUSTOM_LISTS, gson.toJson(lists)).apply()
        saveDataCloud(KEY_CUSTOM_LISTS, lists)
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
    fun isFavorite(movieId: Int): Boolean = getUserLists().find { it.name == FAVORITES_LIST_NAME }?.movies?.any { it.id == movieId } ?: false
    fun isWatched(movieId: Int): Boolean = getUserLists().find { it.name == WATCHED_LIST_NAME }?.movies?.any { it.id == movieId } ?: false

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

    fun getStats(): UserStats {
        val ratings = getRatingsMap()
        val total = ratings.size
        val avg = if (total > 0) ratings.values.average().toFloat() else 0f
        return UserStats(total, avg)
    }

    data class UserStats(val totalMovies: Int, val averageRating: Float)
}
