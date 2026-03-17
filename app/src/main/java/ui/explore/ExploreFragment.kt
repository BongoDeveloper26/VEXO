package ui.explore

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        // En el fragmento principal, ocultamos el botón de volver si no es necesario
        view.findViewById<ImageButton>(R.id.btnBack).visibility = View.GONE
        
        view.findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            showLanguageDialog()
        }
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
        // El FAB de búsqueda ya no es tan necesario si tenemos la pestaña abajo, 
        // pero lo dejamos por si acaso o lo ocultamos. Lo ocultaremos para limpiar la UI.
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
            .setTitle("Seleccionar Idioma")
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
