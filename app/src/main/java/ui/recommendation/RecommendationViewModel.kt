package ui.recommendation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import data.model.Movie
import data.repository.OMDbRepository
import data.repository.TMDBRepository
import data.repository.WatchlistRepository
import data.repository.OMDbMovieDTO
import kotlinx.coroutines.launch

class RecommendationViewModel(application: Application) : AndroidViewModel(application) {
    private val tmdbRepository = TMDBRepository.getInstance()
    private val omdbRepository = OMDbRepository.getInstance()
    private val watchlistRepository = WatchlistRepository(application)
    
    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    private val _currentMovieIndex = MutableLiveData(0)
    val currentMovieIndex: LiveData<Int> = _currentMovieIndex

    private val _currentOMDbData = MutableLiveData<OMDbMovieDTO?>()
    val currentOMDbData: LiveData<OMDbMovieDTO?> = _currentOMDbData

    private val _isLoadingRatings = MutableLiveData<Boolean>()
    val isLoadingRatings: LiveData<Boolean> = _isLoadingRatings

    private val _selectedGenres = MutableLiveData<List<Int>>(emptyList())
    val selectedGenres: LiveData<List<Int>> = _selectedGenres

    private val DISCOVERY_LIST_NAME = "Discovery"

    init {
        loadRecommendations()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            val genres = _selectedGenres.value ?: emptyList()
            val combined = if (genres.isEmpty()) {
                val trendingMovies = tmdbRepository.getTrendingMovies()
                val trendingTV = tmdbRepository.getTrendingTV()
                (trendingMovies + trendingTV).shuffled()
            } else {
                val movieResults = tmdbRepository.getMoviesByGenre(genres)
                val tvResults = tmdbRepository.getTVByGenre(genres)
                (movieResults + tvResults).shuffled()
            }
            
            _movies.value = combined
            _currentMovieIndex.value = 0
            if (combined.isNotEmpty()) {
                fetchDetailedRatings(combined[0])
            }
        }
    }

    fun setGenres(genreIds: List<Int>) {
        _selectedGenres.value = genreIds
        loadRecommendations()
    }

    fun nextMovie() {
        val nextIndex = (_currentMovieIndex.value ?: 0) + 1
        val currentList = _movies.value ?: emptyList()
        
        if (nextIndex < currentList.size) {
            _currentMovieIndex.value = nextIndex
            fetchDetailedRatings(currentList[nextIndex])
        } else {
            loadRecommendations()
        }
    }

    private fun fetchDetailedRatings(movie: Movie) {
        viewModelScope.launch {
            _currentOMDbData.value = null
            _isLoadingRatings.value = true
            
            val imdbId = if (movie.isTvShow) {
                tmdbRepository.getTVDetails(movie.id)?.external_ids?.imdb_id
            } else {
                tmdbRepository.getMovieDetails(movie.id)?.imdb_id
            }

            val data = if (!imdbId.isNullOrEmpty()) {
                omdbRepository.getMovieRatingsById(imdbId)
            } else {
                val year = movie.getReleaseYear().ifEmpty { null }
                omdbRepository.getMovieRatingsByTitle(movie.title, year)
            }
            
            _currentOMDbData.value = data
            _isLoadingRatings.value = false
        }
    }

    fun addMovieToDiscoveryList(movie: Movie) {
        viewModelScope.launch {
            val lists = watchlistRepository.getUserLists()
            var discoveryList = lists.find { it.name == DISCOVERY_LIST_NAME }
            
            if (discoveryList == null) {
                val listId = watchlistRepository.createUserList(DISCOVERY_LIST_NAME, "Películas descubiertas vía VEXO Discovery", false)
                watchlistRepository.addMovieToList(listId, movie)
            } else {
                watchlistRepository.addMovieToList(discoveryList.id, movie)
            }
            nextMovie()
        }
    }
}