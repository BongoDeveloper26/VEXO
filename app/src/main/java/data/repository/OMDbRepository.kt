package data.repository

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OMDbApi {
    @GET("/")
    suspend fun getMovieDetailsByTitle(
        @Query("apikey") apiKey: String,
        @Query("t") title: String,
        @Query("y") year: String? = null
    ): OMDbMovieDTO

    @GET("/")
    suspend fun getMovieDetailsById(
        @Query("apikey") apiKey: String,
        @Query("i") id: String
    ): OMDbMovieDTO
}

data class OMDbMovieDTO(
    @SerializedName("Ratings") val Ratings: List<OMDbRating>? = null,
    @SerializedName("imdbRating") val imdbRating: String? = null,
    @SerializedName("Metascore") val Metascore: String? = null,
    @SerializedName("Awards") val Awards: String? = null,
    @SerializedName("Plot") val Plot: String? = null
)

data class OMDbRating(
    @SerializedName("Source") val Source: String,
    @SerializedName("Value") val Value: String
)

class OMDbRepository {
    private val apiKey = "c7126272"
    private val api: OMDbApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.omdbapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(OMDbApi::class.java)
    }

    suspend fun getMovieRatingsByTitle(title: String, year: String? = null): OMDbMovieDTO? {
        return try {
            api.getMovieDetailsByTitle(apiKey, title, year)
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
