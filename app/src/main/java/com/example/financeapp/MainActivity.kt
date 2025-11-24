package com.example.financeapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels 
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.databinding.ActivityMainBinding 
import com.example.financeapp.ui.adapter.LeakageBucketAdapter 
import com.example.financeapp.ui.viewmodel.LeakageViewModel 
import dagger.hilt.android.AndroidEntryPoint // Hilt Import
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

// Add @AndroidEntryPoint for Hilt to inject the ViewModel
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // IMPORTANT: The by viewModels() delegate works only after @AndroidEntryPoint is added
    private val viewModel: LeakageViewModel by viewModels() 
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var leakageAdapter: LeakageBucketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) 
        
        setupRecyclerView()
        observeLeakageUiState()
    }

    private fun setupRecyclerView() {
        leakageAdapter = LeakageBucketAdapter { bucket ->
            Toast.makeText(this, "Tapped on leak: ${bucket.bucketName}", Toast.LENGTH_SHORT).show()
        }
        
        // All IDs are now accessed via the 'binding' object which Hilt compilation may help resolve
        binding.rvLeakageBuckets.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = leakageAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeLeakageUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                
                // Using the binding object to reference TextViews
                val leakageTextView = binding.tvCurrentLeakage
                val salaryTextView = binding.tvProjectedSalary
                
                if (state.isLoading) {
                    leakageTextView.text = "Loading..." 
                }
                if (state.errorMessage != null) {
                    Toast.makeText(this@MainActivity, state.errorMessage, Toast.LENGTH_LONG).show()
                    leakageTextView.text = "Data Error."
                }
                
                if (!state.isLoading && state.errorMessage == null) {
                    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
                    
                    leakageTextView.text = formatter.format(state.currentLeakageAmount)
                    salaryTextView.text = formatter.format(state.reclaimedSalaryProjection)

                    leakageAdapter.submitList(state.leakageBuckets)
                }
            }
        }
    }
}
