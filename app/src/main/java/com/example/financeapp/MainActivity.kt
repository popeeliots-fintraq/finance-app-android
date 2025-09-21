package com.example.financeapp  // Use your actual package name here

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)  // Reference to the layout XML

        // Find the TextView by its ID and set text dynamically
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "Welcome to Finance App!"  // Update text dynamically
    }
}
