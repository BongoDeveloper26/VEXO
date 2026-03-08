package data.repository

import android.util.Log
import data.model.Movie
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// 1️⃣ Define la API de TMDB
interface TMDBApi {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String
    ): TMDBResponse
}

// 2️⃣ Modelo de respuesta de TMDB
data class TMDBResponse(
    val results: List<MovieDTO>
)

data class MovieDTO(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val vote_average: Double
) {
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterPath = poster_path?.let {
                "https://image.tmdb.org/t/p/w500$it"
            },
            rating = vote_average
        )
    }
}

// 3️⃣ Repositorio
class TMDBRepository {

    private val apiKey = "8998a694dcd2ced231502e3d00a40689"

    private val api: TMDBApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(TMDBApi::class.java)
    }

    suspend fun getTrendingMovies(): List<Movie> {
        return try {
            Log.d("TMDB_DEBUG", "Llamando a TMDB con API key: $apiKey")

            val response = api.getTrendingMovies(apiKey)

            Log.d("TMDB_DEBUG", "Películas recibidas: ${response.results.size}")

            response.results.map { it.toMovie() }

        } catch (e: Exception) {
            Log.e("TMDB_DEBUG", "Error al llamar a TMDB: ${e.message}")
            emptyList()
        }
    }
}
