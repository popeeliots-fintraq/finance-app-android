package com.example.financeapp.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.NumberFormat
import java.util.Locale

/**
 * Utility class containing custom Binding Adapters for data formatting.
 * Using a companion object ensures the functions are compiled as static methods,
 * which is required by the Data Binding library.
 */
class BindingAdapters {
    companion object {
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
            // Uses US locale for standard currency representation ($X.XX)
            val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
            textView.text = currencyFormatter.format(value)
        }
    }
}
