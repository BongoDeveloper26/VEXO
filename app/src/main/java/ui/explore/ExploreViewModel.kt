package ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.model.Movie
import data.model.Category
import data.repository.TMDBRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    private val repository = TMDBRepository.getInstance()
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    fun loadAllCategories() {
        viewModelScope.launch {
            val isSpanish = repository.getLanguage() == "es-ES"
            val list = mutableListOf<Category>()

            // 1-4. Categorías Principales
            val trending = repository.getTrendingMovies()
            if (trending.isNotEmpty()) list.add(Category(if (isSpanish) "Películas en Tendencia" else "Trending Movies", trending))

            val trendingTV = repository.getTrendingTV()
            if (trendingTV.isNotEmpty()) list.add(Category(if (isSpanish) "Series en Tendencia" else "Trending TV Shows", trendingTV))

            val topRated = repository.getTopRatedMovies()
            if (topRated.isNotEmpty()) list.add(Category(if (isSpanish) "Mejor Valoradas" else "Top Rated", topRated))

            val nowPlaying = repository.getNowPlayingMovies()
            if (nowPlaying.isNotEmpty()) list.add(Category(if (isSpanish) "En Cines" else "Now Playing", nowPlaying))

            // 5. RESTAURACIÓN COMPLETA DE GÉNEROS (19 categorías)
            val genreMap = mapOf(
                28 to if (isSpanish) "Acción" else "Action",
                12 to if (isSpanish) "Aventura" else "Adventure",
                16 to if (isSpanish) "Animación" else "Animation",
                35 to if (isSpanish) "Comedia" else "Comedy",
                80 to if (isSpanish) "Crimen" else "Crime",
                99 to if (isSpanish) "Documental" else "Documentary",
                18 to if (isSpanish) "Drama" else "Drama",
                10751 to if (isSpanish) "Familia" else "Family",
                14 to if (isSpanish) "Fantasía" else "Fantasy",
                36 to if (isSpanish) "Historia" else "History",
                27 to if (isSpanish) "Terror" else "Horror",
                10402 to if (isSpanish) "Música" else "Music",
                9648 to if (isSpanish) "Misterio" else "Mystery",
                10749 to if (isSpanish) "Romance" else "Romance",
                878 to if (isSpanish) "Ciencia Ficción" else "Sci-Fi",
                53 to if (isSpanish) "Suspense" else "Thriller",
                10752 to if (isSpanish) "Bélica" else "War",
                37 to if (isSpanish) "Western" else "Western",
                10770 to if (isSpanish) "Películas de TV" else "TV Movie"
            )

            val genreJobs = genreMap.map { (id, name) ->
                async {
                    val movies = repository.getMoviesByGenre(listOf(id))
                    if (movies.isNotEmpty()) Category(name, movies) else null
                }
            }
            
            list.addAll(genreJobs.awaitAll().filterNotNull())
            _categories.value = list
        }
    }
}
