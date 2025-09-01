package com.example.pokemonshinytracker

import android.graphics.*
import coil.size.Size
import coil.transform.Transformation

class ShadowTransformation(
    private val shadowRadius: Float = 12f,
    private val dx: Float = 6f,
    private val dy: Float = 6f,
) : Transformation {

    override val cacheKey: String = "${javaClass.name}-$shadowRadius-$dx-$dy-${Color.BLACK}"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            maskFilter = BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL)
        }

        // new bitmap with padding for shadow
        val output = Bitmap.createBitmap(
            input.width + (dx.toInt() * 2),
            input.height + (dy.toInt() * 2),
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(output)

        // draw shadow
        val alpha = input.extractAlpha(paint, null)
        canvas.drawBitmap(alpha, dx, dy, paint)
        alpha.recycle()

        // draw original image on top
        canvas.drawBitmap(input, 0f, 0f, null)

        return output
    }
}
