package ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vexo.app.R

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Aquí pones el layout de la pantalla de búsqueda
        setContentView(R.layout.activity_search)
    }
}
