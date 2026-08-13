package com.qibla.prayertimes.widget

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.widget.RemoteViews
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.AndroidRemoteViews
import androidx.glance.text.TextAlign
import com.qibla.prayertimes.R

/**
 * Renders a single line of text using the app's estedad font inside a Glance widget.
 *
 * Glance's own `Text()` composable only supports a handful of generic system font families
 * (sans-serif, serif, monospace, cursive) — it has no way to embed a bundled .ttf. The only
 * escape hatch is to fall back to a real RemoteViews layout (which, unlike Glance, is inflated
 * with this package's own resources and so *can* resolve `@font/estedad`) via
 * [AndroidRemoteViews] — the same trick this project already used for the clock and countdown
 * views. This wraps that trick so every other widget text can use it too.
 *
 * @param color an ARGB color int (e.g. `0xFF8A6A2E.toInt()`), not a Glance ColorProvider.
 */
@Composable
fun EstedadText(
    context: Context,
    text: String,
    color: Int,
    sizeSp: Float,
    bold: Boolean = false,
    align: TextAlign = TextAlign.Start,
    maxLines: Int? = null,
    modifier: GlanceModifier = GlanceModifier
) {
    val layoutRes = if (bold) R.layout.widget_text_estedad_bold else R.layout.widget_text_estedad
    val rv = RemoteViews(context.packageName, layoutRes)
    rv.setTextViewText(R.id.widget_text, text)
    rv.setTextColor(R.id.widget_text, color)
    rv.setTextViewTextSize(R.id.widget_text, TypedValue.COMPLEX_UNIT_SP, sizeSp)
    rv.setInt(R.id.widget_text, "setGravity", gravityFor(align))
    if (maxLines != null) {
        rv.setInt(R.id.widget_text, "setMaxLines", maxLines)
    }
    AndroidRemoteViews(rv, modifier)
}

private fun gravityFor(align: TextAlign): Int = when (align) {
    TextAlign.Center -> Gravity.CENTER
    TextAlign.End -> Gravity.END or Gravity.CENTER_VERTICAL
    TextAlign.Left -> Gravity.LEFT or Gravity.CENTER_VERTICAL
    TextAlign.Right -> Gravity.RIGHT or Gravity.CENTER_VERTICAL
    else -> Gravity.START or Gravity.CENTER_VERTICAL
}
