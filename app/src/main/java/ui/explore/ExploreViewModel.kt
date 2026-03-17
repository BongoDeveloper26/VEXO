package ui.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.model.Category
import data.repository.TMDBRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.random.Random

class ExploreViewModel : ViewModel() {

    private val repository = TMDBRepository.getInstance()

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    fun loadAllCategories() {
        viewModelScope.launch {
            try {
                val isSpanish = repository.getLanguage() == "es-ES"
                fun randomPage() = Random.nextInt(1, 4)

                val trending = async { repository.getTrendingMovies() }
                val topRated = async { repository.getTopRatedMovies() }
                val nowPlaying = async { repository.getNowPlayingMovies() }
                
                // Corregido: Ahora pasamos listOf(ID) porque el repositorio espera una lista para el filtrado avanzado
                val animation = async { repository.getMoviesByGenre(listOf(16), randomPage()) }
                val action = async { repository.getMoviesByGenre(listOf(28), randomPage()) }
                val adventure = async { repository.getMoviesByGenre(listOf(12), randomPage()) }
                val comedy = async { repository.getMoviesByGenre(listOf(35), randomPage()) }
                val crime = async { repository.getMoviesByGenre(listOf(80), randomPage()) }
                val documentary = async { repository.getMoviesByGenre(listOf(99), randomPage()) }
                val drama = async { repository.getMoviesByGenre(listOf(18), randomPage()) }
                val family = async { repository.getMoviesByGenre(listOf(10751), randomPage()) }
                val fantasy = async { repository.getMoviesByGenre(listOf(14), randomPage()) }
                val history = async { repository.getMoviesByGenre(listOf(36), randomPage()) }
                val horror = async { repository.getMoviesByGenre(listOf(27), randomPage()) }
                val music = async { repository.getMoviesByGenre(listOf(10402), randomPage()) }
                val mystery = async { repository.getMoviesByGenre(listOf(9648), randomPage()) }
                val romance = async { repository.getMoviesByGenre(listOf(10749), randomPage()) }
                val scifi = async { repository.getMoviesByGenre(listOf(878), randomPage()) }
                val thriller = async { repository.getMoviesByGenre(listOf(53), randomPage()) }
                val war = async { repository.getMoviesByGenre(listOf(10752), randomPage()) }
                val western = async { repository.getMoviesByGenre(listOf(37), randomPage()) }
                val tvMovie = async { repository.getMoviesByGenre(listOf(10770), randomPage()) }

                val categoryList = listOf(
                    Category(if (isSpanish) "Top de la Semana" else "Weekly Top", trending.await()),
                    Category(if (isSpanish) "Las Mejor Valoradas" else "Top Rated", topRated.await()),
                    Category(if (isSpanish) "Ahora en Cines" else "Now Playing", nowPlaying.await()),
                    Category(if (isSpanish) "Animación" else "Animation", animation.await()),
                    Category(if (isSpanish) "Acción Trepidante" else "Fast-Paced Action", action.await()),
                    Category(if (isSpanish) "Grandes Aventuras" else "Great Adventures", adventure.await()),
                    Category(if (isSpanish) "Risas Aseguradas" else "Guaranteed Laughs", comedy.await()),
                    Category(if (isSpanish) "Crimen y Misterio" else "Crime & Mystery", crime.await()),
                    Category(if (isSpanish) "Documentales" else "Documentaries", documentary.await()),
                    Category(if (isSpanish) "Dramas Intensos" else "Intense Dramas", drama.await()),
                    Category(if (isSpanish) "Cine en Familia" else "Family Movies", family.await()),
                    Category(if (isSpanish) "Mundos de Fantasía" else "Fantasy Worlds", fantasy.await()),
                    Category(if (isSpanish) "Basado en la Historia" else "Based on History", history.await()),
                    Category(if (isSpanish) "Terror y Pesadillas" else "Horror & Nightmares", horror.await()),
                    Category(if (isSpanish) "Musicales" else "Musicals", music.await()),
                    Category(if (isSpanish) "Intriga y Misterio" else "Intrigue & Mystery", mystery.await()),
                    Category(if (isSpanish) "Historias Románticas" else "Romantic Stories", romance.await()),
                    Category(if (isSpanish) "Ciencia Ficción" else "Science Fiction", scifi.await()),
                    Category(if (isSpanish) "Thrillers que Atrapan" else "Gripping Thrillers", thriller.await()),
                    Category(if (isSpanish) "Cine Bélico" else "War Movies", war.await()),
                    Category(if (isSpanish) "Del Lejano Oeste" else "Westerns", western.await()),
                    Category(if (isSpanish) "Películas de TV" else "TV Movies", tvMovie.await())
                )

                _categories.value = categoryList.filter { it.movies.isNotEmpty() }
                
            } catch (e: Exception) {
                _categories.value = emptyList()
            }
        }
    }
}
