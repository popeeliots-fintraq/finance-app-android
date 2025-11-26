package com.example.financeapp

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels // <-- Critical: Fixes Unresolved reference: viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.databinding.ActivityMainBinding // <-- Critical: Fixes Unresolved reference: ActivityMainBinding
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
        Log.d("MainActivity", "Leak Bucket Clicked: ${bucket.bucketName}. Launching guided execution...")
        Log.i("UI Message", "Starting Guided Execution for: ${bucket.bucketName}") 
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
        binding.rvLeakageBuckets.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = leakageAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Correctly extracts the state value to fix 'Variable expected' errors
                viewModel.uiState.collectLatest { state -> 
                    
                    if (state.errorMessage != null) {
                        Log.e("MainActivity", "API Error: ${state.errorMessage}")
                    }

                    updateProjectionCard(state.currentLeakageAmount, state.reclaimedSalaryProjection)
                    leakageAdapter.submitList(state.leakageBuckets)
                    binding.tvAutopilotStatus.text = state.autopilotStatusText
                }
            }
        }
    }

    private fun updateProjectionCard(currentLeakage: Double, projectedSalary: Double) {
        val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")) 

        binding.tvCurrentLeakage.text = formatter.format(currentLeakage)
        binding.tvProjectedSalary.text = formatter.format(projectedSalary)
    }
}
