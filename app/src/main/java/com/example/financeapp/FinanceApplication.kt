package com.example.financeapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// This is the entry point for the Hilt dependency injection container.
@HiltAndroidApp
class FinanceApplication : Application() {
    // No code needed inside the class body for a basic setup.
}
