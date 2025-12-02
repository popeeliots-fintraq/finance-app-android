package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.data.model.LeakageBucketNetwork
import com.example.financeapp.databinding.ListItemLeakageBucketBinding // Assuming this generated binding name

/**
 * FIX: Converted to ListAdapter to support submitList() used in MainActivity.
 * Uses ViewBinding for efficient view lookup.
 */
class LeakageBucketAdapter(
    private val clickListener: (LeakageBucketNetwork) -> Unit
) : ListAdapter<LeakageBucketNetwork, LeakageBucketAdapter.LeakageBucketViewHolder>(LeakageBucketDiffCallback()) {

    // Assuming the layout file name 'list_item_leakage_bucket.xml' generates this binding class.
    class LeakageBucketViewHolder(
        private val binding: ListItemLeakageBucketBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bucket: LeakageBucketNetwork, clickListener: (LeakageBucketNetwork) -> Unit) {
            // FIX: Uses binding properties, resolving 'tv_insight' and 'tv_leak_amount' errors
            binding.tvInsight.text = bucket.bucketName // Use bucketName for the insight text
            binding.tvLeakAmount.text = String.format("%,.2f", bucket.totalLeakageAmount)
            
            // Set click listener
            binding.root.setOnClickListener { 
                clickListener(bucket)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        // FIX: Using ViewBinding to inflate the layout, resolving 'list_item_leakage_bucket' error
        val binding = ListItemLeakageBucketBinding.inflate(
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
