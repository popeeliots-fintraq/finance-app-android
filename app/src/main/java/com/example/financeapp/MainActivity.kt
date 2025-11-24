package com.example.financeapp

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.databinding.ActivityMainBinding
import com.example.financeapp.ui.adapter.LeakageBucketAdapter
import com.example.financeapp.ui.viewmodel.LeakageViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: LeakageViewModel by viewModels()

    // Initialize the adapter (using the optimized ListAdapter version)
    private val leakageAdapter = LeakageBucketAdapter { bucket ->
        // TODO: Implement navigation to the "Guided Execution" flow (Phase 2)
        Log.d("MainActivity", "Leak Bucket Clicked: ${bucket.bucketName}. Launching guided execution...")
        showToast("Starting Guided Execution for: ${bucket.bucketName}") // Use a simple toast for now
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- ViewBinding Setup ---
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // Fixes Unresolved reference 'rvLeakageBuckets' and related errors
        binding.rvLeakageBuckets.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = leakageAdapter
            // Disable nested scrolling to ensure smooth scrolling when this view is inside a parent scroll view
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            // Use repeatOnLifecycle to safely collect flow data only when the activity is started
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    
                    // 1. Update Loading and Error State
                    // TODO: Implement proper visibility toggles for loading/error views
                    if (state.errorMessage != null) {
                        Log.e("MainActivity", "API Error: ${state.errorMessage}")
                        showToast(state.errorMessage)
                    }

                    // 2. Update Projection Card Data (Fixes Unresolved reference 'tvCurrentLeakage' and 'tvProjectedSalary')
                    updateProjectionCard(state.currentLeakageAmount, state.reclaimedSalaryProjection)

                    // 3. Update Leakage Buckets List
                    leakageAdapter.submitList(state.leakageBuckets)
                    
                    // 4. Update Autopilot Status Text
                    binding.tvAutopilotStatus.text = state.autopilotStatusText
                }
            }
        }
    }

    /**
     * Helper function to format and display the salary projection data.
     */
    private fun updateProjectionCard(currentLeakage: Double, projectedSalary: Double) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        // Assuming tvCurrentLeakage and tvProjectedSalary are in activity_main.xml (ViewBinding resolved this)
        binding.tvCurrentLeakage.text = formatter.format(currentLeakage)
        binding.tvProjectedSalary.text = formatter.format(projectedSalary)
    }

    /**
     * Placeholder for a proper UI messaging system (e.g., Snackbar or custom Dialog).
     */
    private fun showToast(message: String) {
        // Replace with Toast.makeText(this, message, Toast.LENGTH_LONG).show() if needed
        Log.i("UI Message", message) 
    }
}
