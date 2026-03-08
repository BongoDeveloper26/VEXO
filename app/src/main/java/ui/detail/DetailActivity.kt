package ui.detail

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vexo.app.R
import data.model.Movie

class DetailActivity : AppCompatActivity() {

    private lateinit var textTitle: TextView
    private lateinit var textRating: TextView
    private lateinit var textOverview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        textTitle = findViewById(R.id.textTitle)
        textRating = findViewById(R.id.textRating)
        textOverview = findViewById(R.id.textOverview)

        val movie = intent.getParcelableExtra<Movie>("movie")

        if (movie != null) {
            textTitle.text = movie.title
            textRating.text = "Rating: ${movie.rating}"
            textOverview.text = movie.overview
        } else {
            finish()
        }
    }
}
