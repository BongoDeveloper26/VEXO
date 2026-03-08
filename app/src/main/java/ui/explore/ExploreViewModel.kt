package ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.model.Movie
import data.repository.TMDBRepository
import kotlinx.coroutines.launch

class ExploreViewModel : ViewModel() {

    private val repository = TMDBRepository()

    private val _movies = MutableLiveData<List<Movie>>()
    val movies: LiveData<List<Movie>> = _movies

    fun loadTrendingMovies() {
        viewModelScope.launch {
            try {
                val movieList = repository.getTrendingMovies()
                _movies.value = movieList
            } catch (e: Exception) {
                _movies.value = emptyList()
                e.printStackTrace()
            }
        }
    }
}
