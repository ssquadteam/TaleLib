@file:JvmName("MapColorsKt")

package com.github.ssquadteam.talelib.worldmap

/**
 * Color utilities and constants for world map manipulation.
 *
 * All colors are in ARGB format (Alpha, Red, Green, Blue).
 * Each component is 8 bits (0-255).
 *
 * Example usage:
 * ```kotlin
 * // Using predefined colors
 * fill(MapColors.RED)
 * setPixel(x, y, MapColors.FOREST_GREEN)
 *
 * // Creating custom colors
 * val myColor = MapColors.rgb(128, 64, 255)
 * val transparent = MapColors.withAlpha(MapColors.BLUE, 0.5f)
 * ```
 */
object MapColors {
    const val TRANSPARENT: Int = 0x00000000
    const val BLACK: Int = 0xFF000000.toInt()
    const val WHITE: Int = 0xFFFFFFFF.toInt()
    const val RED: Int = 0xFFFF0000.toInt()
    const val GREEN: Int = 0xFF00FF00.toInt()
    const val BLUE: Int = 0xFF0000FF.toInt()
    const val YELLOW: Int = 0xFFFFFF00.toInt()
    const val CYAN: Int = 0xFF00FFFF.toInt()
    const val MAGENTA: Int = 0xFFFF00FF.toInt()
    const val ORANGE: Int = 0xFFFF8000.toInt()
    const val PURPLE: Int = 0xFF8000FF.toInt()
    const val PINK: Int = 0xFFFF80C0.toInt()

    const val GRAY: Int = 0xFF808080.toInt()
    const val DARK_GRAY: Int = 0xFF404040.toInt()
    const val LIGHT_GRAY: Int = 0xFFC0C0C0.toInt()

    const val FOREST_GREEN: Int = 0xFF228B22.toInt()
    const val GRASS_GREEN: Int = 0xFF7CFC00.toInt()
    const val DARK_GREEN: Int = 0xFF006400.toInt()
    const val SAND: Int = 0xFFC2B280.toInt()
    const val DESERT: Int = 0xFFEDC9AF.toInt()
    const val SNOW: Int = 0xFFFFFAFA.toInt()
    const val ICE: Int = 0xFFB0E0E6.toInt()
    const val WATER: Int = 0xFF4169E1.toInt()
    const val DEEP_WATER: Int = 0xFF000080.toInt()
    const val LAVA: Int = 0xFFFF4500.toInt()
    const val STONE: Int = 0xFF808080.toInt()
    const val DIRT: Int = 0xFF8B4513.toInt()
    const val MUD: Int = 0xFF5C4033.toInt()

    const val MARKER_RED: Int = 0xFFE53935.toInt()
    const val MARKER_GREEN: Int = 0xFF43A047.toInt()
    const val MARKER_BLUE: Int = 0xFF1E88E5.toInt()
    const val MARKER_YELLOW: Int = 0xFFFDD835.toInt()
    const val MARKER_ORANGE: Int = 0xFFFB8C00.toInt()
    const val MARKER_PURPLE: Int = 0xFF8E24AA.toInt()

    fun rgb(r: Int, g: Int, b: Int): Int {
        return (0xFF shl 24) or
                ((r and 0xFF) shl 16) or
                ((g and 0xFF) shl 8) or
                (b and 0xFF)
    }

    fun rgba(r: Int, g: Int, b: Int, a: Int): Int {
        return ((a and 0xFF) shl 24) or
                ((r and 0xFF) shl 16) or
                ((g and 0xFF) shl 8) or
                (b and 0xFF)
    }

    fun rgbf(r: Float, g: Float, b: Float, a: Float = 1f): Int {
        return rgba(
            (r.coerceIn(0f, 1f) * 255).toInt(),
            (g.coerceIn(0f, 1f) * 255).toInt(),
            (b.coerceIn(0f, 1f) * 255).toInt(),
            (a.coerceIn(0f, 1f) * 255).toInt()
        )
    }

    fun withAlpha(color: Int, alpha: Float): Int {
        val a = (alpha.coerceIn(0f, 1f) * 255).toInt()
        return (a shl 24) or (color and 0x00FFFFFF)
    }

    fun alpha(color: Int): Int = (color ushr 24) and 0xFF

    fun red(color: Int): Int = (color ushr 16) and 0xFF

    fun green(color: Int): Int = (color ushr 8) and 0xFF

    fun blue(color: Int): Int = color and 0xFF

    fun lerp(color1: Int, color2: Int, t: Float): Int {
        val factor = t.coerceIn(0f, 1f)
        val invFactor = 1f - factor

        val a = (alpha(color1) * invFactor + alpha(color2) * factor).toInt()
        val r = (red(color1) * invFactor + red(color2) * factor).toInt()
        val g = (green(color1) * invFactor + green(color2) * factor).toInt()
        val b = (blue(color1) * invFactor + blue(color2) * factor).toInt()

        return rgba(r, g, b, a)
    }

    fun darken(color: Int, factor: Float): Int {
        val f = factor.coerceIn(0f, 1f)
        return rgba(
            (red(color) * f).toInt(),
            (green(color) * f).toInt(),
            (blue(color) * f).toInt(),
            alpha(color)
        )
    }

    fun lighten(color: Int, factor: Float): Int {
        val f = factor.coerceIn(0f, 1f)
        return rgba(
            (red(color) + (255 - red(color)) * f).toInt(),
            (green(color) + (255 - green(color)) * f).toInt(),
            (blue(color) + (255 - blue(color)) * f).toInt(),
            alpha(color)
        )
    }

    fun fromHex(hex: String): Int {
        val clean = hex.removePrefix("#").uppercase()
        return when (clean.length) {
            3 -> { // RGB
                val r = clean[0].digitToInt(16) * 17
                val g = clean[1].digitToInt(16) * 17
                val b = clean[2].digitToInt(16) * 17
                rgb(r, g, b)
            }
            4 -> { // RGBA
                val r = clean[0].digitToInt(16) * 17
                val g = clean[1].digitToInt(16) * 17
                val b = clean[2].digitToInt(16) * 17
                val a = clean[3].digitToInt(16) * 17
                rgba(r, g, b, a)
            }
            6 -> { // RRGGBB
                val r = clean.substring(0, 2).toInt(16)
                val g = clean.substring(2, 4).toInt(16)
                val b = clean.substring(4, 6).toInt(16)
                rgb(r, g, b)
            }
            8 -> { // RRGGBBAA
                val r = clean.substring(0, 2).toInt(16)
                val g = clean.substring(2, 4).toInt(16)
                val b = clean.substring(4, 6).toInt(16)
                val a = clean.substring(6, 8).toInt(16)
                rgba(r, g, b, a)
            }
            else -> BLACK
        }
    }

    fun toHex(color: Int, includeAlpha: Boolean = false): String {
        return if (includeAlpha) {
            String.format("#%02X%02X%02X%02X", red(color), green(color), blue(color), alpha(color))
        } else {
            String.format("#%02X%02X%02X", red(color), green(color), blue(color))
        }
    }
}
