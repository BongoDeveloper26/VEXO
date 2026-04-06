package data.repository

import android.os.Parcelable
import data.model.Movie
import kotlinx.parcelize.Parcelize
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBApi {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(@Query("api_key") apiKey: String, @Query("language") language: String, @Query("page") page: Int = 1): TMDBResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(@Query("api_key") apiKey: String, @Query("language") language: String, @Query("page") page: Int = 1): TMDBResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(@Query("api_key") apiKey: String, @Query("language") language: String, @Query("page") page: Int = 1): TMDBResponse

    @GET("discover/movie")
    suspend fun getMoviesByGenre(@Query("api_key") apiKey: String, @Query("with_genres") genreId: String, @Query("page") page: Int = 1, @Query("language") language: String, @Query("sort_by") sortBy: String = "vote_average.desc", @Query("vote_count.gte") voteCount: Int = 500): TMDBResponse

    @GET("discover/tv")
    suspend fun getTVByGenre(@Query("api_key") apiKey: String, @Query("with_genres") genreId: String, @Query("page") page: Int = 1, @Query("language") language: String, @Query("sort_by") sortBy: String = "vote_average.desc", @Query("vote_count.gte") voteCount: Int = 100): TMDBResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String, @Query("language") language: String, @Query("append_to_response") append: String = "belongs_to_collection,release_dates,watch/providers"): MovieDetailDTO

    @GET("movie/{movie_id}/credits")
    suspend fun getMovieCredits(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): MovieCreditsDTO

    @GET("movie/{movie_id}/recommendations")
    suspend fun getMovieRecommendations(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): TMDBResponse

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): MovieVideosDTO

    // --- TV ---
    @GET("trending/tv/week")
    suspend fun getTrendingTV(@Query("api_key") apiKey: String, @Query("language") language: String, @Query("page") page: Int = 1): TMDBResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTV(@Query("api_key") apiKey: String, @Query("language") language: String, @Query("page") page: Int = 1): TMDBResponse

    @GET("tv/{tv_id}")
    suspend fun getTVDetails(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String, @Query("language") language: String, @Query("append_to_response") append: String = "watch/providers,content_ratings,external_ids"): TVDetailDTO

    @GET("tv/{tv_id}/credits")
    suspend fun getTVCredits(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): MovieCreditsDTO

    @GET("tv/{tv_id}/recommendations")
    suspend fun getTVRecommendations(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): TMDBResponse

    @GET("tv/{tv_id}/videos")
    suspend fun getTVVideos(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): MovieVideosDTO

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getTVSeasonDetails(@Path("tv_id") tvId: Int, @Path("season_number") seasonNumber: Int, @Query("api_key") apiKey: String, @Query("language") language: String): TVSeasonDetailDTO

    // --- IMÁGENES ---
    @GET("movie/{movie_id}/images")
    suspend fun getMovieImages(@Path("movie_id") movieId: Int, @Query("api_key") apiKey: String, @Query("include_image_language") languages: String = "en,es,null,fr,de,it,pt"): MovieImagesDTO

    @GET("tv/{tv_id}/images")
    suspend fun getTVImages(@Path("tv_id") tvId: Int, @Query("api_key") apiKey: String, @Query("include_image_language") languages: String = "en,es,null,fr,de,it,pt"): MovieImagesDTO

    @GET("search/multi")
    suspend fun searchMulti(@Query("api_key") apiKey: String, @Query("query") query: String, @Query("language") language: String): TMDBResponse

    @GET("search/person")
    suspend fun searchPeople(@Query("api_key") apiKey: String, @Query("query") query: String, @Query("language") language: String): PersonSearchResponse

    @GET("person/{person_id}")
    suspend fun getPersonDetails(@Path("person_id") personId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): PersonDetailDTO

    @GET("person/{person_id}/movie_credits")
    suspend fun getPersonMovieCredits(@Path("person_id") personId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): PersonMovieCreditsDTO
    
    @GET("collection/{collection_id}")
    suspend fun getCollectionDetails(@Path("collection_id") collectionId: Int, @Query("api_key") apiKey: String, @Query("language") language: String): CollectionResponse
}

data class TMDBResponse(val results: List<MovieDTO>)
data class PersonSearchResponse(val results: List<PersonDTO>)
data class PersonDTO(val id: Int, val name: String, val profile_path: String?, val character: String? = null)

data class MovieDTO(
    val id: Int, val title: String?, val name: String?, val overview: String,
    val poster_path: String?, val backdrop_path: String?, val vote_average: Double,
    val release_date: String?, val first_air_date: String?, val genre_ids: List<Int>?,
    val media_type: String?
) {
    fun toMovie(): Movie {
        val isTv = media_type == "tv" || name != null
        return Movie(
            id = id, title = title ?: name ?: "Sin título", overview = overview,
            posterPath = poster_path?.let { "https://image.tmdb.org/t/p/w500$it" },
            backdropPath = backdrop_path?.let { "https://image.tmdb.org/t/p/w780$it" },
            rating = vote_average, genreIds = genre_ids ?: emptyList(), 
            releaseDate = release_date ?: first_air_date ?: "", isTvShow = isTv
        )
    }
}

