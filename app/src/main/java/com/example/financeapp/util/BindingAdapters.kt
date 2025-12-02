package com.example.financeapp.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.NumberFormat
import java.util.Locale

/**
 * Collection of custom Binding Adapters used across the application to handle data formatting.
 */
object BindingAdapters {

    /**
     * Binding Adapter for formatting a Double value as US Dollar currency text.
     * This function is automatically called by Data Binding when a Double is bound
     * to the 'android:text' attribute of a TextView.
     *
     * Usage in XML: android:text="@{bucket.leakageAmount}"
     */
    @JvmStatic
    @BindingAdapter("android:text")
    fun bindTextToCurrency(textView: TextView, value: Double?) {
        if (value == null) {
            textView.text = ""
            return
        }

        // Use NumberFormat to correctly format the Double into a currency string
        val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
        textView.text = currencyFormatter.format(value)
    }
}
