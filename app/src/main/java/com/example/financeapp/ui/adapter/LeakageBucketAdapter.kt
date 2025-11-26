package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.databinding.ItemLeakBucketBinding
import com.example.financeapp.ui.model.LeakBucketUiModel
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter for displaying Leak Bucket items using ListAdapter and DiffUtil for efficient updates.
 *
 * NOTE: This adapter assumes you have created the layout file:
 * 'app/src/main/res/layout/item_leak_bucket.xml'
 */
class LeakageBucketAdapter(
    private val onClick: (LeakBucketUiModel) -> Unit // Click handler for "Guided Execution"
) : ListAdapter<LeakBucketUiModel, LeakageBucketAdapter.LeakageBucketViewHolder>(LeakBucketDiffCallback()) {

    // Currency formatter initialized once for performance
    // Adjust Locale based on the target currency (e.g., "en", "IN" for Indian Rupee)
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    // The ListAdapter handles 'submitList' automatically and uses DiffUtil internally.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        val binding = ItemLeakBucketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeakageBucketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeakageBucketViewHolder, position: Int) {
        val bucket = getItem(position) // Get item from the internal list managed by ListAdapter
        holder.bind(bucket, currencyFormatter, onClick)
    }

    /**
     * ViewHolder for the LeakBucket item.
     */
    class LeakageBucketViewHolder(private val binding: ItemLeakBucketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            bucket: LeakBucketUiModel,
            formatter: NumberFormat,
            onClick: (LeakBucketUiModel) -> Unit
        ) {
            // 1. Bind Bucket Name
            binding.tvBucketName.text = bucket.bucketName

            // 2. Bind Leakage Amount, formatted as currency
            binding.tvLeakAmount.text = formatter.format(bucket.leakageAmount)

            // 3. Set Insight text
            // This summary can be dynamically updated when the Insight Card text is available from the API.
            binding.tvInsightSummary.text = "Tap for insight and next action."

            // 4. Set click listener for guided execution (The core Fin-Traq feature)
            binding.root.setOnClickListener { onClick(bucket) }
        }
    }

    /**
     * DiffUtil.ItemCallback to calculate the difference between two non-null items in a list.
     * This ensures only changed items are re-bound, improving performance.
     */
    private class LeakBucketDiffCallback : DiffUtil.ItemCallback<LeakBucketUiModel>() {
        override fun areItemsTheSame(oldItem: LeakBucketUiModel, newItem: LeakBucketUiModel): Boolean {
            // Check if the unique identifier (e.g., bucket ID) is the same.
            // Assuming LeakBucketUiModel has an 'id' or 'bucketName' that uniquely identifies it.
            return oldItem.bucketName == newItem.bucketName
        }

        override fun areContentsTheSame(oldItem: LeakBucketUiModel, newItem: LeakBucketUiModel): Boolean {
            // Check if data contents are the same. Data classes often have a good default equals implementation.
            return oldItem == newItem
        }
    }
}
