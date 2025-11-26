package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.databinding.ItemLeakBucketBinding
import com.example.financeapp.ui.model.LeakageBucket
import java.text.NumberFormat
import java.util.Locale

/**
 * RecyclerView Adapter for displaying Leakage Buckets using the ListAdapter pattern for efficiency.
 * Nested classes are defined without the 'inner' keyword to be static in the Java context,
 * which resolves Kapt/DataBinding compilation issues.
 */
class LeakageBucketAdapter(
    private val onClick: (LeakageBucket) -> Unit
) : ListAdapter<LeakageBucket, LeakageBucketAdapter.LeakageBucketViewHolder>(LeakageBucketDiffCallback()) {

    // Removed 'inner' keyword. This class is now static in the Java context.
    class LeakageBucketViewHolder(
        private val binding: ItemLeakBucketBinding 
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bucket: LeakageBucket, onClick: (LeakageBucket) -> Unit) {
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
        // Pass the click lambda to the bind function since the ViewHolder is no longer 'inner'
        holder.bind(item, onClick) 
    }

    // DiffUtil implementation - Also defined as a regular nested class (implicitly static)
    private class LeakageBucketDiffCallback : DiffUtil.ItemCallback<LeakageBucket>() {
        override fun areItemsTheSame(oldItem: LeakageBucket, newItem: LeakageBucket): Boolean {
            return oldItem.bucketName == newItem.bucketName
        }

        override fun areContentsTheSame(oldItem: LeakageBucket, newItem: LeakageBucket): Boolean {
            return oldItem == newItem
        }
    }
}
