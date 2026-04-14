package data.repository

import android.os.Parcelable
import data.model.Movie
import kotlinx.parcelize.Parcelize
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import androidx.appcompat.app.AppCompatDelegate
import java.util.Locale

interface TMDBApi {

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(@Query("api_key") apiKey: String, @Query("language") language: String, @Query("page") page: Int = 1): TMDBResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(@Query("api_key") apiKey: String, @Query("language") language: String, @Query("page") page: Int = 1): TMDBResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(@Query("api_key") apiKey: String, @Query("language") language: String, @Query("page") page: Int = 1): TMDBResponse

    @GET("discover/movie")
    suspend fun getMoviesByGenre(@Query("api_key") apiKey: String, @Query("with_genres") genreId: String, @Query("page") page: Int = 1, @Query("language") language: String, @Query("sort_by") sortBy: String = "vote_average.desc", @Query("vote_count.gte") voteCount: Int = 500): TMDBResponse

    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") genres: String? = null,
        @Query("primary_release_date.gte") dateGte: String? = null,
        @Query("primary_release_date.lte") dateLte: String? = null,
        @Query("vote_average.gte") minRating: Float? = null,
        @Query("vote_count.gte") minVoteCount: Int? = null
    ): TMDBResponse

    @GET("discover/tv")
    suspend fun discoverTV(
        @Query("api_key") apiKey: String,
        @Query("language") language: String,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("with_genres") genres: String? = null,
        @Query("first_air_date.gte") dateGte: String? = null,
        @Query("first_air_date.lte") dateLte: String? = null,
        @Query("vote_average.gte") minRating: Float? = null
    ): TMDBResponse

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
    private val api: TMDBApi
    private val cache = mutableMapOf<String, Any>()

    init {
        val retrofit = Retrofit.Builder().baseUrl("https://api.themoviedb.org/3/").addConverterFactory(GsonConverterFactory.create()).build()
        api = retrofit.create(TMDBApi::class.java)
    }

    fun getLanguage(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) {
            val lang = locales.get(0)?.language
            return if (lang == "es") "es-ES" else "en-US"
        }
        return if (Locale.getDefault().language == "es") "es-ES" else "en-US"
    }

    fun clearCache() {
        cache.clear()
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> withCache(key: String, block: suspend () -> T): T {
        val cacheKey = "${getLanguage()}_$key"
        cache[cacheKey]?.let { return it as T }
        val result = block()
        if (result != null && (result as? List<*>)?.isNotEmpty() != false) {
            cache[cacheKey] = result as Any
        }
        return result
    }

    suspend fun getTrendingMovies(page: Int = 1): List<Movie> = withCache("trending_movies_$page") {
        try { api.getTrendingMovies(apiKey, getLanguage(), page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun getTopRatedMovies(page: Int = 1): List<Movie> = withCache("top_rated_movies_$page") {
        try { api.getTopRatedMovies(apiKey, getLanguage(), page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun getNowPlayingMovies(page: Int = 1): List<Movie> = withCache("now_playing_movies_$page") {
        try { api.getNowPlayingMovies(apiKey, getLanguage(), page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun getTrendingTV(page: Int = 1): List<Movie> = withCache("trending_tv_$page") {
        try { api.getTrendingTV(apiKey, getLanguage(), page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun getTopRatedTV(page: Int = 1): List<Movie> = withCache("top_rated_tv_$page") {
        try { api.getTopRatedTV(apiKey, getLanguage(), page).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun searchAll(query: String): List<Movie> = try { api.searchMulti(apiKey, query, getLanguage()).results.filter { it.media_type == "movie" || it.media_type == "tv" }.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    
    suspend fun searchPeople(query: String): List<PersonDTO> = try { api.searchPeople(apiKey, query, getLanguage()).results } catch (e: Exception) { emptyList() }
    
    suspend fun getMoviesByGenre(genreIds: List<Int>, page: Int = 1, sortBy: String = "vote_average.desc", voteCountGte: Int = 500): List<Movie> = withCache("movies_genre_${genreIds.joinToString(",")}_$page") {
        try { api.getMoviesByGenre(apiKey, genreIds.joinToString(","), page, getLanguage(), sortBy, voteCountGte).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun getTVByGenre(genreIds: List<Int>, page: Int = 1, sortBy: String = "vote_average.desc", voteCountGte: Int = 100): List<Movie> = withCache("tv_genre_${genreIds.joinToString(",")}_$page") {
        try { api.getTVByGenre(apiKey, genreIds.joinToString(","), page, getLanguage(), sortBy, voteCountGte).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun discoverMovies(
        genreIds: List<Int>? = null,
        yearStart: Int? = null,
        yearEnd: Int? = null,
        minRating: Float? = null,
        sortBy: String = "popularity.desc",
        page: Int = 1
    ): List<Movie> = withCache("discover_movies_${genreIds?.joinToString(",")}_${yearStart}_${yearEnd}_${minRating}_${sortBy}_$page") {
        try { 
            api.discoverMovies(
                apiKey, getLanguage(), page, sortBy, 
                genreIds?.joinToString(","), 
                yearStart?.let { "$it-01-01" },
                yearEnd?.let { "$it-12-31" },
                minRating, 
                if (minRating != null) 100 else null
            ).results.map { it.toMovie() }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun discoverTV(
        genreIds: List<Int>? = null,
        yearStart: Int? = null,
        yearEnd: Int? = null,
        minRating: Float? = null,
        sortBy: String = "popularity.desc",
        page: Int = 1
    ): List<Movie> = withCache("discover_tv_${genreIds?.joinToString(",")}_${yearStart}_${yearEnd}_${minRating}_${sortBy}_$page") {
        try { 
            api.discoverTV(
                apiKey, getLanguage(), page, sortBy, 
                genreIds?.joinToString(","), 
                yearStart?.let { "$it-01-01" },
                yearEnd?.let { "$it-12-31" },
                minRating
            ).results.map { it.toMovie() }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getMovieDetails(movieId: Int): MovieDetailDTO? = withCache("movie_details_$movieId") {
        try { api.getMovieDetails(movieId, apiKey, getLanguage()) } catch (e: Exception) { null }
    }

    suspend fun getTVDetails(tvId: Int): TVDetailDTO? = withCache("tv_details_$tvId") {
        try { api.getTVDetails(tvId, apiKey, getLanguage()) } catch (e: Exception) { null }
    }

    suspend fun getMovieRecommendations(movieId: Int): List<Movie> = withCache("movie_rec_$movieId") {
        try { api.getMovieRecommendations(movieId, apiKey, getLanguage()).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun getTVRecommendations(tvId: Int): List<Movie> = withCache("tv_rec_$tvId") {
        try { api.getTVRecommendations(tvId, apiKey, getLanguage()).results.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun getMovieCredits(movieId: Int): MovieCreditsDTO? = withCache("movie_credits_$movieId") {
        try { api.getMovieCredits(movieId, apiKey, getLanguage()) } catch (e: Exception) { null }
    }

    suspend fun getTVCredits(tvId: Int): MovieCreditsDTO? = withCache("tv_credits_$tvId") {
        try { api.getTVCredits(tvId, apiKey, getLanguage()) } catch (e: Exception) { null }
    }
    
    suspend fun getMovieImages(movieId: Int): List<String> = withCache("movie_images_$movieId") {
        try { 
            val res = api.getMovieImages(movieId, apiKey)
            (res.posters + res.backdrops).map { "https://image.tmdb.org/t/p/original${it.file_path}" }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getTVImages(tvId: Int): List<String> = withCache("tv_images_$tvId") {
        try { 
            val res = api.getTVImages(tvId, apiKey)
            (res.posters + res.backdrops).map { "https://image.tmdb.org/t/p/original${it.file_path}" }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getCollectionMovies(collectionId: Int): List<Movie> = withCache("collection_$collectionId") {
        try { api.getCollectionDetails(collectionId, apiKey, getLanguage()).parts.map { it.toMovie() } } catch (e: Exception) { emptyList() }
    }

    suspend fun getPersonDetails(personId: Int): PersonDetailDTO? = withCache("person_details_$personId") {
        try { api.getPersonDetails(personId, apiKey, getLanguage()) } catch (e: Exception) { null }
    }

    suspend fun getMoviesByPerson(personId: Int): List<Movie> = withCache("person_movies_$personId") {
        try { val response = api.getPersonMovieCredits(personId, apiKey, getLanguage()); (response.cast + response.crew).map { it.toMovie() }.distinctBy { it.id } } catch (e: Exception) { emptyList() }
    }

    suspend fun getMovieTrailers(movieId: Int): List<VideoDTO> = withCache("movie_trailers_$movieId") {
        try { 
            api.getMovieVideos(movieId, apiKey, getLanguage()).results.filter { it.site.lowercase() == "youtube" && it.type.lowercase() == "trailer" }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getTVTrailers(tvId: Int): List<VideoDTO> = withCache("tv_trailers_$tvId") {
        try { 
            api.getTVVideos(tvId, apiKey, getLanguage()).results.filter { it.site.lowercase() == "youtube" && it.type.lowercase() == "trailer" }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getTVSeasonDetails(tvId: Int, seasonNumber: Int): TVSeasonDetailDTO? = withCache("tv_season_${tvId}_$seasonNumber") {
        try {
            api.getTVSeasonDetails(tvId, seasonNumber, apiKey, getLanguage())
        } catch (e: Exception) { null }
    }

    companion object {
        private var instance: TMDBRepository? = null
        fun getInstance(): TMDBRepository { if (instance == null) instance = TMDBRepository(); return instance!! }
    }
}
