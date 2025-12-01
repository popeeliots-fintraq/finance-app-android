package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.databinding.ItemLeakBucketBinding
import com.example.financeapp.ui.viewmodel.LeakBucketUiModel
import java.text.NumberFormat
import java.util.Locale

class LeakageBucketAdapter(
    private val onClick: (LeakBucketUiModel) -> Unit
) : ListAdapter<LeakBucketUiModel, LeakageBucketAdapter.LeakageBucketViewHolder>(
    LeakageBucketDiffCallback()
) {

    class LeakageBucketViewHolder(
        private val binding: ItemLeakBucketBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            bucket: LeakBucketUiModel,
            onClick: (LeakBucketUiModel) -> Unit
        ) {
            binding.tvBucketName.text = bucket.bucketName
            binding.tvInsight.text = bucket.insightSummary

            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            binding.tvLeakAmount.text = formatter.format(bucket.leakageAmount)

            binding.root.setOnClickListener {
                onClick(bucket)
            }
        }

        companion object {
            fun from(parent: ViewGroup): LeakageBucketViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemLeakBucketBinding.inflate(layoutInflater, parent, false)
                return LeakageBucketViewHolder(binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        return LeakageBucketViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LeakageBucketViewHolder, position: Int) {
        holder.bind(getItem(position), onClick)
    }

    private class LeakageBucketDiffCallback : DiffUtil.ItemCallback<LeakBucketUiModel>() {
        override fun areItemsTheSame(oldItem: LeakBucketUiModel, newItem: LeakBucketUiModel): Boolean {
            return oldItem.bucketName == newItem.bucketName
        }

        override fun areContentsTheSame(oldItem: LeakBucketUiModel, newItem: LeakBucketUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
