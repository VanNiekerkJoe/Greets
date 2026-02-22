package com.example.greets

import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val TAG = "GreetsGame"
    private lateinit var etName: EditText
    private lateinit var btnDropBlob: Button
    private lateinit var btnClearBlobs: Button
    private lateinit var btnGreetAll: Button
    private lateinit var tvGreeting: TextView
    private lateinit var blobGameView: BlobGameView
    private lateinit var textToSpeech: TextToSpeech
    private var ttsReady = false

    private val greetings = listOf(
        "Welcome, %s! 🎉",
        "Hey there, %s! 👋",
        "Hello %s! Nice to meet you! 😊",
        "Greetings, %s! 🌟",
        "Yo %s! What's up? 😎",
        "Hola %s! ¡Bienvenido! 🎈",
        "Bonjour %s! Welcome! ✨",
        "Howdy, %s! 🤠",
        "Sup %s! Ready to roll? 🚀",
        "Ahoy %s! Welcome aboard! ⚓"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textToSpeech = TextToSpeech(this, this)
        initializeViews()
        setupClickListeners()
        animateTitle()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        btnDropBlob = findViewById(R.id.btnDropBlob)
        btnClearBlobs = findViewById(R.id.btnClearBlobs)
        btnGreetAll = findViewById(R.id.btnGreetAll)
        tvGreeting = findViewById(R.id.tvGreeting)
        blobGameView = findViewById(R.id.blobGameView)
    }

    private fun setupClickListeners() {
        btnDropBlob.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
            dropBlob()
        }

        btnClearBlobs.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
            clearBlobs()
        }

        btnGreetAll.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
            greetAllBlobs()
        }
    }

    private fun animateTitle() {
        val title = findViewById<TextView>(R.id.tvTitle)
        title.startAnimation(AnimationUtils.loadAnimation(this, R.anim.bounce))
    }

    private fun dropBlob() {
        val name = etName.text.toString().trim()

        if (name.isEmpty()) {
            shakeEditText()
            Toast.makeText(this, "Please enter a name first!", Toast.LENGTH_SHORT).show()
            return
        }

        // Add blob to game
        blobGameView.addBlob(name)

        // Show greeting
        showGreeting(name)

        // Speak greeting
        speakGreeting(name)

        // Clear input
        etName.text.clear()
        etName.requestFocus()

        // Success feedback
        Toast.makeText(this, "🎉 $name joined the party!", Toast.LENGTH_SHORT).show()
    }

    private fun showGreeting(name: String) {
        val greeting = greetings.random().format(name)
        tvGreeting.text = greeting
        tvGreeting.visibility = View.VISIBLE

        // Animate greeting
        val slideIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        tvGreeting.startAnimation(slideIn)

        // Hide after 3 seconds
        tvGreeting.postDelayed({
            val fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
            tvGreeting.startAnimation(fadeOut)
            tvGreeting.visibility = View.INVISIBLE
        }, 3000)
    }

    private fun speakGreeting(name: String) {
        if (ttsReady) {
            val message = "Welcome, $name!"
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun greetAllBlobs() {
        val blobNames = blobGameView.getBlobNames()

        if (blobNames.isEmpty()) {
            Toast.makeText(this, "No blobs to greet yet! Add some first!", Toast.LENGTH_SHORT).show()
            shakeEditText()
            return
        }

        // Show group greeting
        val namesList = blobNames.joinToString(", ")
        tvGreeting.text = "👋 Hello everyone: $namesList!"
        tvGreeting.visibility = View.VISIBLE

        // Animate
        val bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)
        tvGreeting.startAnimation(bounce)

        // Speak all names
        if (ttsReady) {
            val message = "Greetings to ${blobNames.joinToString(", ")}"
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        }

        Toast.makeText(this, "🎊 Greeting ${blobNames.size} blobs!", Toast.LENGTH_SHORT).show()
    }

    private fun clearBlobs() {
        val count = blobGameView.getBlobCount()
        blobGameView.clearBlobs()
        etName.text.clear()
        etName.requestFocus()

        tvGreeting.text = "👋 Goodbye to $count friends!"
        tvGreeting.visibility = View.VISIBLE

        if (ttsReady) {
            textToSpeech.speak("Goodbye friends!", TextToSpeech.QUEUE_FLUSH, null, null)
        }

        Toast.makeText(this, "Cleared $count blobs!", Toast.LENGTH_SHORT).show()

        tvGreeting.postDelayed({
            tvGreeting.visibility = View.INVISIBLE
        }, 2000)
    }

    private fun shakeEditText() {
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        etName.startAnimation(shake)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech.setLanguage(Locale.US)
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            Log.d(TAG, "TTS initialized: $ttsReady")
        }
    }

    override fun onDestroy() {
        textToSpeech.stop()
        textToSpeech.shutdown()
        super.onDestroy()
    }
}