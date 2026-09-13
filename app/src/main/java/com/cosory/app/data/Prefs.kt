package com.cosory.app.data

import android.content.Context

class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("cosory_prefs", Context.MODE_PRIVATE)

    var welcomeRead: Boolean
        get() = sp.getBoolean(KEY_WELCOME_READ, false)
        set(value) {
            sp.edit().putBoolean(KEY_WELCOME_READ, value).apply()
        }

    private companion object {
        const val KEY_WELCOME_READ = "welcome_read"
    }
}
