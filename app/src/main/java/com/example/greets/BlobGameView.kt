package com.example.greets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class BlobGameView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val TAG = "BlobGameView"
    private val blobs = mutableListOf<Blob>()
    private val particles = mutableListOf<Particle>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isRunning = false

    // Touch handling
    private var selectedBlob: Blob? = null
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var touchStartTime = 0L
    private var clickCount = 0
    private var lastClickedBlob: Blob? = null
    private val clickHandler = Handler(Looper.getMainLooper())

    // Explosion tracking
    private val explosionClicks = mutableMapOf<Blob, Int>()
    private val clickResetHandler = Handler(Looper.getMainLooper())

    // Sound effects
    private val popSound: MediaPlayer? by lazy {
        try {
            MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        } catch (e: Exception) {
            null
        }
    }

    private val gravity = 0.8f
    private val groundY: Float get() = height - 200f
    private val bounceDamping = 0.6f
    private val runSpeed = 5f

    private val bgPaint = Paint().apply {
        color = Color.parseColor("#1A1A2E")
    }

    private val groundPaint = Paint().apply {
        color = Color.parseColor("#16213E")
        style = Paint.Style.FILL
    }

    private val stars = mutableListOf<Pair<Float, Float>>()
    private val starPaint = Paint().apply {
        color = Color.WHITE
        alpha = 200
    }

    private val selectionPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
        isAntiAlias = true
    }

    init {
        generateStars()
        isRunning = true
        startGameLoop()
        Log.d(TAG, "BlobGameView created with touch support")
    }

    private fun generateStars() {
        repeat(50) {
            stars.add(
                Pair(
                    Random.nextFloat() * 1000,
                    Random.nextFloat() * 1000
                )
            )
        }
    }

    private fun startGameLoop() {
        val runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    updatePhysics()
                    updateParticles()
                    invalidate()
                    mainHandler.postDelayed(this, 16)
                }
            }
        }
        mainHandler.post(runnable)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartTime = System.currentTimeMillis()
                lastTouchX = x
                lastTouchY = y

                // Find clicked blob
                val clickedBlob = findBlobAt(x, y)

                if (clickedBlob != null) {
                    // Check for rapid clicking (explosion)
                    if (lastClickedBlob == clickedBlob) {
                        clickCount++
                        if (clickCount >= 5) {
                            explodeBlob(clickedBlob)
                            clickCount = 0
                            lastClickedBlob = null
                            return true
                        }
                    } else {
                        clickCount = 1
                        lastClickedBlob = clickedBlob
                    }

                    // Reset click count after delay
                    clickResetHandler.removeCallbacksAndMessages(null)
                    clickResetHandler.postDelayed({
                        clickCount = 0
                        lastClickedBlob = null
                    }, 1000)

                    // Select blob for dragging
                    selectedBlob = clickedBlob
                    touchOffsetX = x - clickedBlob.x
                    touchOffsetY = y - clickedBlob.y
                    clickedBlob.isBeingDragged = true
                    clickedBlob.velocityX = 0f
                    clickedBlob.velocityY = 0f
                    invalidate()
                    return true
                }
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                selectedBlob?.let { blob ->
                    blob.x = x - touchOffsetX
                    blob.y = y - touchOffsetY

                    // Calculate throw velocity
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY
                    blob.velocityX = dx * 0.5f
                    blob.velocityY = dy * 0.5f

                    lastTouchX = x
                    lastTouchY = y
                    invalidate()
                    return true
                }
                return false
            }

            MotionEvent.ACTION_UP -> {
                selectedBlob?.let { blob ->
                    blob.isBeingDragged = false

                    // Check if it was a quick tap (click) vs drag
                    val touchDuration = System.currentTimeMillis() - touchStartTime
                    val moveDistance = sqrt((x - lastTouchX).pow(2) + (y - lastTouchY).pow(2))

                    // If it was a drag (moved far or took long), apply throw physics
                    if (moveDistance > 10 || touchDuration > 200) {
                        // Throw! - velocity already set during move
                        blob.isLanded = false
                        playPopSound()
                    }

                    selectedBlob = null
                    invalidate()
                    return true
                }
                return false
            }
        }
        return false
    }

    private fun findBlobAt(x: Float, y: Float): Blob? {
        // Check in reverse order (top blobs first)
        return blobs.reversed().find { blob ->
            val dx = x - blob.x
            val dy = y - blob.y
            sqrt(dx * dx + dy * dy) <= blob.radius + 20f // +20 for easier touch
        }
    }

    private fun explodeBlob(blob: Blob) {
        // Create explosion particles
        val particleCount = 20
        repeat(particleCount) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = Random.nextFloat() * 15f + 5f
            val vx = kotlin.math.cos(angle).toFloat() * speed
            val vy = kotlin.math.sin(angle).toFloat() * speed

            particles.add(Particle(
                x = blob.x,
                y = blob.y,
                velocityX = vx,
                velocityY = vy,
                color = blob.color,
                life = 1.0f
            ))
        }

        // Remove blob
        blobs.remove(blob)

        // Play sound
        playPopSound()

        // Notify
        (context as? MainActivity)?.onBlobExploded(blob.name)

        invalidate()
    }

    private fun playPopSound() {
        popSound?.let {
            if (!it.isPlaying) {
                it.start()
            }
        }
    }

    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.x += particle.velocityX
            particle.y += particle.velocityY
            particle.velocityY += 0.5f // gravity
            particle.life -= 0.02f

            if (particle.life <= 0) {
                iterator.remove()
            }
        }
    }

    private fun updatePhysics() {
        blobs.forEach { blob ->
            if (blob.isBeingDragged) {
                // Being dragged - no physics
                return@forEach
            }

            if (!blob.isLanded) {
                blob.velocityY += gravity
                blob.x += blob.velocityX
                blob.y += blob.velocityY

                // Wall collisions
                if (blob.x - blob.radius <= 0) {
                    blob.x = blob.radius
                    blob.velocityX *= -0.8f
                }
                if (blob.x + blob.radius >= width) {
                    blob.x = width - blob.radius
                    blob.velocityX *= -0.8f
                }

                // Ground collision
                if (blob.y + blob.radius >= groundY) {
                    blob.y = groundY - blob.radius
                    blob.velocityY *= -bounceDamping
                    blob.velocityX *= 0.95f // friction

                    if (kotlin.math.abs(blob.velocityY) < 2.0f && kotlin.math.abs(blob.velocityX) < 1.0f) {
                        blob.isLanded = true
                        blob.velocityY = 0f
                        blob.velocityX = if (Random.nextBoolean()) runSpeed else -runSpeed
                    }
                }

                // Ceiling collision
                if (blob.y - blob.radius < 0) {
                    blob.y = blob.radius
                    blob.velocityY *= -0.5f
                }
            } else {
                // Running on ground
                blob.x += blob.velocityX

                if (blob.x - blob.radius <= 0 || blob.x + blob.radius >= width) {
                    blob.velocityX *= -1
                    blob.x = blob.x.coerceIn(blob.radius, width - blob.radius)
                }

                val wobble = kotlin.math.sin(System.currentTimeMillis() / 200.0).toFloat() * 3f
                blob.y = (groundY - blob.radius) + wobble
            }
        }
    }

    fun addBlob(name: String) {
        if (width <= 0 || height <= 0) {
            postDelayed({ addBlob(name) }, 100)
            return
        }
        val blob = Blob.createRandom(name, width.toFloat())
        blobs.add(blob)
        invalidate()
    }

    fun clearBlobs() {
        blobs.clear()
        particles.clear()
        invalidate()
    }

    fun getBlobNames(): List<String> = blobs.map { it.name }
    fun getBlobCount(): Int = blobs.size

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Stars
        stars.forEach { (x, y) ->
            canvas.drawCircle(x % width, y % height, 2f, starPaint)
        }

        // Ground
        canvas.drawRect(0f, groundY, width.toFloat(), height.toFloat(), groundPaint)

        // Ground line
        val linePaint = Paint().apply {
            color = Color.parseColor("#0F3460")
            strokeWidth = 5f
        }
        canvas.drawLine(0f, groundY, width.toFloat(), groundY, linePaint)

        // Blobs
        blobs.forEach { blob ->
            drawBlob(canvas, blob)
        }

        // Particles (explosion effects)
        particles.forEach { particle ->
            val particlePaint = Paint().apply {
                color = particle.color
                alpha = (255 * particle.life).toInt()
            }
            canvas.drawCircle(particle.x, particle.y, 8f * particle.life, particlePaint)
        }

        // Instructions
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            alpha = 150
        }
        canvas.drawText("DRAG to throw • SPAM CLICK (5x) to explode", 50f, height - 50f, textPaint)
    }

    private fun drawBlob(canvas: Canvas, blob: Blob) {
        val squish = if (blob.isLanded) 0.9f else 1.0f

        // Selection ring if being dragged
        if (blob.isBeingDragged) {
            canvas.drawCircle(blob.x, blob.y, blob.radius + 10f, selectionPaint)
        }

        canvas.save()
        canvas.translate(blob.x, blob.y)
        canvas.scale(1f, squish)

        // Body
        canvas.drawCircle(0f, 0f, blob.radius, blob.paint)

        // Shine
        val shinePaint = Paint().apply {
            color = Color.WHITE
            alpha = 100
            style = Paint.Style.FILL
        }
        canvas.drawCircle(-blob.radius * 0.3f, -blob.radius * 0.3f, blob.radius * 0.2f, shinePaint)

        canvas.restore()

        // Name
        canvas.drawText(
            blob.name,
            blob.x,
            blob.y + blob.radius + 35f,
            blob.textPaint
        )

        // Click counter for explosion hint
        if (lastClickedBlob == blob && clickCount > 0) {
            val counterPaint = Paint().apply {
                color = Color.RED
                textSize = 40f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "$clickCount/5",
                blob.x,
                blob.y - blob.radius - 20f,
                counterPaint
            )
        }

        // Shadow
        if (blob.isLanded && !blob.isBeingDragged) {
            val shadowPaint = Paint().apply {
                color = Color.BLACK
                alpha = 50
            }
            canvas.drawOval(
                blob.x - blob.radius,
                groundY - 10f,
                blob.x + blob.radius,
                groundY + 5f,
                shadowPaint
            )
        }
    }
}

// Particle class for explosion effect
data class Particle(
    var x: Float,
    var y: Float,
    var velocityX: Float,
    var velocityY: Float,
    val color: Int,
    var life: Float
)