package com.meergo.analytics.kotlin.core.compat

import com.meergo.analytics.kotlin.core.Configuration
import com.meergo.analytics.kotlin.core.RequestFactory

/**
 * This class serves as a helper class for Java compatibility, which makes the
 * @see Configuration buildable through a builder pattern.
 * It's strongly discouraged to use this builder in a Kotlin based project, since
 * the optional parameters is the way to go in Kotlin.
 */
class ConfigurationBuilder (writeKey: String) {

    private val configuration: Configuration = Configuration(writeKey)

    fun setApplication(application: Any?) = apply { configuration.application = application }

    fun setCollectDeviceId(collectDeviceId: Boolean) = apply { configuration.collectDeviceId = collectDeviceId }

    fun setTrackApplicationLifecycleEvents(trackApplicationLifecycleEvents: Boolean) = apply { configuration.trackApplicationLifecycleEvents = trackApplicationLifecycleEvents }

    fun setUseLifecycleObserver(useLifecycleObserver: Boolean) = apply { configuration.useLifecycleObserver = useLifecycleObserver }

    fun setTrackDeepLinks(trackDeepLinks: Boolean) = apply { configuration.trackDeepLinks = trackDeepLinks }

    fun setFlushAt(flushAt: Int) = apply { configuration.flushAt = flushAt }

    fun setFlushInterval(flushInterval: Int) = apply { configuration.flushInterval = flushInterval }

    fun setAutoAddMeergoDestination(autoAddMeergoDestination: Boolean) = apply { configuration.autoAddMeergoDestination = autoAddMeergoDestination}

    fun setAutoAddSegmentDestination(autoAddSegmentDestination: Boolean) = apply { configuration.autoAddSegmentDestination = autoAddSegmentDestination}

    fun setEndpoint(endpoint: String) = apply { configuration.endpoint = endpoint}

    fun setRequestFactory(requestFactory: RequestFactory) = apply { configuration.requestFactory = requestFactory }

    fun setSessionAutoTrack(sessionAutoTrack: Boolean) = apply { configuration.sessionAutoTrack = sessionAutoTrack }

    fun setSessionTimeout(sessionTimeout: Long) = apply { configuration.sessionTimeout = sessionTimeout }

    fun build() = configuration
}