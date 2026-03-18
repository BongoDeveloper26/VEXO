package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.vexo.app.R
import data.repository.TMDBRepository
import ui.detail.DetailActivity

class ExploreFragment : Fragment() {

    private val viewModel: ExploreViewModel by viewModels()
    private val repository = TMDBRepository.getInstance()
    private lateinit var recyclerCategories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_explore, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeader(view)
        setupRecyclerView(view)
        setupFab(view)
        observeViewModel()

        viewModel.loadAllCategories()
    }

    private fun setupHeader(view: View) {
        view.findViewById<ImageButton>(R.id.btnBack).visibility = View.GONE
        
        view.findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showSettingsMenu()
        }
    }

    private fun showSettingsMenu() {
        val bottomSheet = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.layout_explore_menu, null)
        val isSpanish = repository.getLanguage() == "es-ES"

        // Configurar textos según idioma
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
            startActivity(Intent(requireContext(), AboutActivity::class.java))
        }

        bottomSheet.setContentView(view)
        bottomSheet.show()
    }

    private fun setupRecyclerView(view: View) {
        recyclerCategories = view.findViewById(R.id.recyclerCategories)
        categoryAdapter = CategoryAdapter(emptyList()) { movie ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("movie", movie)
            startActivity(intent)
        }
        
        recyclerCategories.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
    }

    private fun setupFab(view: View) {
        view.findViewById<View>(R.id.fabSearch).visibility = View.GONE
    }

    private fun observeViewModel() {
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.updateCategories(categories)
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Español", "English")
        val currentLang = if (repository.getLanguage() == "es-ES") 0 else 1

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (repository.getLanguage() == "es-ES") "Seleccionar Idioma" else "Select Language")
            .setSingleChoiceItems(languages, currentLang) { dialog, which ->
                val newLang = if (which == 0) "es-ES" else "en-US"
                if (newLang != repository.getLanguage()) {
                    repository.setLanguage(newLang)
                    activity?.recreate()
                }
                dialog.dismiss()
            }
            .show()
    }
}
