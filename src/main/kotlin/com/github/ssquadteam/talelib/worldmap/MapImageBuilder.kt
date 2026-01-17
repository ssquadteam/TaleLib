@file:JvmName("MapImageBuilderKt")

package com.github.ssquadteam.talelib.worldmap

import com.hypixel.hytale.protocol.packets.worldmap.MapImage
import kotlin.math.min

/**
 * DSL builder for creating and manipulating map chunk images.
 *
 * Map images are rendered as ARGB pixel arrays. Default size is 32x32.
 *
 * Example usage:
 * ```kotlin
 * val image = mapImage(32, 32) {
 *     fill(MapColors.TRANSPARENT)
 *     drawRect(0, 0, 16, 16, MapColors.RED)
 *     setPixel(16, 16, MapColors.BLUE)
 * }
 * ```
 */
class MapImageBuilder(val width: Int = 32, val height: Int = 32) {
    private val pixels: IntArray = IntArray(width * height)

    fun fill(color: Int): MapImageBuilder {
        pixels.fill(color)
        return this
    }

    fun setPixel(x: Int, y: Int, color: Int): MapImageBuilder {
        if (x in 0 until width && y in 0 until height) {
            pixels[y * width + x] = color
        }
        return this
    }

    fun getPixel(x: Int, y: Int): Int {
        return if (x in 0 until width && y in 0 until height) {
            pixels[y * width + x]
        } else 0
    }

    fun drawRect(x: Int, y: Int, w: Int, h: Int, color: Int): MapImageBuilder {
        for (dy in 0 until h) {
            for (dx in 0 until w) {
                setPixel(x + dx, y + dy, color)
            }
        }
        return this
    }

    fun drawRectOutline(x: Int, y: Int, w: Int, h: Int, color: Int): MapImageBuilder {
        for (dx in 0 until w) {
            setPixel(x + dx, y, color)
            setPixel(x + dx, y + h - 1, color)
        }
        for (dy in 0 until h) {
            setPixel(x, y + dy, color)
            setPixel(x + w - 1, y + dy, color)
        }
        return this
    }

    fun drawHLine(x: Int, y: Int, length: Int, color: Int): MapImageBuilder {
        for (dx in 0 until length) {
            setPixel(x + dx, y, color)
        }
        return this
    }

    fun drawVLine(x: Int, y: Int, length: Int, color: Int): MapImageBuilder {
        for (dy in 0 until length) {
            setPixel(x, y + dy, color)
        }
        return this
    }

    fun drawImage(srcPixels: IntArray, srcWidth: Int, offsetX: Int = 0, offsetY: Int = 0): MapImageBuilder {
        val srcHeight = srcPixels.size / srcWidth
        for (sy in 0 until srcHeight) {
            for (sx in 0 until srcWidth) {
                val color = srcPixels[sy * srcWidth + sx]
                if ((color ushr 24) > 0) {
                    setPixel(offsetX + sx, offsetY + sy, color)
                }
            }
        }
        return this
    }

    fun overlay(other: MapImageBuilder, alpha: Float = 1f, offsetX: Int = 0, offsetY: Int = 0): MapImageBuilder {
        for (y in 0 until min(other.height, height - offsetY)) {
            for (x in 0 until min(other.width, width - offsetX)) {
                val srcColor = other.getPixel(x, y)
                val srcAlpha = ((srcColor ushr 24) and 0xFF) / 255f * alpha

                if (srcAlpha > 0) {
                    val dstColor = getPixel(offsetX + x, offsetY + y)
                    val blended = blendColors(dstColor, srcColor, srcAlpha)
                    setPixel(offsetX + x, offsetY + y, blended)
                }
            }
        }
        return this
    }

    fun clear(): MapImageBuilder {
        pixels.fill(0)
        return this
    }

    fun getPixels(): IntArray = pixels.copyOf()

    internal fun build(): MapImage {
        return MapImage(width, height, pixels.copyOf())
    }

    private fun blendColors(dst: Int, src: Int, srcAlpha: Float): Int {
        val dstA = ((dst ushr 24) and 0xFF) / 255f
        val dstR = (dst ushr 16) and 0xFF
        val dstG = (dst ushr 8) and 0xFF
        val dstB = dst and 0xFF

        val srcR = (src ushr 16) and 0xFF
        val srcG = (src ushr 8) and 0xFF
        val srcB = src and 0xFF

        val outA = srcAlpha + dstA * (1 - srcAlpha)
        if (outA == 0f) return 0

        val outR = ((srcR * srcAlpha + dstR * dstA * (1 - srcAlpha)) / outA).toInt()
        val outG = ((srcG * srcAlpha + dstG * dstA * (1 - srcAlpha)) / outA).toInt()
        val outB = ((srcB * srcAlpha + dstB * dstA * (1 - srcAlpha)) / outA).toInt()

        return ((outA * 255).toInt() shl 24) or (outR shl 16) or (outG shl 8) or outB
    }
}

fun mapImage(width: Int = 32, height: Int = 32, block: MapImageBuilder.() -> Unit): MapImage {
    return MapImageBuilder(width, height).apply(block).build()
}

fun mapImageBuilder(width: Int = 32, height: Int = 32, block: MapImageBuilder.() -> Unit = {}): MapImageBuilder {
    return MapImageBuilder(width, height).apply(block)
}
