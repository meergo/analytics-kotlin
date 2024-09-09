package com.meergo.analytics.kotlin.core.compat

import com.meergo.analytics.kotlin.core.Configuration
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

import org.junit.jupiter.api.Test

internal class ConfigurationBuilderTest {

    lateinit var builder: ConfigurationBuilder

    private val writeKey = "123"

    @BeforeEach
    internal fun setUp() {
        builder = ConfigurationBuilder(writeKey)
    }

    @Test
    fun setApplication() {
        val config = builder.setApplication(this).build()

        assertEquals(this, config.application)
    }

    @Test
    fun setCollectDeviceId() {
        val expected = true
        val config = builder.setCollectDeviceId(expected).build()

        assertEquals(expected, config.collectDeviceId)
    }

    @Test
    fun setTrackApplicationLifecycleEvents() {
        val expected = true
        val config = builder.setTrackApplicationLifecycleEvents(expected).build()

        assertEquals(expected, config.trackApplicationLifecycleEvents)
    }

    @Test
    fun setUseLifecycleObserver() {
        val expected = true
        val config = builder.setUseLifecycleObserver(expected).build()

        assertEquals(expected, config.useLifecycleObserver)
    }

    @Test
    fun setTrackDeepLinks() {
        val expected = true
        val config = builder.setTrackDeepLinks(expected).build()

        assertEquals(expected, config.trackDeepLinks)
    }

    @Test
    fun setFlushAt() {
        val expected = 100
        val config = builder.setFlushAt(expected).build()

        assertEquals(expected, config.flushAt)
    }

    @Test
    fun setFlushInterval() {
        val expected = 200
        val config = builder.setFlushInterval(expected).build()

        assertEquals(expected, config.flushInterval)
    }

    @Test
    fun setAutoAddMeergoDestination() {
        val expected = false
        val config = builder.setAutoAddMeergoDestination(expected).build()

        assertEquals(expected, config.autoAddMeergoDestination)
    }

    @Test
    fun setAutoAddSegmentDestination() {
        val expected = false
        val config = builder.setAutoAddSegmentDestination(expected).build()

        assertEquals(expected, config.autoAddSegmentDestination)
    }

    @Test
    fun setEndpoint() {
        val expected = "test"
        val config = builder.setEndpoint(expected).build()

        assertEquals(expected, config.endpoint)
    }

    @Test
    fun setSessionAutoTrack() {
        val expected = false
        val config = builder.setSessionAutoTrack(expected).build()

        assertEquals(expected, config.sessionAutoTrack)
    }

    @Test
    fun setSessionTimeout() {
        val expected: Long = 5 * 60000
        val config = builder.setSessionTimeout(expected).build()

        assertEquals(expected, config.sessionTimeout)
    }

    @Test
    fun build() {
        val expected = Configuration(
            writeKey = writeKey,
            application = this,
            collectDeviceId = true,
            trackApplicationLifecycleEvents = true,
            useLifecycleObserver = true,
            trackDeepLinks = true,
            flushAt = 100,
            flushInterval = 200,
            flushPolicies = emptyList(),
            autoAddMeergoDestination = false,
            autoAddSegmentDestination = false,
            endpoint = "test"
        )

        val config = builder.setApplication(expected.application)
            .setCollectDeviceId(expected.collectDeviceId)
            .setTrackApplicationLifecycleEvents(expected.trackApplicationLifecycleEvents)
            .setUseLifecycleObserver(expected.useLifecycleObserver)
            .setTrackDeepLinks(expected.trackDeepLinks)
            .setFlushAt(expected.flushAt)
            .setFlushInterval(expected.flushInterval)
            .setAutoAddMeergoDestination(expected.autoAddMeergoDestination)
            .setAutoAddSegmentDestination(expected.autoAddSegmentDestination)
            .setEndpoint(expected.endpoint)
            .setRequestFactory(expected.requestFactory)
            .setSessionAutoTrack(expected.sessionAutoTrack)
            .setSessionTimeout(expected.sessionTimeout)
            .build()

        assertEquals(expected, config)
    }
}