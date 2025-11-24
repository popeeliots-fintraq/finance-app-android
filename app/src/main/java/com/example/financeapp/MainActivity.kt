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

    private lateinit var binding: ActivityMainBinding
    private val viewModel: LeakageViewModel by viewModels()
    private lateinit var leakageAdapter: LeakageBucketAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding (ActivityMainBinding now correctly generated)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) 
        
        setupRecyclerView()
        observeLeakageUiState()
    }

    private fun setupRecyclerView() {
        leakageAdapter = LeakageBucketAdapter { bucket ->
            Toast.makeText(this, "Tapped on leak: ${bucket.bucketName}", Toast.LENGTH_SHORT).show()
        }
        
        // Accessing the RecyclerView using the binding object (rvLeakageBuckets is now resolved)
        binding.rvLeakageBuckets.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = leakageAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun observeLeakageUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                
                // Binding all elements, which should now be resolved
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
                    
                    // Direct binding to the resolved IDs
                    leakageTextView.text = formatter.format(state.currentLeakageAmount)
                    salaryTextView.text = formatter.format(state.reclaimedSalaryProjection)

                    leakageAdapter.submitList(state.leakageBuckets)
                }
            }
        }
    }
}
