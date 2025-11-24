package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.databinding.ItemLeakBucketBinding 
import com.example.financeapp.ui.model.LeakBucketUiModel
import java.text.NumberFormat
import java.util.Locale

// NOTE: This adapter assumes you have created the layout file:
// 'app/src/main/res/layout/item_leak_bucket.xml'
class LeakageBucketAdapter(
    private val onClick: (LeakBucketUiModel) -> Unit // Click handler for "Guided Execution"
) : RecyclerView.Adapter<LeakageBucketAdapter.LeakageBucketViewHolder>() {

    private var buckets: List<LeakBucketUiModel> = emptyList()
    
    // Currency formatter initialized once
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN")) // Adjust Locale as necessary

    fun submitList(newBuckets: List<LeakBucketUiModel>) {
        // NOTE: A DiffUtil is recommended for performance, but simple list submission for MVP
        buckets = newBuckets
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        val binding = ItemLeakBucketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeakageBucketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeakageBucketViewHolder, position: Int) {
        val bucket = buckets[position]
        holder.bind(bucket, currencyFormatter, onClick)
    }

    override fun getItemCount(): Int = buckets.size

    class LeakageBucketViewHolder(private val binding: ItemLeakBucketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bucket: LeakBucketUiModel, formatter: NumberFormat, onClick: (LeakBucketUiModel) -> Unit) {
            
            // 1. Bind Bucket Name
            binding.tvBucketName.text = bucket.bucketName
            
            // 2. Bind Leakage Amount
            binding.tvLeakAmount.text = formatter.format(bucket.leakageAmount)
            
            // 3. Set Insight text (Assuming tvInsightSummary exists in item_leak_bucket.xml)
            // Currently, using a default message until API supports the full Insight Card text
            binding.tvInsightSummary.text = "Tap for insight and next action."
            
            // 4. Set click listener for guided execution
            binding.root.setOnClickListener { onClick(bucket) }
        }
    }
}
