package com.vexo.app

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import ui.explore.ExploreActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnExplore = findViewById<Button>(R.id.btnExplore)
        val btnList = findViewById<Button>(R.id.btnList)

        btnExplore.setOnClickListener {
            startActivity(Intent(this, ExploreActivity::class.java))
        }

        btnList.setOnClickListener {
            startActivity(Intent(this, ListActivity::class.java))
        }
    }
}


