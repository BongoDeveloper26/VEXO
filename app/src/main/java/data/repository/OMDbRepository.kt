package data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OMDbApi {
    @GET("/")
    suspend fun getMovieDetailsByTitle(
        @Query("apikey") apiKey: String,
        @Query("t") title: String
    ): OMDbMovieDTO

    @GET("/")
    suspend fun getMovieDetailsById(
        @Query("apikey") apiKey: String,
        @Query("i") id: String
    ): OMDbMovieDTO
}

data class OMDbMovieDTO(
    val Ratings: List<OMDbRating>? = null,
    val imdbRating: String? = null,
    val Metascore: String? = null,
    val Awards: String? = null
)

data class OMDbRating(
    val Source: String,
    val Value: String
)

class OMDbRepository {
    // Tu clave: c7126272
    private val apiKey = "c7126272"
    private val api: OMDbApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/") // CAMBIADO A HTTPS
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(OMDbApi::class.java)
    }

    suspend fun getMovieRatingsByTitle(title: String): OMDbMovieDTO? {
        return try {
            api.getMovieDetailsByTitle(apiKey, title)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMovieRatingsById(id: String): OMDbMovieDTO? {
        return try {
            api.getMovieDetailsById(apiKey, id)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private var instance: OMDbRepository? = null
        fun getInstance(): OMDbRepository {
            if (instance == null) instance = OMDbRepository()
            return instance!!
        }
    }
}
