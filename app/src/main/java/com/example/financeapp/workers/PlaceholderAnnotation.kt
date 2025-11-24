package com.example.financeapp.workers

/**
 * Temporary annotation used to force Kapt cache invalidation due to a persistent
 * build error in the CI environment where clean is unavailable.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PlaceholderAnnotation
