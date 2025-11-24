package com.example.financeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels 
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
// *** ViewBinding Class Generated from activity_main.xml ***
import com.example.financeapp.databinding.ActivityMainBinding 
// *** New V2 Imports ***
import com.example.financeapp.ui.adapter.LeakageBucketAdapter 
import com.example.financeapp.ui.viewmodel.LeakageViewModel 
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // 1. Initialize ViewBinding
    private lateinit var binding: ActivityMainBinding

    // 2. Initialize the V2 LeakageViewModel 
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
    }

    private fun setupRecyclerView() {
        // Define the click action for a bucket (part of the Guided Execution flow)
        leakageAdapter = LeakageBucketAdapter { bucket ->
            // TODO: Implement logic to show Leak Detail/Insight card for 'Guided execution'
            Toast.makeText(this, "Tapped on leak: ${bucket.bucketName}", Toast.LENGTH_SHORT).show()
        }
        
        // Access views using the binding object
        // The ViewBinding fix in build.gradle should resolve rvLeakageBuckets, adapter, and layoutManager now.
        binding.rvLeakageBuckets.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = leakageAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeLeakageUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                
                // --- Handle Loading and Error States ---
                if (state.isLoading) {
                    // Accessing TextViews directly from the binding object
                    binding.tvCurrentLeakage.text = "Loading..." 
                }
                if (state.errorMessage != null) {
                    Toast.makeText(this@MainActivity, state.errorMessage, Toast.LENGTH_LONG).show()
                    binding.tvCurrentLeakage.text = "Data Error."
                }
                
                // --- Bind Projection Card Data ---
                if (!state.isLoading && state.errorMessage == null) {
                    
                    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")) // Customize locale
                    
                    // Accessing TextViews directly from the binding object
                    binding.tvCurrentLeakage.text = formatter.format(state.currentLeakageAmount)
                    binding.tvProjectedSalary.text = formatter.format(state.reclaimedSalaryProjection)

                    // --- Bind Leakage Bucket List Data ---
                    leakageAdapter.submitList(state.leakageBuckets)
                }
            }
        }
    }
}
