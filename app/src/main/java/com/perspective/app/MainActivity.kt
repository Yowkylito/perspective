package com.perspective.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.perspective.app.ui.PerspectiveApp
import com.perspective.app.ui.brand.BrandGate
import com.perspective.app.ui.theme.PerspectiveTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Before super.onCreate, as the library requires. The system splash is
        // themed to brand ground with no icon; BrandMoment draws the only mark
        // the launch shows, so the two hand over on the same colour and the
        // seam between them is invisible.
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PerspectiveTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BrandGate {
                        PerspectiveApp()
                    }
                }
            }
        }
    }
}
