package com.example.financeapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R // Assume R is imported for resource IDs
import com.example.financeapp.data.model.LeakageBucketNetwork

/**
 * Adapter for displaying a list of LeakageBucketNetwork items in a RecyclerView.
 */
class LeakageBucketAdapter(private val buckets: List<LeakageBucketNetwork>) :
    RecyclerView.Adapter<LeakageBucketAdapter.LeakageBucketViewHolder>() {

    class LeakageBucketViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // FIX: Update references to match likely XML IDs (snake_case)
        val tvInsight: TextView = view.findViewById(R.id.tv_insight)
        val tvLeakAmount: TextView = view.findViewById(R.id.tv_leak_amount)

        fun bind(bucket: LeakageBucketNetwork) {
            // Display the bucket name/insight description
            tvInsight.text = bucket.name 
            
            // Display the total leakage amount, formatted for currency
            tvLeakAmount.text = String.format("%,.2f", bucket.totalLeakageAmount)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeakageBucketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_leakage_bucket, parent, false)
        return LeakageBucketViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeakageBucketViewHolder, position: Int) {
        holder.bind(buckets[position])
    }

    override fun getItemCount() = buckets.size
}
