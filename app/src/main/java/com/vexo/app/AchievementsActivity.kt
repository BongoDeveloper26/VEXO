package com.vexo.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import data.repository.WatchlistRepository
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AchievementsActivity : AppCompatActivity() {

    private lateinit var watchlistRepository: WatchlistRepository
    private lateinit var adapter: AchievementsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)
        
        watchlistRepository = WatchlistRepository(this)
        
        findViewById<View>(R.id.btnBackAchievements).setOnClickListener { finish() }
        
        val achievements = calculateAchievements()
        setupHeader(achievements)
        
        val recycler = findViewById<RecyclerView>(R.id.recyclerAchievements)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = AchievementsAdapter(achievements)
        recycler.adapter = adapter
    }

    private fun setupHeader(achievements: List<Achievement>) {
        val completed = achievements.count { it.currentProgress >= it.maxProgress }
        val total = achievements.size
        val progressPercent = (completed.toFloat() / total.toFloat() * 100).toInt()

        findViewById<ProgressBar>(R.id.progressBarGlobal).progress = progressPercent
        findViewById<TextView>(R.id.textPercent).text = "$progressPercent% completado"
        
        val statusText = findViewById<TextView>(R.id.textGlobalStatus)
        val statsText = findViewById<TextView>(R.id.textGlobalStats)
        
        statusText.text = when {
            progressPercent >= 100 -> "Maestro Supremo"
            progressPercent >= 75 -> "Cinéfilo de Oro"
            progressPercent >= 50 -> "Cinéfilo de Plata"
            progressPercent >= 25 -> "Cinéfilo de Bronce"
            else -> "Iniciado en Vexo"
        }

        val remaining = total - completed
        statsText.text = if (remaining > 0) "Te faltan $remaining logros para el siguiente nivel" else "¡Has desbloqueado todo!"
    }

    private fun calculateAchievements(): List<Achievement> {
        val stats = watchlistRepository.getStats()
        val diary = watchlistRepository.getDiary()
        val vitrina = watchlistRepository.getVitrinaMovies()
        val lists = watchlistRepository.getUserLists()
        val favorites = lists.find { it.name == WatchlistRepository.FAVORITES_LIST_NAME }?.movies ?: emptyList()

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dates = diary.mapNotNull { 
            try { sdf.parse(it.date)?.time } catch (e: Exception) { null } 
        }.distinct().sortedDescending()

        var currentStreak = 0
        if (dates.isNotEmpty()) {
            currentStreak = 1
            for (i in 0 until dates.size - 1) {
                val diff = dates[i] - dates[i + 1]
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                if (days == 1L) currentStreak++ else break
            }
        }

        val uniqueGenres = diary.flatMap { it.movie?.genreIds ?: emptyList() }.distinct()

        return listOf(
            Achievement("Bienvenida Cinéfila", "Regístrate y comienza tu viaje en Vexo", 1, 1),
            Achievement("Primer Paso", "Valora tu primera película", if (stats.totalMovies >= 1) 1 else 0, 1),
            Achievement("Cinéfilo madrugador", "Valora contenido durante 3 días seguidos", currentStreak, 3),
            Achievement("Racha Imparable", "Mantén tu actividad durante 7 días seguidos", currentStreak, 7),
            Achievement("Hábito de Hierro", "Llega a los 30 días seguidos", currentStreak, 30),
            Achievement("Explorador de Géneros", "Descubre 5 géneros distintos", uniqueGenres.size, 5),
            Achievement("Cultura Universal", "Valora películas de 10 géneros", uniqueGenres.size, 10),
            Achievement("Omnívoro del Cine", "Completa 15 géneros", uniqueGenres.size, 15),
            Achievement("Columnista de Vexo", "Escribe 10 reseñas detalladas", diary.count { !it.review.isNullOrEmpty() }, 10),
            Achievement("Filósofo del Cine", "Escribe 25 reseñas", diary.count { !it.review.isNullOrEmpty() }, 25),
            Achievement("Serialófilo", "Añade 5 series a tu historial", diary.count { it.movie?.isTvShow == true }, 5),
            Achievement("Maestro del Séptimo Arte", "Alcanza las 100 películas valoradas", stats.totalMovies, 100),
            Achievement("Curador de Arte", "Completa los 4 huecos de tu vitrina", vitrina.count { it != null }, 4),
            Achievement("Mis Favoritas", "Añade 10 películas a tus favoritos", favorites.size, 10),
            Achievement("Identidad Visual", "Cambia tu foto de perfil", if (watchlistRepository.getProfileImageUri() != null) 1 else 0, 1)
        )
    }
}

data class Achievement(
    val title: String,
    val description: String,
    val currentProgress: Int,
    val maxProgress: Int
)

class AchievementsAdapter(private val achievements: List<Achievement>) :
    RecyclerView.Adapter<AchievementsAdapter.ViewHolder>() {

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.textAchievementTitle)
        val desc: TextView = v.findViewById(R.id.textAchievementDesc)
        val progress: ProgressBar = v.findViewById(R.id.progressAchievement)
        val status: TextView = v.findViewById(R.id.textAchievementStatus)
        val checkBadge: View = v.findViewById(R.id.cardCheckBadge)
        val iconCard: View = v.findViewById(R.id.cardAchievementIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val a = achievements[position]
        holder.title.text = a.title
        holder.desc.text = a.description
        
        val isCompleted = a.currentProgress >= a.maxProgress
        holder.progress.max = a.maxProgress
        holder.progress.progress = if (a.currentProgress > a.maxProgress) a.maxProgress else a.currentProgress
        
        holder.status.text = if (isCompleted) "Logro Desbloqueado" else "${a.currentProgress}/${a.maxProgress}"
        holder.checkBadge.visibility = if (isCompleted) View.VISIBLE else View.GONE
        
        if (isCompleted) {
            holder.iconCard.setBackgroundTintList(android.content.res.ColorStateList.valueOf(holder.itemView.context.getColor(R.color.primary)))
            holder.status.setTextColor(holder.itemView.context.getColor(R.color.primary))
        } else {
            holder.iconCard.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#107C3AED")))
            holder.status.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
        }
    }

    override fun getItemCount() = achievements.size
}
