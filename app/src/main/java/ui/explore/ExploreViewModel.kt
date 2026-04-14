package ui.explore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.vexo.app.R
import data.model.Movie
import data.model.Category
import data.repository.TMDBRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ExploreViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TMDBRepository.getInstance()
    private val context = application.applicationContext
    
    private val _movieCategories = MutableLiveData<List<Category>>()
    val movieCategories: LiveData<List<Category>> = _movieCategories

    private val _tvCategories = MutableLiveData<List<Category>>()
    val tvCategories: LiveData<List<Category>> = _tvCategories

    fun loadMovies() {
        viewModelScope.launch {
            val list = mutableListOf<Category>()

            val trending = repository.getTrendingMovies()
            if (trending.isNotEmpty()) list.add(Category(context.getString(R.string.cat_trending_movies), trending))

            val topRated = repository.getTopRatedMovies()
            if (topRated.isNotEmpty()) list.add(Category(context.getString(R.string.cat_top_rated_movies), topRated))

            val nowPlaying = repository.getNowPlayingMovies()
            if (nowPlaying.isNotEmpty()) list.add(Category(context.getString(R.string.cat_now_playing), nowPlaying))

            val genreMap = getMovieGenres()
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
            val list = mutableListOf<Category>()

            val trendingTV = repository.getTrendingTV()
            if (trendingTV.isNotEmpty()) list.add(Category(context.getString(R.string.cat_trending_tv), trendingTV))

            val topRatedTV = repository.getTopRatedTV()
            if (topRatedTV.isNotEmpty()) list.add(Category(context.getString(R.string.cat_top_rated_tv), topRatedTV))

            val genreMap = getTVGenres()
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

    private fun getMovieGenres() = mapOf(
        28 to context.getString(R.string.genre_action),
        12 to context.getString(R.string.genre_adventure),
        16 to context.getString(R.string.genre_animation),
        35 to context.getString(R.string.genre_comedy),
        80 to context.getString(R.string.genre_crime),
        99 to context.getString(R.string.genre_documentary),
        18 to context.getString(R.string.genre_drama),
        10751 to context.getString(R.string.genre_family),
        14 to context.getString(R.string.genre_fantasy),
        36 to context.getString(R.string.genre_history),
        27 to context.getString(R.string.genre_horror),
        10402 to context.getString(R.string.genre_music),
        9648 to context.getString(R.string.genre_mystery),
        10749 to context.getString(R.string.genre_romance),
        878 to context.getString(R.string.genre_sci_fi),
        53 to context.getString(R.string.genre_thriller),
        10752 to context.getString(R.string.genre_war),
        37 to context.getString(R.string.genre_western)
    )

    private fun getTVGenres() = mapOf(
        10759 to context.getString(R.string.genre_action_adventure),
        16 to context.getString(R.string.genre_animation),
        35 to context.getString(R.string.genre_comedy),
        80 to context.getString(R.string.genre_crime),
        99 to context.getString(R.string.genre_documentary),
        18 to context.getString(R.string.genre_drama),
        10751 to context.getString(R.string.genre_family),
        10762 to context.getString(R.string.genre_kids),
        9648 to context.getString(R.string.genre_mystery),
        10763 to context.getString(R.string.genre_news),
        10764 to context.getString(R.string.genre_reality),
        10765 to context.getString(R.string.genre_sci_fi_fantasy),
        10766 to context.getString(R.string.genre_soap),
        10767 to context.getString(R.string.genre_talk),
        10768 to context.getString(R.string.genre_war_politics),
        37 to context.getString(R.string.genre_western)
    )

    fun loadAllCategories() {
        loadMovies()
        loadTVShows()
    }
}