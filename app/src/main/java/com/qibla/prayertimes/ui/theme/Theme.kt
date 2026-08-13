package com.qibla.prayertimes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.qibla.prayertimes.R

private val entezarFontFamily = FontFamily(Font(R.font.estedad))

/** The app's default type scale, all mapped onto the Entezar font. */
private val QiblaTypography: Typography by lazy {
    val base = Typography()
    Typography(
        displayLarge = base.displayLarge.copy(fontFamily = entezarFontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = entezarFontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = entezarFontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = entezarFontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = entezarFontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = entezarFontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = entezarFontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = entezarFontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = entezarFontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = entezarFontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = entezarFontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = entezarFontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = entezarFontFamily),
        labelMedium = base.labelMedium.copy(fontFamily = entezarFontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = entezarFontFamily)
    )
}

@Composable
fun QiblaAppTheme(content: @Composable () -> Unit) {
    // Built fresh on every recomposition (not cached in a top-level val) so that toggling
    // ThemeState.isDark — read here — actually changes the resolved Material colors.
    val scheme = if (ThemeState.isDark) {
        darkColorScheme(
            primary = Brass,
            onPrimary = NightDeep,
            secondary = EmeraldAccent,
            background = NightMid,
            onBackground = AmberText,
            surface = NightSlate,
            onSurface = AmberText,
            error = RoseError
        )
    } else {
        lightColorScheme(
            primary = Brass,
            onPrimary = NightSlate,
            secondary = EmeraldAccent,
            background = NightMid,
            onBackground = AmberText,
            surface = NightSlate,
            onSurface = AmberText,
            error = RoseError
        )
    }

    MaterialTheme(
        colorScheme = scheme,
        typography = QiblaTypography,
        content = content
    )
}
