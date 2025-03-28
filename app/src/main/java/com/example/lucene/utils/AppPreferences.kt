package com.example.lucene.utils

import android.content.Context

object AppPreferences {
    private const val PREFS_NAME = "my_app_prefs"
    private const val KEY_LAST_PAGE = "last_page_fetched"

    fun getLastPage(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_LAST_PAGE, 1)
    }

    fun setLastPage(context: Context, page: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_LAST_PAGE, page).apply()
    }
}
