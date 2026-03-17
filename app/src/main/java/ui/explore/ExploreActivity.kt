package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vexo.app.R
import data.repository.TMDBRepository
import ui.detail.DetailActivity
import ui.search.SearchActivity

class ExploreActivity : AppCompatActivity() {

    private val viewModel: ExploreViewModel by viewModels()
    private val repository = TMDBRepository.getInstance()
    private lateinit var recyclerCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore)

        setupHeader()
        setupRecyclerView()
        setupFab()
        observeViewModel()

        viewModel.loadAllCategories()
    }

    private fun setupHeader() {
        // Restaurar botón volver
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Botón ajustes
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun setupRecyclerView() {
        recyclerCategories = findViewById(R.id.recyclerCategories)
        categoryAdapter = CategoryAdapter(emptyList()) { movie ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        
        recyclerCategories.apply {
            layoutManager = LinearLayoutManager(this@ExploreActivity)
            adapter = categoryAdapter
        }
    }

    private fun setupFab() {
        findViewById<View>(R.id.fabSearch).setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.categories.observe(this) { categories ->
            categoryAdapter.updateCategories(categories)
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Español", "English")
        val currentLang = if (repository.getLanguage() == "es-ES") 0 else 1

        MaterialAlertDialogBuilder(this)
            .setTitle("Seleccionar Idioma")
            .setSingleChoiceItems(languages, currentLang) { dialog, which ->
                val newLang = if (which == 0) "es-ES" else "en-US"
                if (newLang != repository.getLanguage()) {
                    repository.setLanguage(newLang)
                    // Al cambiar el idioma, recreamos para refrescar todo el sistema
                    recreate()
                }
                dialog.dismiss()
            }
            .show()
    }
}
