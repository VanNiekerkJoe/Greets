package com.example.greets

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "GreetsGame"
    private lateinit var etName: EditText
    private lateinit var btnDropBlob: Button
    private lateinit var btnClearBlobs: Button
    private lateinit var blobGameView: BlobGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "onCreate called")

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etName = findViewById(R.id.etName)
        btnDropBlob = findViewById(R.id.btnDropBlob)
        btnClearBlobs = findViewById(R.id.btnClearBlobs)
        blobGameView = findViewById(R.id.blobGameView)
        Log.d(TAG, "Views initialized, gameView width: ${blobGameView.width}, height: ${blobGameView.height}")
    }

    private fun setupClickListeners() {
        btnDropBlob.setOnClickListener {
            Log.d(TAG, "Drop blob clicked")
            dropBlob()
        }

        btnClearBlobs.setOnClickListener {
            Log.d(TAG, "Clear clicked")
            blobGameView.clearBlobs()
            etName.text.clear()
            etName.requestFocus()
            Toast.makeText(this, "All blobs cleared!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dropBlob() {
        val name = etName.text.toString().trim()
        Log.d(TAG, "Name entered: '$name'")

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a name first!", Toast.LENGTH_SHORT).show()
            return
        }

        blobGameView.addBlob(name)
        etName.text.clear()
        etName.requestFocus()
        Toast.makeText(this, "Blob $name dropped!", Toast.LENGTH_SHORT).show()
    }
}