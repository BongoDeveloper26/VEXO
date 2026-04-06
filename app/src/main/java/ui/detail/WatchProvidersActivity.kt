package ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vexo.app.R
import data.repository.WatchProviderItem

class WatchProvidersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watch_providers)

        val providers = intent.getParcelableArrayListExtra<WatchProviderItem>("providers") ?: emptyList<WatchProviderItem>()

        findViewById<ImageButton>(R.id.btnBackProviders).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerFullProviders)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FullWatchProviderAdapter(providers)
    }

    inner class FullWatchProviderAdapter(private val list: List<WatchProviderItem>) : RecyclerView.Adapter<FullWatchProviderAdapter.ViewHolder>() {
        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val img: ImageView = v.findViewById(R.id.imgProviderLogoFull)
            val name: TextView = v.findViewById(R.id.textProviderNameFull)
        }

        override fun onCreateViewHolder(p: ViewGroup, t: Int): ViewHolder {
            val v = LayoutInflater.from(p.context).inflate(R.layout.item_watch_provider_full, p, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(h: ViewHolder, p: Int) {
            val item = list[p]
            h.name.text = item.provider_name
            Glide.with(h.itemView)
                .load("https://image.tmdb.org/t/p/w154${item.logo_path}")
                .into(h.img)
        }

        override fun getItemCount() = list.size
    }
}