data class MovieDetailDTO(
    val id: Int, val imdb_id: String?, val title: String, val original_title: String,
    val overview: String, val poster_path: String?, val backdrop_path: String?,
    val vote_average: Double, val release_date: String, val runtime: Int?,
    val genres: List<GenreDTO>, val tagline: String?, val budget: Long, val revenue: Long,
    val status: String, val original_language: String, val production_companies: List<ProductionCompanyDTO> = emptyList(),
    val production_countries: List<ProductionCountryDTO> = emptyList(),
    val belongs_to_collection: CollectionInfoDTO?, val release_dates: ReleaseDatesResponse?,
    @com.google.gson.annotations.SerializedName("watch/providers") val watchProviders: WatchProvidersResponse?
)

data class TVDetailDTO(
    val id: Int, val name: String, val original_name: String, val overview: String, val poster_path: String?, val backdrop_path: String?,
    val vote_average: Double, val first_air_date: String, val number_of_episodes: Int,
    val number_of_seasons: Int, val genres: List<GenreDTO>, val tagline: String?, val status: String,
    val original_language: String, val production_companies: List<ProductionCompanyDTO> = emptyList(),
    val production_countries: List<ProductionCountryDTO> = emptyList(),
    val content_ratings: TVContentRatingsResponse?, val external_ids: TVExternalIds?,
    @com.google.gson.annotations.SerializedName("watch/providers") val watchProviders: WatchProvidersResponse?
)

data class TVSeasonDetailDTO(
    val id: String,
    val name: String,
    val overview: String,
    val poster_path: String?,
    val season_number: Int,
    val episodes: List<TVEpisodeDTO>
)

data class TVEpisodeDTO(
    val id: Int,
    val name: String,
    val overview: String,
    val still_path: String?,
    val episode_number: Int,
    val vote_average: Double,
    val runtime: Int?
)

data class TVContentRatingsResponse(val results: List<TVContentRating>)
data class TVContentRating(val iso_3166_1: String, val rating: String)
data class TVExternalIds(val imdb_id: String?)

data class MovieVideosDTO(val results: List<VideoDTO>)
data class VideoDTO(val id: String, val key: String, val name: String, val site: String, val type: String)

data class GenreDTO(val id: Int, val name: String)
data class CollectionInfoDTO(val id: Int, val name: String, val poster_path: String?, val backdrop_path: String?)
data class CollectionResponse(val id: Int, val name: String, val parts: List<MovieDTO>)
data class ReleaseDatesResponse(val results: List<CountryReleaseDates>)
data class CountryReleaseDates(val iso_3166_1: String, val release_dates: List<ReleaseDateItem>)
data class ReleaseDateItem(val certification: String)
data class WatchProvidersResponse(val results: Map<String, WatchCountryProviders>)
data class WatchCountryProviders(
    val flatrate: List<WatchProviderItem>? = null,
    val buy: List<WatchProviderItem>? = null,
    val rent: List<WatchProviderItem>? = null
)

@Parcelize
data class WatchProviderItem(val provider_id: Int, val provider_name: String, val logo_path: String) : Parcelable

data class MovieImagesDTO(val posters: List<ImageDTO>, val backdrops: List<ImageDTO>)
data class ImageDTO(val file_path: String, val aspect_ratio: Double)
data class ProductionCompanyDTO(val id: Int, val name: String, val logo_path: String?, val origin_country: String)
data class ProductionCountryDTO(val iso_3166_1: String, val name: String)
data class MovieCreditsDTO(val cast: List<CastDTO>, val crew: List<CrewDTO>)
data class CastDTO(val id: Int, val name: String, val character: String, val profile_path: String?)
data class CrewDTO(val id: Int, val name: String, val job: String, val profile_path: String?)

data class PersonDetailDTO(val id: Int, val name: String, val biography: String?, val profile_path: String?, val known_for_department: String?, val birthday: String?, val place_of_birth: String?)
data class PersonMovieCreditsDTO(val cast: List<MovieDTO>, val crew: List<MovieDTO>)

class TMDBRepository {
    private val apiKey = "8998a694dcd2ced231502e3d00a40689"
    private var currentLanguage = "es-ES"
    private val api: TMDBApi

    init {
        val retrofit = Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/").addConverterFactory(GsonConverterFactory.create()).build()
        api = retrofit.create(TMDBApi::class.java)
    }

    fun setLanguage(language: String) { currentLanguage = language }
    fun getLanguage(): String = currentLanguage

