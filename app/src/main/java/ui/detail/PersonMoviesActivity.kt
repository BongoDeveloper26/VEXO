package ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.repository.TMDBRepository
import kotlinx.coroutines.launch
import ui.explore.MovieAdapter

class PersonMoviesActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private var isBioExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_movies)

        val personId = intent.getIntExtra("personId", -1)
        
        val btnBack = findViewById<View>(R.id.btnBackPerson)
        if (btnBack is android.widget.ImageButton) {
            btnBack.setImageResource(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        }
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        if (personId != -1) {
            setupLabels()
            loadPersonDetails(personId)
            loadPersonMovies(personId)
        } else {
            finish()
        }
    }

    private fun setupLabels() {
        val isSpanish = repository.getLanguage() == "es-ES"
        findViewById<TextView>(R.id.labelMovieCount).text = if (isSpanish) "PELÍCULAS" else "MOVIES"
        findViewById<TextView>(R.id.labelBirth).text = if (isSpanish) "NACIMIENTO" else "BIRTH"
        findViewById<TextView>(R.id.textFilmographyTitle).text = if (isSpanish) "FILMOGRAFÍA" else "FILMOGRAPHY"
    }

    private fun loadPersonDetails(personId: Int) {
        val imgHeader: ImageView = findViewById(R.id.imgPersonHeader)
        val textName: TextView = findViewById(R.id.textPersonNameHeader)
        val textDept: TextView = findViewById(R.id.textPersonDept)
        val textBio: TextView = findViewById(R.id.textPersonBio)
        val textReadMore: TextView = findViewById(R.id.textReadMore)
        val textBirthYear: TextView = findViewById(R.id.textPersonBirthYear)
        val textPlace: TextView = findViewById(R.id.textPersonPlace)
        val textMovieCount: TextView = findViewById(R.id.textMovieCountValue)
        
        val isSpanish = repository.getLanguage() == "es-ES"

        lifecycleScope.launch {
            val person = repository.getPersonDetails(personId)
            val movieCredits = repository.getMoviesByPerson(personId)

            if (person != null) {
                textName.text = person.name
                
                textDept.text = when(person.known_for_department) {
                    "Acting" -> if (isSpanish) "Actuación" else "Acting"
                    "Directing" -> if (isSpanish) "Dirección" else "Directing"
                    "Writing" -> if (isSpanish) "Guion" else "Writing"
                    "Production" -> if (isSpanish) "Producción" else "Production"
                    else -> person.known_for_department
                }

                // Año de nacimiento simplificado para estilo Letterboxd
                textBirthYear.text = if (!person.birthday.isNullOrBlank()) {
                    person.birthday.take(4)
                } else {
                    "--"
                }
                
                textPlace.text = person.place_of_birth ?: ""
                textMovieCount.text = movieCredits.size.toString()
                
                if (!person.biography.isNullOrBlank()) {
                    textBio.text = person.biography
                    textReadMore.visibility = if (person.biography.length > 150) View.VISIBLE else View.GONE
                    
                    textReadMore.setOnClickListener {
                        if (isBioExpanded) {
                            textBio.maxLines = 3
                            textReadMore.text = if (isSpanish) "Leer más" else "Read more"
                        } else {
                            textBio.maxLines = Int.MAX_VALUE
                            textReadMore.text = if (isSpanish) "Ver menos" else "Show less"
                        }
                        isBioExpanded = !isBioExpanded
                    }
                } else {
                    textBio.text = if (isSpanish) "Sin biografía disponible." else "No biography available."
                    textReadMore.visibility = View.GONE
                }

                val profileUrl = if (person.profile_path != null) {
                    "https://image.tmdb.org/t/p/h632${person.profile_path}"
                } else {
                    "https://ui-avatars.com/api/?name=${person.name.replace(" ", "+")}&background=random"
                }

                Glide.with(this@PersonMoviesActivity)
                    .load(profileUrl)
                    .centerCrop()
                    .into(imgHeader)
            }
        }
    }

    private fun loadPersonMovies(personId: Int) {
        val recycler: RecyclerView = findViewById(R.id.recyclerPersonMovies)
        
        lifecycleScope.launch {
            val movies = repository.getMoviesByPerson(personId)
            if (movies.isNotEmpty()) {
                val filteredMovies = movies.filter { movie ->
                    movie.posterPath != null && movie.rating >= 6.0
                }
                
                val relevantMovies = if (filteredMovies.size > 30) filteredMovies.take(30) else filteredMovies
                val adapter = MovieAdapter(relevantMovies)
                adapter.onItemClick = { movie ->
                    val intent = Intent(this@PersonMoviesActivity, DetailActivity::class.java)
                    intent.putExtra("movie", movie)
                    startActivity(intent)
                }
                recycler.apply {
                    layoutManager = LinearLayoutManager(this@PersonMoviesActivity)
                    this.adapter = adapter
                }
            }
        }
    }
}
