package com.example.memoapp_firebase_practice

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

object LocaleHelper {

    fun setLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val res: Resources = context.resources
        val config = Configuration(res.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            res.updateConfiguration(config, res.displayMetrics)
            return context
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    fun getCurrentLanguage(): String {
        return Locale.getDefault().language
    }
}
