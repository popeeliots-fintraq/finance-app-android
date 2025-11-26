package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.databinding.ItemLeakBucketBinding // <-- Critical: Fixes ItemLeakBucketBinding
import com.example.financeapp.ui.model.LeakageBucket // <-- Critical: Fixes LeakageBucket
import java.text.NumberFormat
import java.util.Locale

/**
 * RecyclerView Adapter for displaying Leakage Buckets using the ListAdapter pattern for efficiency.
 */
class LeakageBucketAdapter(
    private val onClick: (LeakageBucket) -> Unit
) : ListAdapter<LeakageBucket, LeakageBucketAdapter.LeakageBucketViewHolder>(LeakageBucketDiffCallback()) {

    // Inner class for the ViewHolder
    inner class LeakageBucketViewHolder(
        private val binding: ItemLeakBucketBinding // Uses the generated binding class
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bucket: LeakageBucket) {
            // Set data to the views
            binding.tvBucketName.text = bucket.bucketName
            binding.tvDescription.text = bucket.description
            
            // Format amounts using Indian Locale as context suggests
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvCurrentAmount.text = formatter.format(bucket.currentAmount)
            binding.tvTargetAmount.text = formatter.format(bucket.targetAmount)
            
            // Set click listener
            binding.root.setOnClickListener {
                onClick(bucket)
            }
        }

        companion object {
            fun from(parent: ViewGroup): LeakageBucketViewHolder {
                // Use the generated Binding class to inflate the layout
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLeakBucketBinding.inflate(layoutInflater, parent, false)
                return LeakageBucketViewHolder(binding)
            }
        }
    }

    // Override methods from ListAdapter
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        return LeakageBucketViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LeakageBucketViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    // DiffUtil implementation for efficient list updates
    private class LeakageBucketDiffCallback : DiffUtil.ItemCallback<LeakageBucket>() {
        override fun areItemsTheSame(oldItem: LeakageBucket, newItem: LeakageBucket): Boolean {
            // Assuming bucketName is unique identifier for the UI model
            return oldItem.bucketName == newItem.bucketName
        }

        override fun areContentsTheSame(oldItem: LeakageBucket, newItem: LeakageBucket): Boolean {
            return oldItem == newItem
        }
    }
}
