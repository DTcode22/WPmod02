package com.example.wpmod02

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.sin
import kotlin.ranges.ClosedFloatingPointRange
import kotlin.ranges.ClosedRange

// Custom progression for floating point values
class FloatProgression(
    val start: Float,
    val endInclusive: Float,
    val step: Float
) : Iterable<Float> {
    override fun iterator(): Iterator<Float> = FloatProgressionIterator(start, endInclusive, step)
}

class FloatProgressionIterator(
    private val start: Float,
    private val endInclusive: Float,
    private val step: Float
) : Iterator<Float> {
    private var current = start
    private val finalElement = endInclusive
    private val hasNext = if (step > 0) current <= finalElement else current >= finalElement

    override fun hasNext(): Boolean = hasNext && current <= finalElement

    override fun next(): Float {
        val value = current
        current += step
        return value
    }
}

// Extension function to create a float progression
fun ClosedFloatingPointRange<Float>.step(step: Float): FloatProgression {
    return FloatProgression(start, endInclusive, step)
}

class MyWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = MyWallpaperEngine()

    inner class MyWallpaperEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val drawRunnable = object : Runnable {
            override fun run() {
                drawFrame()
                if (isVisible) {
                    handler.postDelayed(this, 33) // ~30fps to save battery
                }
            }
        }
        private var width = 0f
        private var height = 0f
        private var xOffset = 0f
        private var yOffset = 0f
        private var scale = 0f
        private val startTime = System.currentTimeMillis()
        private val defaultParams = PatternParams()
        private var currentParams = defaultParams.copy()
        private var mode = "default"

        private val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this@MyWallpaperService.applicationContext)
        private val prefListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                "mode" -> updateParams()
                "waveAmplitude", "rotationAmplitude", "angleFrequency", "densityFactor" -> if (mode == "custom") updateParams()
            }
        }

        init {
            prefs.registerOnSharedPreferenceChangeListener(prefListener)
            updateParams()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            this.width = width.toFloat()
            this.height = height.toFloat()
            xOffset = width / 2f
            yOffset = height / 2f
            scale = (width / 400f) * defaultParams.scale // Scale pattern to device width (HTML base: 400px)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                handler.post(drawRunnable)
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawRunnable)
            prefs.unregisterOnSharedPreferenceChangeListener(prefListener)
        }

        private fun drawFrame() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                val time = (System.currentTimeMillis() - startTime) / 1000f * currentParams.speed
                val breathingFactor = sin(time / currentParams.breathPeriod) * currentParams.breathAmplitude + currentParams.breathBase
                val rotationFactor = sin(time / currentParams.rotationPeriod) * currentParams.rotationAmplitude + currentParams.rotationBase
                val waveFactor = sin(time / currentParams.wavePeriod) * currentParams.waveAmplitude + currentParams.waveBase

                canvas.drawColor(currentParams.backgroundColor)
                val paint = Paint().apply { style = Paint.Style.FILL }

                for (r in (currentParams.rMin..currentParams.rMax).step(currentParams.rStep)) {
                    val pointCount = floor(r * currentParams.densityFactor).toInt()
                    for (i in 0 until pointCount) {
                        val angle = (i.toFloat() / pointCount) * 2 * Math.PI.toFloat()
                        val radiusModifier = 1 + sin(r / currentParams.radiusDivisor + time * currentParams.radiusTimeFactor) * currentParams.radiusAmplitude
                        val angleModifier = sin(angle * currentParams.angleFrequency + time * currentParams.angleTimeFactor) * currentParams.angleAmplitude
                        val modifiedRadius = r * radiusModifier * breathingFactor
                        val modifiedAngle = angle + angleModifier + (r / currentParams.rMax) * rotationFactor
                        val x = cos(modifiedAngle) * modifiedRadius
                        val y = sin(modifiedAngle) * modifiedRadius
                        val waveDistortion = sin(angle * currentParams.waveFrequency + time * currentParams.waveTimeFactor) * waveFactor * (r / currentParams.rMax)
                        val finalX = x + waveDistortion * cos(angle + Math.PI.toFloat() / 2)
                        val finalY = y + waveDistortion * sin(angle + Math.PI.toFloat() / 2)
                        val px = (finalX * scale) + xOffset
                        val py = (finalY * scale) + yOffset

                        var hue = ((angle / (2 * Math.PI.toFloat())) * currentParams.hueRange + time * currentParams.hueSpeed) % 360
                        if (hue < 0) hue += 360
                        val saturation = (currentParams.baseSaturation - (r / currentParams.rMax) * currentParams.saturationRange) / 100f
                        val value = (currentParams.baseLightness + (r / currentParams.rMax) * currentParams.lightnessRange + sin(angle * 3 + time) * currentParams.lightnessPulse) / 100f
                        val color = Color.HSVToColor(255, floatArrayOf(hue.toFloat(), saturation.coerceIn(0f, 1f), value.coerceIn(0f, 1f)))
                        paint.color = color
                        val dotSizeVariation = currentParams.dotSize * (1 - (r / currentParams.rMax) * currentParams.dotSizeVariationFactor)
                        canvas.drawCircle(px, py, max(0.5f, dotSizeVariation), paint)
                    }
                }
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        private fun updateParams() {
            mode = prefs.getString("mode", "default") ?: "default"
            when (mode) {
                "default" -> currentParams = defaultParams.copy()
                "custom" -> {
                    currentParams = defaultParams.copy(
                        waveAmplitude = prefs.getFloat("waveAmplitude", 12f),
                        rotationAmplitude = prefs.getFloat("rotationAmplitude", 0.8f),
                        angleFrequency = prefs.getFloat("angleFrequency", 3f),
                        densityFactor = prefs.getFloat("densityFactor", 0.8f)
                    )
                }
                "random" -> updateRandomParams()
            }
        }

        private fun updateRandomParams() {
            if (mode == "random") {
                currentParams = defaultParams.copy(
                    waveAmplitude = (0..40).random() * 0.5f, // 0 to 20
                    rotationAmplitude = (0..20).random() * 0.1f, // 0 to 2
                    angleFrequency = (2..20).random() * 0.5f, // 1 to 10
                    densityFactor = (1..20).random() * 0.1f // 0.1 to 2
                )
                handler.postDelayed({ updateRandomParams() }, 10000) // Update every 10 seconds
            }
        }
    }
}

