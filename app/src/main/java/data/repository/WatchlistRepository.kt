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
    
    val userId: String? get() = auth.currentUser?.uid

    private fun getUserDoc() = userId?.let { firestore.collection("users").document(it) }

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
        private const val KEY_HEADER_BACKGROUND = "user_header_background"
        private const val KEY_HEADER_TRANSPARENT = "user_header_transparent"
        private const val KEY_HEADER_COLOR = "user_header_color"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_SEEN_ACHIEVEMENTS = "seen_achievements_count"
        private const val KEY_LIKED_LISTS = "user_liked_lists_ids"
        private const val TAG = "WatchlistRepository"

        // Caché estática compartida entre todas las instancias del repositorio
        private var cachedLists: List<UserList>? = null
        private var cachedRatings: Map<String, Float>? = null
        private var cachedDiary: List<DiaryEntry>? = null
        private var cachedRatedMovies: List<Movie>? = null
        
        fun clearMemoryCache() {
            cachedLists = null
            cachedRatings = null
            cachedDiary = null
            cachedRatedMovies = null
        }
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
                clearMemoryCache()
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
        userDoc.set(mapOf(key to value), SetOptions.merge())
            .addOnFailureListener { e -> Log.e(TAG, "Error guardando $key en nube", e) }
    }

    // --- LIKES DE LISTAS ---
    fun isListLiked(listId: String): Boolean {
        val likedSet = getPrefs().getStringSet(KEY_LIKED_LISTS, emptySet()) ?: emptySet()
        return likedSet.contains(listId)
    }

    fun setListLiked(listId: String, liked: Boolean) {
        val likedSet = getPrefs().getStringSet(KEY_LIKED_LISTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (liked) likedSet.add(listId) else likedSet.remove(listId)
        getPrefs().edit().putStringSet(KEY_LIKED_LISTS, likedSet).apply()
        saveDataCloud(KEY_LIKED_LISTS, likedSet.toList())
    }

    // --- PERFIL ---
    fun setProfileImageUri(uri: String?) {
        getPrefs().edit().putString(KEY_PROFILE_IMAGE, uri).apply()
        uri?.let { saveDataCloud(KEY_PROFILE_IMAGE, it) }
    }

    fun getProfileImageUri(): String? = getPrefs().getString(KEY_PROFILE_IMAGE, null)

    fun setHeaderBackground(resName: String?) {
        getPrefs().edit().putString(KEY_HEADER_BACKGROUND, resName).apply()
        resName?.let { saveDataCloud(KEY_HEADER_BACKGROUND, it) }
    }

    fun getHeaderBackground(): String? = getPrefs().getString(KEY_HEADER_BACKGROUND, null)

    fun isHeaderTransparent(): Boolean = getPrefs().getBoolean(KEY_HEADER_TRANSPARENT, false)

    fun setHeaderTransparent(transparent: Boolean) {
        getPrefs().edit().putBoolean(KEY_HEADER_TRANSPARENT, transparent).apply()
        saveDataCloud(KEY_HEADER_TRANSPARENT, transparent)
    }

    fun getHeaderColor(): String? = getPrefs().getString(KEY_HEADER_COLOR, null)

    fun setHeaderColor(colorHex: String?) {
        getPrefs().edit().putString(KEY_HEADER_COLOR, colorHex).apply()
        colorHex?.let { saveDataCloud(KEY_HEADER_COLOR, it) }
    }

    fun setUserName(name: String) {
        val sanitizedName = if (name.length > 15) name.substring(0, 15) else name
        getPrefs().edit().putString(KEY_USER_NAME, sanitizedName).apply()
        saveDataCloud(KEY_USER_NAME, sanitizedName)
    }

    fun getUserName(): String {
        val firebaseUser = auth.currentUser
        if (!firebaseUser?.displayName.isNullOrEmpty()) return firebaseUser?.displayName!!
        return getPrefs().getString(KEY_USER_NAME, "Usuario VEXO") ?: "Usuario VEXO"
    }

    // --- LOGROS ---
    fun setSeenAchievementsCount(count: Int) {
        getPrefs().edit().putInt(KEY_SEEN_ACHIEVEMENTS, count).apply()
        saveDataCloud(KEY_SEEN_ACHIEVEMENTS, count)
    }

    fun getSeenAchievementsCount(): Int = getPrefs().getInt(KEY_SEEN_ACHIEVEMENTS, 0)

    // --- VALORACIONES Y DIARIO ---
    fun setMovieRating(movie: Movie, rating: Float, review: String? = null) {
        val ratings = getRatingsMap().toMutableMap()
        val ratedMovies = getRatedMoviesList().toMutableList()
        val diary = getDiary().toMutableList()

        if (rating <= 0) {
            ratings.remove(movie.id.toString())
            ratedMovies.removeAll { it.id == movie.id }
            diary.removeAll { it.movieId == movie.id }
        } else {
            ratings[movie.id.toString()] = rating
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

        cachedRatings = ratings
        cachedRatedMovies = ratedMovies
        cachedDiary = diary

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
        cachedDiary?.let { return it }
        val json = getPrefs().getString(KEY_DIARY, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<DiaryEntry>>() {}.type
            val result: List<DiaryEntry> = gson.fromJson(json, type)
            cachedDiary = result
            result
        } catch (e: Exception) { emptyList() }
    }

    private fun getRatingsMap(): Map<String, Float> {
        cachedRatings?.let { return it }
        val json = getPrefs().getString(KEY_RATINGS, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Float>>() {}.type
            val result: Map<String, Float> = gson.fromJson(json, type)
            cachedRatings = result
            result
        } catch (e: Exception) { emptyMap() }
    }

    private fun getRatedMoviesList(): List<Movie> {
        cachedRatedMovies?.let { return it }
        val json = getPrefs().getString(KEY_RATED_MOVIES_DATA, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Movie>>() {}.type
            val result: List<Movie> = gson.fromJson(json, type)
            cachedRatedMovies = result
            result
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

    fun isMovieInVitrina(movieId: Int): Boolean = getVitrinaMovies().any { it?.id == movieId }

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
        cachedLists?.let { return it }
        val json = getPrefs().getString(KEY_CUSTOM_LISTS, null) ?: return emptyList()
        val type = object : TypeToken<List<UserList>>() {}.type
        return try { 
            val result: List<UserList> = gson.fromJson(json, type)
            cachedLists = result
            result
        } catch (e: Exception) { emptyList() }
    }

    private fun saveUserLists(lists: List<UserList>) {
        cachedLists = lists
        getPrefs().edit().putString(KEY_CUSTOM_LISTS, gson.toJson(lists)).apply()
        saveDataCloud(KEY_CUSTOM_LISTS, lists)
    }

    fun createUserList(name: String, description: String? = null, isPublic: Boolean = false): String {
        val lists = getUserLists().toMutableList()
        val id = UUID.randomUUID().toString()
        lists.add(UserList(id = id, name = name, description = description, isPublic = isPublic))
        saveUserLists(lists)
        return id
    }

    fun updateUserList(listId: String, name: String, description: String?, isPublic: Boolean = false) {
        val lists = getUserLists().toMutableList()
        val index = lists.indexOfFirst { it.id == listId }
        if (index != -1) {
            lists[index] = lists[index].copy(name = name, description = description, isPublic = isPublic)
            saveUserLists(lists)
        }
    }

    fun updateUserListLikes(listId: String, likes: Int) {
        val lists = getUserLists().toMutableList()
        val index = lists.indexOfFirst { it.id == listId }
        if (index != -1) {
            lists[index] = lists[index].copy(likes = likes)
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
        var targetIndex = lists.indexOfFirst { it.name == listName }
        
        if (targetIndex == -1) {
            lists.add(UserList(id = UUID.randomUUID().toString(), name = listName))
            targetIndex = lists.size - 1
        }
        
        val targetList = lists[targetIndex]
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