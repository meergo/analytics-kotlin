package com.meergo.analytics.kotlin.android

import com.meergo.analytics.kotlin.core.Telemetry
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class AndroidAnalyticsKtTest {
    init {
        Telemetry.enable = false
    }

    @Test
    fun `jvm initializer in android platform should failed`() {
        val exception =  assertThrows<Exception> {
            com.meergo.analytics.kotlin.core.Analytics("123") {
                application = "Test"
            }
        }

        assertEquals(exception.message?.contains("Android"), true)
    }
}