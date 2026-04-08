package com.vexo.app

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import data.model.Movie

class PublicProfileActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val gson = Gson()
    private lateinit var imgSlots: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_public_profile)

        val userId = intent.getStringExtra("USER_ID")
        if (userId == null) {
            Toast.makeText(this, "Error: Usuario no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        loadPublicData(userId)
    }

    private fun setupUI() {
        findViewById<View>(R.id.toolbarPublic).setOnClickListener { finish() }
        
        imgSlots = listOf(
            findViewById(R.id.imgPublicSlot1),
            findViewById(R.id.imgPublicSlot2),
            findViewById(R.id.imgPublicSlot3),
            findViewById(R.id.imgPublicSlot4)
        )
    }

    private fun loadPublicData(uid: String) {
        val progress = findViewById<View>(R.id.progressPublic)
        progress.visibility = View.VISIBLE

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                progress.visibility = View.GONE
                if (doc.exists()) {
                    // Nombre
                    val name = doc.getString("user_name") ?: "Usuario VEXO"
                    findViewById<TextView>(R.id.textPublicUserName).text = name
                    
                    // Imagen de perfil
                    val profileUri = doc.getString("user_profile_image")
                    if (profileUri != null) {
                        Glide.with(this).load(profileUri).into(findViewById(R.id.imgPublicProfile))
                    }

                    // Vitrina
                    val vitrinaRaw = doc.get("user_vitrina")
                    if (vitrinaRaw != null) {
                        val json = gson.toJson(vitrinaRaw)
                        val type = object : TypeToken<List<Movie?>>() {}.type
                        val vitrina: List<Movie?> = gson.fromJson(json, type)
                        displayVitrina(vitrina)
                    }

                    // Estadísticas (simuladas desde los ratings guardados en el doc)
                    val ratingsRaw = doc.get("user_movie_ratings")
                    if (ratingsRaw is Map<*, *>) {
                        val ratings = ratingsRaw as Map<String, Double>
                        val total = ratings.size
                        val avg = if (total > 0) ratings.values.average() else 0.0
                        findViewById<TextView>(R.id.textPublicMoviesCount).text = total.toString()
                        findViewById<TextView>(R.id.textPublicAverage).text = String.format("%.1f", avg)
                    }
                } else {
                    Toast.makeText(this, "El perfil no es público o no existe", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                progress.visibility = View.GONE
                Toast.makeText(this, "Error al cargar el perfil", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayVitrina(vitrina: List<Movie?>) {
        vitrina.forEachIndexed { index, movie ->
            if (index < imgSlots.size && movie != null) {
                Glide.with(this).load(movie.posterPath).centerCrop().into(imgSlots[index])
            }
        }
    }
}