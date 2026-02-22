package com.example.greets

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Declare UI elements
    private lateinit var etName: EditText
    private lateinit var btnDisplay: Button
    private lateinit var btnClear: Button
    private lateinit var tvGreeting: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        initializeViews()

        // Set up button click listeners
        setupClickListeners()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        btnDisplay = findViewById(R.id.btnDisplay)
        btnClear = findViewById(R.id.btnClear)
        tvGreeting = findViewById(R.id.tvGreeting)
    }

    private fun setupClickListeners() {
        // Display button - shows greeting with user's name
        btnDisplay.setOnClickListener {
            displayGreeting()
        }

        // Clear button - clears input and greeting
        btnClear.setOnClickListener {
            clearFields()
        }
    }

    private fun displayGreeting() {
        val name = etName.text.toString().trim()

        // Validate input
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        // Display personalized greeting
        val greeting = "Hello, $name! 👋\nWelcome to Greets!"
        tvGreeting.text = greeting
        tvGreeting.visibility = TextView.VISIBLE
    }

    private fun clearFields() {
        // Clear the EditText
        etName.text.clear()

        // Hide the greeting TextView
        tvGreeting.text = ""
        tvGreeting.visibility = TextView.INVISIBLE

        // Optional: Show confirmation toast
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show()

        // Return focus to EditText for next user
        etName.requestFocus()
    }
}