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
        // Since we cannot use Toast in this environment, using a Log message for now.
        Log.i("UI Message", "Starting Guided Execution for: ${bucket.bucketName}") 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // --- ViewBinding Setup ---
        // This will resolve Unresolved reference 'ActivityMainBinding' once the redeclaration errors are gone.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // Fixes Unresolved reference 'rvLeakageBuckets' and related errors once binding is generated
        binding.rvLeakageBuckets.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = leakageAdapter
            // Disable nested scrolling to ensure smooth scrolling when this view is inside a parent scroll view
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    
                    if (state.errorMessage != null) {
                        Log.e("MainActivity", "API Error: ${state.errorMessage}")
                        Log.e("MainActivity", state.errorMessage) // Use Log for error messages
                    }

                    // These references will resolve once ActivityMainBinding is generated
                    updateProjectionCard(state.currentLeakageAmount, state.reclaimedSalaryProjection)
                    leakageAdapter.submitList(state.leakageBuckets)
                    binding.tvAutopilotStatus.text = state.autopilotStatusText
                }
            }
        }
    }

    private fun updateProjectionCard(currentLeakage: Double, projectedSalary: Double) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        // These references will resolve once ActivityMainBinding is generated
        binding.tvCurrentLeakage.text = formatter.format(currentLeakage)
        binding.tvProjectedSalary.text = formatter.format(projectedSalary)
    }
}
