package com.example.financeapp 

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.ui.viewmodel.CategorizationViewModel // Import your new ViewModel

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    
    // Instantiate your new ViewModel
    private val categorizationViewModel = CategorizationViewModel()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val textView: TextView = findViewById(R.id.textView)
        textView.text = "fin-traq The Income Optimizer"
        
        // ** NEW INTEGRATION BLOCK: Test the Cloud Run API call **
        
        Log.d(TAG, "Launching Categorization API Test...")
        
        // This launches the coroutine inside the ViewModel to hit your Cloud Run service.
        // Results will print to Logcat via the ViewModel's print statements.
        categorizationViewModel.testApiCall()
        
        // Update the TextView to show the test has been initiated.
        textView.text = "API Categorization Test Initiated. Check Logcat for SUCCESS/FAILURE."
        
        // The old 'fetchMessage' function has been removed.
    }
}
