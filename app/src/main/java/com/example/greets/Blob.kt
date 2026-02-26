package com.example.greets

import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

class Blob(
    val name: String,
    val color: Int,
    var x: Float,
    var y: Float,
    var velocityY: Float = 0f,
    var velocityX: Float = 0f,
    var isLanded: Boolean = false,
    var isBeingDragged: Boolean = false,
    val radius: Float = 60f
) {
    // Paint objects initialized with blob's color
    val paint: Paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        this.color = this@Blob.color
    }

    val textPaint: Paint = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    companion object {
        fun createRandom(name: String, screenWidth: Float): Blob {
            val vibrantColors = listOf(
                Color.parseColor("#FF6B6B"),
                Color.parseColor("#4ECDC4"),
                Color.parseColor("#45B7D1"),
                Color.parseColor("#96CEB4"),
                Color.parseColor("#FFEAA7"),
                Color.parseColor("#DDA0DD"),
                Color.parseColor("#98D8C8"),
                Color.parseColor("#F7DC6F"),
                Color.parseColor("#BB8FCE"),
                Color.parseColor("#85C1E9"),
                Color.parseColor("#F8B500"),
                Color.parseColor("#6C5CE7"),
                Color.parseColor("#A8E6CF"),
                Color.parseColor("#FD79A8"),
                Color.parseColor("#FDCB6E")
            )

            val randomColor = vibrantColors.random()
            val startX = Random.nextFloat() * (screenWidth - 120) + 60

            return Blob(
                name = name,
                color = randomColor,
                x = startX,
                y = -100f,
                velocityY = 0f,
                velocityX = 0f
            )
        }
    }
}