package data.repository

import data.model.Movie
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBApi {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1
    ): TMDBResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1
    ): TMDBResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1
    ): TMDBResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String
    ): TMDBResponse

    @GET("search/person")
    suspend fun searchPeople(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String
    ): PersonSearchResponse

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: String,
        @Query("page") page: Int = 1,
        @Query("language") language: String,
        @Query("sort_by") sortBy: String = "vote_average.desc",
        @Query("vote_count.gte") voteCount: Int = 500,
        @Query("release_date.lte") releaseDateLte: String? = null
    ): TMDBResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("append_to_response") append: String = "belongs_to_collection,release_dates,watch/providers"
    ): MovieDetailDTO

    @GET("movie/{movie_id}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): TMDBResponse

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): MovieCreditsDTO

    @GET("person/{person_id}/movie_credits")
    suspend fun getPersonMovieCredits(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): PersonMovieCreditsDTO

    @GET("person/{person_id}")
    suspend fun getPersonDetails(
        @Path("person_id") personId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): PersonDetailDTO

    @GET("collection/{collection_id}")
    suspend fun getCollectionDetails(
        @Path("collection_id") collectionId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String
    ): CollectionResponse

    @GET("movie/{movie_id}/images")
    suspend fun getMovieImages(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): MovieImagesDTO
}

data class TMDBResponse(
    val results: List<MovieDTO>
)

data class PersonSearchResponse(
    val results: List<PersonDTO>
)

data class PersonDTO(
    val id: Int,
    val name: String,
    val profile_path: String?,
    val character: String? = null,
    val known_for: List<MovieDTO>? = null
)

data class MovieDTO(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double,
    val release_date: String?,
    val genre_ids: List<Int>?
) {
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterPath = poster_path?.let { "https://image.tmdb.org/t/p/w500$it" },
            backdropPath = backdrop_path?.let { "https://image.tmdb.org/t/p/w780$it" },
            rating = vote_average,
            genreIds = genre_ids ?: emptyList()
        )
    }
}

data class MovieDetailDTO(
    val id: Int,
    val imdb_id: String? = null,
    val title: String,
    val original_title: String,
    val overview: String,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double,
    val release_date: String,
    val runtime: Int?,
    val genres: List<GenreDTO>,
    val tagline: String?,
    val budget: Long,
    val revenue: Long,
    val status: String,
    val original_language: String,
    val production_companies: List<ProductionCompanyDTO>,
    val production_countries: List<ProductionCountryDTO>,
    val belongs_to_collection: CollectionInfoDTO?,
    val release_dates: ReleaseDatesResponse?,
    @com.google.gson.annotations.SerializedName("watch/providers")
    val watchProviders: WatchProvidersResponse?
)

data class ReleaseDatesResponse(val results: List<CountryReleaseDates>)
data class CountryReleaseDates(val iso_3166_1: String, val release_dates: List<ReleaseDateItem>)
data class ReleaseDateItem(val certification: String)

data class WatchProvidersResponse(val results: Map<String, WatchCountryProviders>)
data class WatchCountryProviders(
    val flatrate: List<WatchProviderItem>? = null,
    val rent: List<WatchProviderItem>? = null,
    val buy: List<WatchProviderItem>? = null
)
data class WatchProviderItem(val provider_id: Int, val provider_name: String, val logo_path: String)

data class MovieImagesDTO(
    val posters: List<ImageDTO>,
    val backdrops: List<ImageDTO>
)

data class ImageDTO(
    val file_path: String,
    val aspect_ratio: Double
)

data class CollectionInfoDTO(
    val id: Int,
    val name: String,
    val poster_path: String?,
    val backdrop_path: String?
)

data class CollectionResponse(
    val id: Int,
    val name: String,
    val parts: List<MovieDTO>
)

data class ProductionCompanyDTO(
    val id: Int,
    val name: String,
    val logo_path: String?,
    val origin_country: String
)

data class ProductionCountryDTO(
    val iso_3166_1: String,
    val name: String
)

