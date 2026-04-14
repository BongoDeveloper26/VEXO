package com.vexo.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import java.util.Locale

class VexoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        applyLanguage(this)
    }

    companion object {
        fun applyLanguage(context: Context) {
            val prefs = context.getSharedPreferences("vexo_settings", Context.MODE_PRIVATE)
            val language = prefs.getString("app_language", "es-ES") ?: "es-ES"
            val locale = if (language == "es-ES") Locale("es", "ES") else Locale("en", "US")
            
            Locale.setDefault(locale)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
}