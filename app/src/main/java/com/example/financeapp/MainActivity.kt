package com.example.financeapp 

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.ui.viewmodel.CategorizationViewModel // Keep this import

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    
    // Instantiate your new ViewModel
    private val categorizationViewModel = CategorizationViewModel() // Keep this line
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "fin-traq The Income Optimizer" // fin-traq's vision [cite: 2025-10-15]
        
        // --- NEW STATUS BLOCK ---
        
        Log.d(TAG, "Fin-Traq Main Activity Initialized.")
        
        // We call a clean function on the ViewModel to confirm it's working
        categorizationViewModel.logStatus() 
        
        // Update the TextView to reflect the new architecture focus
        textView.text = "Fin-Traq V2: Salary Autopilot Running in Worker."
    }
}
