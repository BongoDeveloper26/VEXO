package ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.vexo.app.R
import data.repository.TMDBRepository
import data.repository.TVEpisodeDTO
import kotlinx.coroutines.launch
import java.util.Locale

class TVSeasonsActivity : AppCompatActivity() {

    private val repository = TMDBRepository.getInstance()
    private var seriesId: Int = -1
    private var seriesName: String = ""
    private var totalSeasons: Int = 0
    
    private lateinit var recyclerEpisodes: RecyclerView
    private lateinit var loadingBar: ProgressBar
    private lateinit var textSeasonTitle: TextView
    private lateinit var textSeasonOverview: TextView
    private lateinit var btnReadMore: TextView
    private lateinit var imgSeasonBackdrop: ImageView
    
    private var isExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_seasons)

        seriesId = intent.getIntExtra("seriesId", -1)
        seriesName = intent.getStringExtra("seriesName") ?: ""
        totalSeasons = intent.getIntExtra("totalSeasons", 0)

        if (seriesId == -1) {
            finish()
            return
        }

        setupUI()
        setupSeasonChips()
        loadSeason(1)
    }

    private fun setupUI() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.title = ""

        val btnBack: ImageButton = findViewById(R.id.btnBackSeasons)
        btnBack.setOnClickListener { finish() }

        recyclerEpisodes = findViewById(R.id.recyclerEpisodes)
        loadingBar = findViewById(R.id.loadingSeasons)
        textSeasonTitle = findViewById(R.id.textSeasonTitle)
        textSeasonOverview = findViewById(R.id.textSeasonOverview)
        btnReadMore = findViewById(R.id.btnReadMoreSeason)
        imgSeasonBackdrop = findViewById(R.id.imgSeasonBackdrop)
        
        findViewById<TextView>(R.id.textSeriesName).text = seriesName.uppercase(Locale.getDefault())

        recyclerEpisodes.layoutManager = LinearLayoutManager(this)
        
        btnReadMore.setOnClickListener {
            isExpanded = !isExpanded
            if (isExpanded) {
                textSeasonOverview.maxLines = Int.MAX_VALUE
                btnReadMore.text = "LEER MENOS"
            } else {
                textSeasonOverview.maxLines = 3
                btnReadMore.text = "LEER MÁS"
            }
        }
    }

    private fun setupSeasonChips() {
        val chipGroup: ChipGroup = findViewById(R.id.chipGroupSeasons)
        chipGroup.removeAllViews()
        for (i in 1..totalSeasons) {
            val chip = LayoutInflater.from(this).inflate(R.layout.layout_season_chip, chipGroup, false) as Chip
            chip.text = i.toString()
            chip.id = View.generateViewId()
            chip.isChecked = (i == 1)
            chip.setOnClickListener { loadSeason(i) }
            chipGroup.addView(chip)
        }
    }

    private fun loadSeason(seasonNumber: Int) {
        lifecycleScope.launch {
            loadingBar.visibility = View.VISIBLE
            val details = repository.getTVSeasonDetails(seriesId, seasonNumber)
            loadingBar.visibility = View.GONE

            if (details != null) {
                textSeasonTitle.text = details.name
                
                // Reset Read More state
                isExpanded = false
                textSeasonOverview.maxLines = 3
                btnReadMore.text = "LEER MÁS"
                btnReadMore.visibility = View.GONE
                
                val overviewText = details.overview.ifEmpty { "No hay descripción disponible para esta temporada." }
                textSeasonOverview.text = overviewText
                
                // Forzar visibilidad del botón si el texto es largo, como medida de seguridad
                if (overviewText.length > 150) {
                    btnReadMore.visibility = View.VISIBLE
                }

                // Detectar por lineCount también
                textSeasonOverview.post {
                    if (textSeasonOverview.lineCount > 3) {
                        btnReadMore.visibility = View.VISIBLE
                    }
                }
                
                val posterUrl = if (!details.poster_path.isNullOrEmpty()) {
                    "https://image.tmdb.org/t/p/w780${details.poster_path}"
                } else {
                    ""
                }

                Glide.with(this@TVSeasonsActivity)
                    .load(posterUrl)
                    .placeholder(R.drawable.gradient_overlay)
                    .centerCrop()
                    .into(imgSeasonBackdrop)
                
                recyclerEpisodes.adapter = EpisodeAdapter(details.episodes)
            }
        }
    }

    inner class EpisodeAdapter(private val episodes: List<TVEpisodeDTO>) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val img: ImageView = v.findViewById(R.id.imgEpisodeStill)
            val textNum: TextView = v.findViewById(R.id.textEpisodeNumber)
            val textName: TextView = v.findViewById(R.id.textEpisodeName)
            val textOverview: TextView = v.findViewById(R.id.textEpisodeOverview)
            val textRuntime: TextView = v.findViewById(R.id.textEpisodeRuntime)
            val textRating: TextView = v.findViewById(R.id.textEpisodeRating)
        }

        override fun onCreateViewHolder(p: ViewGroup, t: Int) = ViewHolder(LayoutInflater.from(p.context).inflate(R.layout.item_episode, p, false))
        override fun onBindViewHolder(h: ViewHolder, p: Int) {
            val ep = episodes[p]
            h.textNum.text = "EP. ${ep.episode_number}"
            h.textName.text = ep.name
            h.textOverview.text = ep.overview
            h.textRuntime.text = "${ep.runtime ?: "--"} MIN"
            h.textRating.text = String.format(Locale.getDefault(), "%.1f", ep.vote_average)
            
            Glide.with(h.itemView)
                .load("https://image.tmdb.org/t/p/w500${ep.still_path ?: ""}")
                .centerCrop()
                .into(h.img)
        }
        override fun getItemCount() = episodes.size
    }
}
