package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vexo.app.R
import data.model.Category
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
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showSettingsMenu()
        }
    }

    private fun showSettingsMenu() {
        val bottomSheet = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_explore_menu, null)
        val isSpanish = repository.getLanguage() == "es-ES"

        // Configurar textos según idioma en el menú
        view.findViewById<TextView>(R.id.textOptionLanguage).text = if (isSpanish) "Cambiar Idioma" else "Change Language"
        view.findViewById<TextView>(R.id.textOptionAbout).text = if (isSpanish) "Quiénes Somos" else "About Us"

        // Opción Idioma
        view.findViewById<View>(R.id.optionLanguage).setOnClickListener {
            bottomSheet.dismiss()
            showLanguageDialog()
        }

        // Opción Quiénes Somos
        view.findViewById<View>(R.id.optionAbout).setOnClickListener {
            bottomSheet.dismiss()
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
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
            .setTitle(if (repository.getLanguage() == "es-ES") "Seleccionar Idioma" else "Select Language")
            .setSingleChoiceItems(languages, currentLang) { dialog, which ->
                val newLang = if (which == 0) "es-ES" else "en-US"
                if (newLang != repository.getLanguage()) {
                    repository.setLanguage(newLang)
                    recreate()
                }
                dialog.dismiss()
            }
            .show()
    }
}
