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
    
    private val _movieCategories = MutableLiveData<List<Category>>()
    val movieCategories: LiveData<List<Category>> = _movieCategories

    private val _tvCategories = MutableLiveData<List<Category>>()
    val tvCategories: LiveData<List<Category>> = _tvCategories

    fun loadMovies() {
        viewModelScope.launch {
            val isSpanish = repository.getLanguage() == "es-ES"
            val list = mutableListOf<Category>()

            val trending = repository.getTrendingMovies()
            if (trending.isNotEmpty()) list.add(Category(if (isSpanish) "Películas en Tendencia" else "Trending Movies", trending))

            val topRated = repository.getTopRatedMovies()
            if (topRated.isNotEmpty()) list.add(Category(if (isSpanish) "Películas Mejor Valoradas" else "Top Rated Movies", topRated))

            val nowPlaying = repository.getNowPlayingMovies()
            if (nowPlaying.isNotEmpty()) list.add(Category(if (isSpanish) "En Cines" else "Now Playing", nowPlaying))

            val genreMap = getMovieGenres(isSpanish)
            val genreJobs = genreMap.map { (id, name) ->
                async {
                    val movies = repository.getMoviesByGenre(listOf(id))
                    if (movies.isNotEmpty()) Category(name, movies) else null
                }
            }
            list.addAll(genreJobs.awaitAll().filterNotNull())
            _movieCategories.value = list
        }
    }

    fun loadTVShows() {
        viewModelScope.launch {
            val isSpanish = repository.getLanguage() == "es-ES"
            val list = mutableListOf<Category>()

            val trendingTV = repository.getTrendingTV()
            if (trendingTV.isNotEmpty()) list.add(Category(if (isSpanish) "Series en Tendencia" else "Trending TV Shows", trendingTV))

            val topRatedTV = repository.getTopRatedTV()
            if (topRatedTV.isNotEmpty()) list.add(Category(if (isSpanish) "Series Mejor Valoradas" else "Top Rated TV Shows", topRatedTV))

            val genreMap = getTVGenres(isSpanish)
            val genreJobs = genreMap.map { (id, name) ->
                async {
                    val tvShows = repository.getTVByGenre(listOf(id))
                    if (tvShows.isNotEmpty()) Category(name, tvShows) else null
                }
            }
            list.addAll(genreJobs.awaitAll().filterNotNull())
            _tvCategories.value = list
        }
    }

    private fun getMovieGenres(isSpanish: Boolean) = mapOf(
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
        37 to if (isSpanish) "Western" else "Western"
    )

    private fun getTVGenres(isSpanish: Boolean) = mapOf(
        10759 to if (isSpanish) "Acción y Aventura" else "Action & Adventure",
        16 to if (isSpanish) "Animación" else "Animation",
        35 to if (isSpanish) "Comedia" else "Comedy",
        80 to if (isSpanish) "Crimen" else "Crime",
        99 to if (isSpanish) "Documental" else "Documentary",
        18 to if (isSpanish) "Drama" else "Drama",
        10751 to if (isSpanish) "Familia" else "Family",
        10762 to if (isSpanish) "Infantil" else "Kids",
        9648 to if (isSpanish) "Misterio" else "Mystery",
        10763 to if (isSpanish) "Noticias" else "News",
        10764 to if (isSpanish) "Reality" else "Reality",
        10765 to if (isSpanish) "Sci-Fi y Fantasía" else "Sci-Fi & Fantasy",
        10766 to if (isSpanish) "Soap" else "Soap",
        10767 to if (isSpanish) "Talk Show" else "Talk",
        10768 to if (isSpanish) "Bélica y Política" else "War & Politics",
        37 to if (isSpanish) "Western" else "Western"
    )

    fun loadAllCategories() {
        loadMovies()
        loadTVShows()
    }
}
