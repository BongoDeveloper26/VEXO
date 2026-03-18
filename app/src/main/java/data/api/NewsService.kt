package data.api

import data.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {
    /**
     * Filtro de Élite Vexo:
     * - qInTitle: Fuerza a que el tema principal sea cine/actores.
     * - Filtros negativos (-): Eliminamos ruido de bolsa, política y economía.
     */
    @GET("everything")
    suspend fun getMovieNews(
        @Query("qInTitle") query: String = "(película OR cine OR estreno OR tráiler OR reparto OR Oscars OR Hollywood) -bolsa -acciones -política -economía -sucesos",
        @Query("language") language: String = "es",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("apiKey") apiKey: String = "0633bd51c20545028d04efe81cde61ed"
    ): Response<NewsResponse>
}