data class PatternParams(
    val speed: Float = 0.8f,
    val scale: Float = 1.6f,
    val dotSize: Float = 1.5f,
    val backgroundColor: Int = Color.rgb(5, 5, 15),
    val rMin: Float = 5f,
    val rMax: Float = 120f,
    val rStep: Float = 2f,
    val densityFactor: Float = 0.8f,
    val radiusDivisor: Float = 10f,
    val radiusTimeFactor: Float = 0.5f,
    val radiusAmplitude: Float = 0.2f,
    val angleFrequency: Float = 3f,
    val angleTimeFactor: Float = 0.3f,
    val angleAmplitude: Float = 0.4f,
    val breathPeriod: Float = 8f,
    val breathAmplitude: Float = 0.15f,
    val breathBase: Float = 1f,
    val rotationPeriod: Float = 15f,
    val rotationAmplitude: Float = 0.8f,
    val rotationBase: Float = 0.2f,
    val wavePeriod: Float = 10f,
    val waveAmplitude: Float = 12f,
    val waveBase: Float = 8f,
    val waveFrequency: Float = 4f,
    val waveTimeFactor: Float = 0.5f,
    val hueRange: Float = 60f,
    val hueSpeed: Float = 10f,
    val baseSaturation: Float = 80f,
    val saturationRange: Float = 30f,
    val baseLightness: Float = 50f,
    val lightnessRange: Float = 20f,
    val lightnessPulse: Float = 10f,
    val dotSizeVariationFactor: Float = 0.5f,
    val cyclePeriod: Float = 30f
)