data class GenreDTO(
    val id: Int,
    val name: String
)

data class MovieCreditsDTO(
    val cast: List<CastDTO>,
    val crew: List<CrewDTO>
)

data class CastDTO(
    val id: Int,
    val name: String,
    val character: String,
    val profile_path: String?
)

data class CrewDTO(
    val id: Int,
    val name: String,
    val job: String,
    val profile_path: String?
)

data class PersonMovieCreditsDTO(
    val cast: List<MovieDTO>,
    val crew: List<MovieDTO>
)

data class PersonDetailDTO(
    val id: Int,
    val name: String,
    val biography: String?,
    val profile_path: String?,
    val known_for_department: String?,
    val birthday: String?,
    val place_of_birth: String?,
    val popularity: Double?
)

class TMDBRepository {

    private val apiKey = "8998a694dcd2ced231502e3d00a40689"
    private var currentLanguage = "es-ES"

    private val api: TMDBApi

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(TMDBApi::class.java)
    }

    fun setLanguage(language: String) {
        currentLanguage = language
    }

    fun getLanguage(): String = currentLanguage

    suspend fun getTrendingMovies(page: Int = 1): List<Movie> {
        return try {
            val response = api.getTrendingMovies(apiKey, currentLanguage, page)
            response.results.map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTopRatedMovies(page: Int = 1): List<Movie> {
        return try {
            val response = api.getTopRatedMovies(apiKey, currentLanguage, page)
            response.results.map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getNowPlayingMovies(page: Int = 1): List<Movie> {
        return try {
            val response = api.getNowPlayingMovies(apiKey, currentLanguage, page)
            response.results.map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchMovies(query: String): List<Movie> {
        return try {
            val response = api.searchMovies(apiKey, query, currentLanguage)
            response.results.map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchPeople(query: String): List<PersonDTO> {
        return try {
            val response = api.searchPeople(apiKey, query, currentLanguage)
            response.results
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMoviesByGenre(
        genreIds: List<Int>, 
        page: Int = 1, 
        sortBy: String = "vote_average.desc",
        voteCountGte: Int = 100
    ): List<Movie> {
        return try {
            val genreString = genreIds.joinToString(",")
            val response = api.getMoviesByGenre(
                apiKey = apiKey, 
                genreId = genreString, 
                page = page, 
                language = currentLanguage,
                sortBy = sortBy,
                voteCount = voteCountGte
            )
            response.results.map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMovieDetails(movieId: Int): MovieDetailDTO? {
        return try {
            api.getMovieDetails(movieId, apiKey, currentLanguage)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMovieRecommendations(movieId: Int): List<Movie> {
        return try {
            val response = api.getMovieRecommendations(movieId, apiKey, currentLanguage)
            response.results.map { it.toMovie() }.sortedByDescending { it.rating }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCollectionMovies(collectionId: Int): List<Movie> {
        return try {
            val response = api.getCollectionDetails(collectionId, apiKey, currentLanguage)
            response.parts.map { it.toMovie() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getMovieCredits(movieId: Int): MovieCreditsDTO? {
        return try {
            api.getMovieCredits(movieId, apiKey, currentLanguage)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMoviesByPerson(personId: Int): List<Movie> {
        return try {
            val response = api.getPersonMovieCredits(personId, apiKey, currentLanguage)
            val allMovies = (response.cast + response.crew)
                .map { it.toMovie() }
                .distinctBy { it.id }
                .sortedByDescending { it.rating }
            allMovies
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPersonDetails(personId: Int): PersonDetailDTO? {
        return try {
            api.getPersonDetails(personId, apiKey, currentLanguage)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMovieImages(movieId: Int): List<String> {
        return try {
            val response = api.getMovieImages(movieId, apiKey)
            response.posters.map { "https://image.tmdb.org/t/p/original${it.file_path}" }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    companion object {
        private var instance: TMDBRepository? = null
        
        fun getInstance(): TMDBRepository {
            if (instance == null) {
                instance = TMDBRepository()
            }
            return instance!!
        }
    }
}
