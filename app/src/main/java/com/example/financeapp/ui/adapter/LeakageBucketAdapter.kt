package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.data.model.LeakageBucketNetwork
import com.example.financeapp.databinding.ItemLeakBucketBinding // CHANGED: Now uses ItemLeakBucketBinding
import java.util.Locale

/**
 * ListAdapter implementation for the Leakage Buckets list.
 */
class LeakageBucketAdapter(
    private val clickListener: (LeakageBucketNetwork) -> Unit
) : ListAdapter<LeakageBucketNetwork, LeakageBucketAdapter.LeakageBucketViewHolder>(LeakageBucketDiffCallback()) {

    // View Holder using Data Binding
    class LeakageBucketViewHolder(
        private val binding: ItemLeakBucketBinding // CHANGED: Using your generated binding class
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bucket: LeakageBucketNetwork, clickListener: (LeakageBucketNetwork) -> Unit) {
            // Set data directly to the Data Binding variable in the layout
            // NOTE: Since your XML is expecting 'LeakBucketUiModel', we must manually set the TextViews
            // until the UI Model is created, or change the XML's <data> type.
            
            // For now, setting data manually using the IDs from your XML
            binding.tvBucketName.text = bucket.bucketName
            
            // Format the leakage amount as currency (matching your XML ID tvBucketAmount)
            binding.tvBucketAmount.text = String.format(Locale.US, "$ %,.2f", bucket.totalLeakageAmount)
            
            // Use the insight field for the summary (matching your XML ID tvInsightSummary)
            binding.tvInsightSummary.text = bucket.insight ?: "No specific insight available."

            // Important: executePendingBindings is needed when setting data manually
            binding.executePendingBindings() 

            // Set the click listener
            binding.root.setOnClickListener {
                clickListener(bucket)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        // Inflate the layout using Data Binding's inflate method
        val binding = ItemLeakBucketBinding.inflate( // CHANGED: Using ItemLeakBucketBinding.inflate
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeakageBucketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeakageBucketViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    private class LeakageBucketDiffCallback : DiffUtil.ItemCallback<LeakageBucketNetwork>() {
        override fun areItemsTheSame(oldItem: LeakageBucketNetwork, newItem: LeakageBucketNetwork): Boolean {
            return oldItem.bucketId == newItem.bucketId
        }

        override fun areContentsTheSame(oldItem: LeakageBucketNetwork, newItem: LeakageBucketNetwork): Boolean {
            return oldItem == newItem
        }
    }
}
