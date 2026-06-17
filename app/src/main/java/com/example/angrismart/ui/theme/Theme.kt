package com.example.angrismart.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = ForestGreen,
    onPrimary        = TextLight,
    primaryContainer = LightMint,
    onPrimaryContainer = ForestGreen,

    secondary        = SageGreen,
    onSecondary      = TextLight,
    secondaryContainer = PaleGreen,
    onSecondaryContainer = SageGreen,

    tertiary         = MintGreen,
    onTertiary       = TextLight,

    background       = NeutralBg,
    onBackground     = TextPrimary,

    surface          = SurfaceWhite,
    onSurface        = TextPrimary,
    surfaceVariant   = PaleGreen,
    onSurfaceVariant = TextSecondary,

    outline          = DividerLine,
    error            = DangerRed,
    onError          = TextLight,
)

@Composable
fun AngriSmartTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Transparent status bar – content draws edge-to-edge
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}