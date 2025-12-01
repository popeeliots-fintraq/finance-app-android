package com.example.financeapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Defines the local data structure for a single financial transaction derived from an SMS.
 * This entity will store the raw parsed data and the category determined by the backend API.
 *
 * @property id Primary key for Room, auto-generated.
 * @property timestampMillis The time the transaction occurred (or was parsed), used for sorting.
 * @property amount The monetary amount of the transaction.
 * @property type The type of transaction (e.g., "DEBIT", "CREDIT").
 * @property description The raw text extracted from the SMS describing the transaction.
 * @property category The high-level category assigned by the backend (e.g., "FOOD", "TRANSPORT", "SALARY").
 * @property isLeak A flag indicating if this transaction is currently classified as a "Leak" based on rules.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, 
    val timestampMillis: Long,
    val amount: Double,
    val type: String, // e.g., DEBIT, CREDIT
    val description: String,
    
    // Core fields for the Fin-Traq V2 logic
    val category: String, // e.g., 'UNCATEGORIZED', 'SALARY', 'GROCERIES', 'NEEDLESS_SPEND'
    val isLeak: Boolean = false // Flag for easy filtering of identified leaks
)
