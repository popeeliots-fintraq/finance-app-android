package com.example.financeapp.workers

/**
 * Temporary annotation used to invalidate Hilt/KSP caches in CI builds.
 * 
 * NOTE:
 * - This can be removed once the CI persistent cache issue is resolved.
 * - It does NOT impact runtime behavior.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class PlaceholderAnnotation
