package com.cosory.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.cosory.app.ui.navigation.CosoryNavHost
import com.cosory.app.ui.theme.CosoryTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as CosoryApplication
        val startAtWelcome = !app.prefs.welcomeRead

        setContent {
            CosoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    CosoryNavHost(
                        startAtWelcome = startAtWelcome,
                        onWelcomeConfirmed = { app.prefs.welcomeRead = true },
                    )
                }
            }
        }
    }
}
