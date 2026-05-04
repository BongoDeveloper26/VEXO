package data.api

import data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {
    @GET("everything")
    suspend fun getMovieNews(
        @Query("q") query: String,
        @Query("language") language: String = "es",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("searchIn") searchIn: String? = null,
        @Query("domains") domains: String? = null,
        @Query("apiKey") apiKey: String = "0633bd51c20545028d04efe81cde61ed"
    ): Response<NewsResponse>
}