    suspend fun getTrendingMovies(page: Int = 1): List<Movie> = try { api.getTrendingMovies(apiKey, currentLanguage, page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getTopRatedMovies(page: Int = 1): List<Movie> = try { api.getTopRatedMovies(apiKey, currentLanguage, page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getNowPlayingMovies(page: Int = 1): List<Movie> = try { api.getNowPlayingMovies(apiKey, currentLanguage, page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getTrendingTV(page: Int = 1): List<Movie> = try { api.getTrendingTV(apiKey, currentLanguage, page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getTopRatedTV(page: Int = 1): List<Movie> = try { api.getTopRatedTV(apiKey, currentLanguage, page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun searchAll(query: String): List<Movie> = try { api.searchMulti(apiKey, query, currentLanguage).results.filter { it.media_type == "movie" || it.media_type == "tv" }.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun searchPeople(query: String): List<PersonDTO> = try { api.searchPeople(apiKey, query, currentLanguage).results } catch (e: Exception) { emptyList() }
    suspend fun getMoviesByGenre(genreIds: List<Int>, page: Int = 1, sortBy: String = "vote_average.desc", voteCountGte: Int = 500): List<Movie> = try { api.getMoviesByGenre(apiKey, genreIds.joinToString(","), page, currentLanguage, sortBy, voteCountGte).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getTVByGenre(genreIds: List<Int>, page: Int = 1, sortBy: String = "vote_average.desc", voteCountGte: Int = 100): List<Movie> = try { api.getTVByGenre(apiKey, genreIds.joinToString(","), page, currentLanguage, sortBy, voteCountGte).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getMovieDetails(movieId: Int): MovieDetailDTO? = try { api.getMovieDetails(movieId, apiKey, currentLanguage) } catch (e: Exception) { null }
    suspend fun getTVDetails(tvId: Int): TVDetailDTO? = try { api.getTVDetails(tvId, apiKey, currentLanguage) } catch (e: Exception) { null }
    suspend fun getMovieRecommendations(movieId: Int): List<Movie> = try { api.getMovieRecommendations(movieId, apiKey, currentLanguage).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getTVRecommendations(tvId: Int): List<Movie> = try { api.getTVRecommendations(tvId, apiKey, currentLanguage).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getMovieCredits(movieId: Int): MovieCreditsDTO? = try { api.getMovieCredits(movieId, apiKey, currentLanguage) } catch (e: Exception) { null }
    suspend fun getTVCredits(tvId: Int): MovieCreditsDTO? = try { api.getTVCredits(tvId, apiKey, currentLanguage) } catch (e: Exception) { null }
    
    suspend fun getMovieImages(movieId: Int): List<String> = try { 
        val res = api.getMovieImages(movieId, apiKey)
        (res.posters + res.backdrops).map { "https://image.tmdb.org/t/p/original${it.file_path}" }
    } catch (e: Exception) { emptyList() }

    suspend fun getTVImages(tvId: Int): List<String> = try { 
        val res = api.getTVImages(tvId, apiKey)
        (res.posters + res.backdrops).map { "https://image.tmdb.org/t/p/original${it.file_path}" }
    } catch (e: Exception) { emptyList() }

    suspend fun getCollectionMovies(collectionId: Int): List<Movie> = try { api.getCollectionDetails(collectionId, apiKey, currentLanguage).parts.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    suspend fun getPersonDetails(personId: Int): PersonDetailDTO? = try { api.getPersonDetails(personId, apiKey, currentLanguage) } catch (e: Exception) { null }
    suspend fun getMoviesByPerson(personId: Int): List<Movie> = try { val response = api.getPersonMovieCredits(personId, apiKey, currentLanguage); (response.cast + response.crew).map { it.toMovie() }.distinctBy { it.id } } catch (e: Exception) { emptyList() }

    suspend fun getMovieTrailers(movieId: Int): List<VideoDTO> = try { 
        api.getMovieVideos(movieId, apiKey, currentLanguage).results.filter { it.site.lowercase() == "youtube" && it.type.lowercase() == "trailer" }
    } catch (e: Exception) { emptyList() }

    suspend fun getTVTrailers(tvId: Int): List<VideoDTO> = try { 
        api.getTVVideos(tvId, apiKey, currentLanguage).results.filter { it.site.lowercase() == "youtube" && it.type.lowercase() == "trailer" }
    } catch (e: Exception) { emptyList() }

    suspend fun getTVSeasonDetails(tvId: Int, seasonNumber: Int): TVSeasonDetailDTO? = try {
        api.getTVSeasonDetails(tvId, seasonNumber, apiKey, currentLanguage)
    } catch (e: Exception) { null }

    companion object {
        private var instance: TMDBRepository? = null
        fun getInstance(): TMDBRepository { if (instance == null) instance = TMDBRepository(); return instance!! }
    }
}
