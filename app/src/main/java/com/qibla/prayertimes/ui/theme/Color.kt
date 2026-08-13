package com.qibla.prayertimes.ui.theme

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Holds the current dark/light theme choice as Compose state, so every screen that reads a
 * color token below automatically recomposes when the theme is toggled — no need to thread a
 * "isDark" parameter through every composable in the app.
 */
object ThemeState {
    var isDark by mutableStateOf(true)

    fun toggle(context: Context) {
        isDark = !isDark
        ThemePrefs(context).setDark(isDark)
    }

    fun toggleTo(context: Context, dark: Boolean) {
        isDark = dark
        ThemePrefs(context).setDark(dark)
    }

    fun initFrom(context: Context) {
        isDark = ThemePrefs(context).isDark()
    }
}

class ThemePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("qibla_theme_prefs", Context.MODE_PRIVATE)
    fun isDark(): Boolean = prefs.getBoolean("is_dark", true)
    fun setDark(value: Boolean) { prefs.edit().putBoolean("is_dark", value).apply() }
}

// ---- Dark palette (original "night sky + brass" look) ----
private val DarkNightDeep = Color(0xFF081420)
private val DarkNightMid = Color(0xFF0C1A2B)
private val DarkNightSlate = Color(0xFF16324A)
private val DarkCardSurface = Color(0x0DFFFFFF)
private val DarkCardBorder = Color(0x26F2D8A0)
private val DarkBrassLight = Color(0xFFF2D8A0)
private val DarkBrass = Color(0xFFC9A15C)
private val DarkBrassDark = Color(0xFF8A6A35)
private val DarkAmberText = Color(0xFFFFF6E5)
private val DarkAmberMuted = Color(0x80F2D8A0)
private val DarkAmberFaint = Color(0x40F2D8A0)
private val DarkEmeraldAccent = Color(0xFF2E7D6E)
private val DarkRoseError = Color(0xFFE5A3A3)
private val DarkOccasionMourning = Color(0xFFB0A6A6)

// ---- Light palette (warm parchment + antique gold) ----
private val LightNightDeep = Color(0xFFEBD9AE)
private val LightNightMid = Color(0xFFF6EAD0)
private val LightNightSlate = Color(0xFFFFFBF2)
private val LightCardSurface = Color(0x143B2E1A)
private val LightCardBorder = Color(0x40B8863B)
private val LightBrassLight = Color(0xFFD4A24F)
private val LightBrass = Color(0xFFB8863B)
private val LightBrassDark = Color(0xFF8C6423)
private val LightAmberText = Color(0xFF2A2013)
private val LightAmberMuted = Color(0x992A2013)
private val LightAmberFaint = Color(0x662A2013)
private val LightEmeraldAccent = Color(0xFF1F6E5E)
private val LightRoseError = Color(0xFFB0524F)
private val LightOccasionMourning = Color(0xFF4A3F3F)

// ---- Public tokens used throughout the app; theme-aware via ThemeState.isDark ----
val NightDeep: Color get() = if (ThemeState.isDark) DarkNightDeep else LightNightDeep
val NightMid: Color get() = if (ThemeState.isDark) DarkNightMid else LightNightMid
val NightSlate: Color get() = if (ThemeState.isDark) DarkNightSlate else LightNightSlate
val CardSurface: Color get() = if (ThemeState.isDark) DarkCardSurface else LightCardSurface
val CardBorder: Color get() = if (ThemeState.isDark) DarkCardBorder else LightCardBorder

val BrassLight: Color get() = if (ThemeState.isDark) DarkBrassLight else LightBrassLight
val Brass: Color get() = if (ThemeState.isDark) DarkBrass else LightBrass
val BrassDark: Color get() = if (ThemeState.isDark) DarkBrassDark else LightBrassDark

val AmberText: Color get() = if (ThemeState.isDark) DarkAmberText else LightAmberText
val AmberMuted: Color get() = if (ThemeState.isDark) DarkAmberMuted else LightAmberMuted
val AmberFaint: Color get() = if (ThemeState.isDark) DarkAmberFaint else LightAmberFaint

val EmeraldAccent: Color get() = if (ThemeState.isDark) DarkEmeraldAccent else LightEmeraldAccent
val RoseError: Color get() = if (ThemeState.isDark) DarkRoseError else LightRoseError
/** Used for mourning-type [com.qibla.prayertimes.data.ShiaOccasion] badges — deliberately somber, not the celebratory emerald. */
val OccasionMourning: Color get() = if (ThemeState.isDark) DarkOccasionMourning else LightOccasionMourning

// Faint neutral fills (button backgrounds, unselected chips, subtle row highlights) that need
// to flip from a white-alpha wash on dark backgrounds to a dark-alpha wash on light ones.
val OverlayFaint: Color get() = if (ThemeState.isDark) Color(0x0DFFFFFF) else Color(0x0D2A2013)
val OverlayMedium: Color get() = if (ThemeState.isDark) Color(0x14FFFFFF) else Color(0x142A2013)
val OverlayStrong: Color get() = if (ThemeState.isDark) Color(0x1AFFFFFF) else Color(0x1A2A2013)
