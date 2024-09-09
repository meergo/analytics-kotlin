package com.meergo.analytics.kotlin.core.platform.plugins

import com.meergo.analytics.kotlin.core.Analytics
import com.meergo.analytics.kotlin.core.platform.plugins.logger.*
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean

internal class MeergoLogTest {

    @Test
    fun `can call meergoLog() `() {
        val parseLogCalled = AtomicBoolean(false)
        val testLogger = object : Logger {
            override fun parseLog(log: LogMessage) {
                if (log.message.contains("test") && log.kind == LogKind.ERROR) {
                    parseLogCalled.set(true)
                }
            }
        }
        Analytics.logger = testLogger

        Analytics.meergoLog("test")

        assertTrue(parseLogCalled.get())
    }

    @Test
    fun `can call meergoLog() with different log filter kind`() {
        val parseLogErrorCalled = AtomicBoolean(false)
        val parseLogWarnCalled = AtomicBoolean(false)
        val parseLogDebugCalled = AtomicBoolean(false)

        val testLogger = object: Logger {
            override fun parseLog(log: LogMessage) {

                if (log.message.contains("test")) {
                    when (log.kind) {
                        LogKind.ERROR -> {
                            parseLogErrorCalled.set(true)
                        }
                        LogKind.WARNING -> {
                            parseLogWarnCalled.set(true)
                        }
                        LogKind.DEBUG -> {
                            parseLogDebugCalled.set(true)
                        }
                    }
                }
            }
        }

        Analytics.logger = testLogger
        Analytics.debugLogsEnabled = true

        Analytics.meergoLog("test") // Default LogFilterKind is ERROR
        Analytics.meergoLog("test", kind = LogKind.WARNING)
        Analytics.meergoLog("test", kind = LogKind.DEBUG)

        assertTrue(parseLogErrorCalled.get())
        assertTrue(parseLogWarnCalled.get())
        assertTrue(parseLogDebugCalled.get())
    }

    @Test
    fun `debug logging respects debugLogsEnabled flag`() {

        var logSent = AtomicBoolean(false)

        val testLogger = object : Logger {
            override fun parseLog(log: LogMessage) {
                logSent.set(true)
            }
        }

        Analytics.logger = testLogger

        // Turn ON debug logs
        Analytics.debugLogsEnabled = true
        Analytics.meergoLog("test", kind = LogKind.DEBUG)

        assertTrue(logSent.get())

        // Turn OFF debug logs
        Analytics.debugLogsEnabled = false
        logSent.set(false)

        Analytics.meergoLog("test", kind = LogKind.DEBUG)
        assertFalse(logSent.get())
    }
}

