package com.github.reygnn.hocschwiiz.domain.util

/**
 * Interface for time operations, enabling testability.
 */
interface TimeProvider {
    fun nowMillis(): Long
}

