package com.github.reygnn.hocschwiiz.domain.util

/**
 * Default implementation using system time.
 */
class SystemTimeProvider : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}