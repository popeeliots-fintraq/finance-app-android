package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.data.model.LeakageBucketNetwork
import com.example.financeapp.databinding.ItemLeakBucketBinding
import com.example.financeapp.ui.viewmodel.LeakBucketUiModel

/**
 * ListAdapter implementation for the Leakage Buckets list.
 */
class LeakageBucketAdapter(
    private val clickListener: (LeakageBucketNetwork) -> Unit
) : ListAdapter<LeakageBucketNetwork, LeakageBucketAdapter.LeakageBucketViewHolder>(LeakageBucketDiffCallback()) {

    // Mapper function to convert Network Model to UI Model
    private fun LeakageBucketNetwork.toUiModel(): LeakBucketUiModel {
        return LeakBucketUiModel(
            bucketName = this.bucketName,
            leakageAmount = this.totalLeakageAmount,
            insightSummary = this.insight ?: "Tap to see more details."
        )
    }

    // View Holder using Data Binding
    class LeakageBucketViewHolder(
        private val binding: ItemLeakBucketBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(uiModel: LeakBucketUiModel, clickListener: (LeakageBucketNetwork) -> Unit, networkModel: LeakageBucketNetwork) {
            // Set data directly to the Data Binding variable in the layout
            binding.bucket = uiModel
            binding.executePendingBindings() // Crucial for Data Binding to run immediately

            // Set the click listener, passing the original network model for handling
            binding.root.setOnClickListener {
                clickListener(networkModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        // Inflate the layout using Data Binding's inflate method
        val binding = ItemLeakBucketBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LeakageBucketViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LeakageBucketViewHolder, position: Int) {
        val networkModel = getItem(position)
        val uiModel = networkModel.toUiModel()
        holder.bind(uiModel, clickListener, networkModel)
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
