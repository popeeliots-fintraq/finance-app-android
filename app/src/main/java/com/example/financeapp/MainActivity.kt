package com.example.financeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.databinding.ActivityMainBinding
// *** IMPORTANT: New V2 Imports ***
import com.example.financeapp.ui.adapter.LeakageBucketAdapter
import com.example.financeapp.ui.viewmodel.LeakageViewModel // Use the new ViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// NOTE: Ensure your build.gradle has viewBinding { enabled = true } and the
// necessary dependencies for the 'by viewModels()' delegate (activity-ktx).

class MainActivity : AppCompatActivity() {

    // 1. Initialize ViewBinding
    private lateinit var binding: ActivityMainBinding

    // 2. Initialize the V2 LeakageViewModel using the recommended delegate
    // NOTE: This assumes Hilt or a ViewModelFactory is configured to inject ApiService
    private val viewModel: LeakageViewModel by viewModels()

    // 3. Initialize the Adapter for the Leakage Bucket List
    private lateinit var leakageAdapter: LeakageBucketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // --- Setup RecyclerView ---
        setupRecyclerView()
        
        // --- Start Observing Data ---
        observeLeakageUiState()
        
        // The old logStatus() call and TextView find are no longer needed
        // The UI elements now automatically reflect the ViewModel state
    }

    private fun setupRecyclerView() {
        // Define the click action for a bucket (part of the Guided Execution flow)
        leakageAdapter = LeakageBucketAdapter { bucket ->
            // TODO: Implement logic to show Leak Detail/Insight card for 'Guided execution' [cite: 2025-10-15]
            Toast.makeText(this, "Tapped on leak: ${bucket.bucketName}", Toast.LENGTH_SHORT).show()
        }
        
        binding.rvLeakageBuckets.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = leakageAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeLeakageUiState() {
        // Observe the ViewModel's stateflow using the lifecycleScope
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                
                // --- Handle Loading and Error States ---
                if (state.isLoading) {
                    // Optionally show a progress spinner or loading text
                    binding.tvCurrentLeakage.text = "Loading..." 
                }
                if (state.errorMessage != null) {
                    Toast.makeText(this@MainActivity, state.errorMessage, Toast.LENGTH_LONG).show()
                    // Set error on the main view
                    binding.tvCurrentLeakage.text = "Data Error."
                }
                
                // --- Bind Projection Card Data ---
                if (!state.isLoading && state.errorMessage == null) {
                    
                    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")) // Customize locale
                    
                    // Bind Current Leakage Amount
                    binding.projectionCard.tvCurrentLeakage.text = 
                        formatter.format(state.currentLeakageAmount)
                    
                    // Bind Projected New Salary (The "If Leak Fixed → New Salary" value)
                    binding.projectionCard.tvProjectedSalary.text = 
                        formatter.format(state.reclaimedSalaryProjection)

                    // --- Bind Leakage Bucket List Data ---
                    // This completes the 'Leakage Bucket View' implementation [cite: 2025-10-15]
                    leakageAdapter.submitList(state.leakageBuckets)
                }
            }
        }
    }
}
