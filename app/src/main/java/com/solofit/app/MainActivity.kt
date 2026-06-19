package com.solofit.app

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.solofit.app.domain.model.ThemeMode
import com.solofit.app.ui.RootViewModel
import com.solofit.app.ui.SoloFitApp
import com.solofit.app.ui.theme.SoloFitTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val rootViewModel: RootViewModel = hiltViewModel()
            val themeMode by rootViewModel.themeMode.collectAsStateWithLifecycle()

            // One-time: honor the OS "remove animations" / reduce-motion setting.
            LaunchedEffect(Unit) {
                val scale = runCatching {
                    Settings.Global.getFloat(
                        contentResolver,
                        Settings.Global.ANIMATOR_DURATION_SCALE,
                        1f
                    )
                }.getOrDefault(1f)
                rootViewModel.applyReducedMotionOnce(scale)
            }

            // Light is the default (premium-notebook feel); dark is a full alternative.
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            SoloFitTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SoloFitApp(rootViewModel = rootViewModel)
                }
            }
        }
    }
}
