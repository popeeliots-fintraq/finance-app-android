package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.data.model.LeakageBucket
import com.example.financeapp.databinding.ItemLeakBucketBinding // Import for DataBinding class
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter for displaying LeakageBuckets in a RecyclerView.
 * Uses ListAdapter for efficient item updates.
 *
 * @param onBucketClicked Lambda function executed when a bucket item is clicked.
 */
class LeakageBucketAdapter(
    private val onBucketClicked: (LeakageBucket) -> Unit
) : ListAdapter<LeakageBucket, LeakageBucketAdapter.LeakageBucketViewHolder>(LeakageBucketDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        // Inflate the binding layout for the item view
        val binding = ItemLeakBucketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeakageBucketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeakageBucketViewHolder, position: Int) {
        val bucket = getItem(position)
        holder.bind(bucket, onBucketClicked)
    }

    /**
     * ViewHolder class that holds the views and binds the data.
     */
    class LeakageBucketViewHolder(private val binding: ItemLeakBucketBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(bucket: LeakageBucket, onBucketClicked: (LeakageBucket) -> Unit) {
            // Set data for the binding variables in the layout
            binding.leakageBucket = bucket

            // Format the amount for display
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvBucketAmount.text = formatter.format(bucket.currentLeakageAmount)

            // Set the click listener for the entire item view
            binding.root.setOnClickListener {
                onBucketClicked(bucket)
            }
            
            // This ensures the data is immediately bound to the views
            binding.executePendingBindings()
        }
    }

    /**
     * DiffCallback implementation for calculating the difference between two lists.
     */
    private class LeakageBucketDiffCallback : DiffUtil.ItemCallback<LeakageBucket>() {
        override fun areItemsTheSame(oldItem: LeakageBucket, newItem: LeakageBucket): Boolean {
            // LeakageBucket name is used as the unique identifier
            return oldItem.bucketName == newItem.bucketName
        }

        override fun areContentsTheSame(oldItem: LeakageBucket, newItem: LeakageBucket): Boolean {
            // Checks if all data fields are the same
            return oldItem == newItem
        }
    }
}
