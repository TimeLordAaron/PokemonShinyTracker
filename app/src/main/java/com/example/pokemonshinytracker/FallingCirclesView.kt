package com.example.pokemonshinytracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class FallingCirclesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private data class Circle(
        var x: Float,
        var y: Float,
        val radius: Float,
        val color: Int,
        val speed: Float,
        val sway: Float
    )

    private val circles = mutableListOf<Circle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var circleColors: List<Int> = listOf(Color.parseColor("#66FFFFFF")) // default: white
    private var circleCount = 32    // default: 32
    private val frameDelay = 16L    // ~60fps
    private var isRunning = false

    // update runnable must be declared before usage
    private val updateRunnable = object : Runnable {
        override fun run() {
            val w = width.coerceAtLeast(1)
            val h = height.coerceAtLeast(1)

            if (circles.isNotEmpty()) {
                for (i in circles.indices) {
                    val c = circles[i]
                    c.y += c.speed  // vertical speed to make the circle rain down the screen
                    c.x += c.sway   // small horizontal sway to make motion organic
                    if (c.x < -c.radius) c.x = (w + c.radius)
                    if (c.x > w + c.radius) c.x = -c.radius

                    // recycle when below the bottom
                    if (c.y - c.radius > h) {
                        circles[i] = createCircle(w, h, startAbove = true)
                    }
                }
                invalidate()
            }

            // schedule next frame if still running and view visible
            if (isRunning && visibility == VISIBLE) {
                postDelayed(this, frameDelay)
            } else {
                isRunning = false
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // recreate circles for the new size
        circles.clear()
        if (w > 0 && h > 0) {
            repeat(circleCount) {
                circles.add(createCircle(w, h, startAbove = false))
            }
            // start the animation if view visible
            if (visibility == VISIBLE) {
                start()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (c in circles) {
            paint.color = c.color
            canvas.drawCircle(c.x, c.y, c.radius, paint)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) start() else stop()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    // Function to create a single circle
    private fun createCircle(viewWidth: Int, viewHeight: Int, startAbove: Boolean = false): Circle {
        val radius = Random.nextInt(6, 20).toFloat() * resources.displayMetrics.density // size in dp-ish
        val x = Random.nextFloat() * viewWidth
        val y = if (startAbove) -radius else Random.nextFloat() * viewHeight    // if startAbove is True, place it just above the top
        val speed = Random.nextFloat() * 1.5f + 0.5f   // slow fall
        val sway = (Random.nextFloat() - 0.5f) * 1.2f  // slight horizontal movement
        val color = circleColors.random()
        return Circle(x, y, radius, color, speed, sway)
    }

    // Function to start animation loop
    fun start() {
        if (!isRunning && circles.isNotEmpty() && visibility == VISIBLE) {
            isRunning = true
            removeCallbacks(updateRunnable)
            post(updateRunnable)
        }
    }

    // Function to stop animation loop
    fun stop() {
        isRunning = false
        removeCallbacks(updateRunnable)
    }

    // Function to set the colors of the circles
    fun setColors(colors: List<Int>) {
        if (colors.isNotEmpty()) {
            circleColors = colors
            // regenerate circles with new colors
            if (width > 0 && height > 0) {
                onSizeChanged(width, height, width, height)
            }
        }
    }

    // Function to set the circle count
    fun setCircleCount(count: Int) {
        circleCount = count
        if (width > 0 && height > 0) {
            onSizeChanged(width, height, width, height)
        }
    }

}
