package org.skyfaced.circlechart.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random.Default.nextFloat

val String.Companion.Empty get() = ""

val Color.Companion.Random get(): Color = Color((nextFloat() * 16777215).toInt() or (0xFF shl 24))

@Deprecated("Use Color.RainbowHsl instead it's more accurate on calculations")
val Color.Companion.RainbowRgb by lazy<List<Color>>(mode = LazyThreadSafetyMode.NONE) {
    val colors = mutableListOf<Color>()

    for (i in 0..31) {
        val red = sin(0.2f * i + 0) * 127 + 128
        val green = sin(0.2f * i + 2) * 127 + 128
        val blue = sin(0.2f * i + 4) * 127 + 128
        colors.add(Color(red.roundToInt(), green.roundToInt(), blue.roundToInt()))
    }

    colors
}

@Deprecated("Use Color.RainbowHsl() instead it's more accurate on calculations")
fun Color.Companion.RainbowRgb(
    start: Int = 0,
    stop: Int = 32,
    frequency: Float = 0.2f,
): List<Color> {
    val colors = mutableListOf<Color>()

    for (i in start..stop) {
        val red = sin(frequency * i + 0) * 127 + 128
        val green = sin(frequency * i + 2) * 127 + 128
        val blue = sin(frequency * i + 4) * 127 + 128
        colors.add(Color(red.roundToInt(), green.roundToInt(), blue.roundToInt()))
    }

    return colors
}

@OptIn(ExperimentalGraphicsApi::class)
val Color.Companion.RainbowHsl by lazy(LazyThreadSafetyMode.NONE) {
    Color.RainbowHsl()
}

@OptIn(ExperimentalGraphicsApi::class)
fun Color.Companion.RainbowHsl(
    saturation: Float = ColorDefaults.Hsl.Saturation,
    lightness: Float = ColorDefaults.Hsl.Lightness,
    alpha: Float = ColorDefaults.Hsl.Alpha,
) = List(360) {
    hsl(it.toFloat(), saturation, lightness, alpha)
}

@OptIn(ExperimentalGraphicsApi::class)
fun List<Color>.rightShift(shift: Int) = drop(shift) + take(shift)

fun List<Color>.leftShift(shift: Int) = takeLast(shift) + dropLast(shift)