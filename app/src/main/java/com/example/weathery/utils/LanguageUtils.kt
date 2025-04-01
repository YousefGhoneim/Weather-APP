package com.example.weathery.utils

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

object LanguageUtils {
    fun wrap(context: Context, language: String): ContextWrapper {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val config = resources.configuration

        config.setLocales(LocaleList(locale))

        val updatedContext = context.createConfigurationContext(config)
        return ContextWrapper(updatedContext)
    }
}
