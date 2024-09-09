package com.meergo.analytics.kotlin.core

import com.meergo.analytics.kotlin.core.Constants.DEFAULT_ENDPOINT
import com.meergo.analytics.kotlin.core.platform.policies.FlushPolicy
import com.meergo.analytics.kotlin.core.utilities.ConcreteStorageProvider
import kotlinx.coroutines.*
import sovran.kotlin.Store

/**
 * Configuration that analytics can use
 * @property writeKey the Meergo writeKey
 * @property application defaults to `null`
 * @property storageProvider Provider for storage class, defaults to `ConcreteStorageProvider`
 * @property collectDeviceId collect deviceId, defaults to `false`
 * @property trackApplicationLifecycleEvents automatically send track for Lifecycle events (eg: Application Opened, Application Backgrounded, etc.), defaults to `false`
 * @property useLifecycleObserver enables the use of LifecycleObserver to track Application lifecycle events. Defaults to `false`.
 * @property trackDeepLinks automatically track [Deep link][https://developer.android.com/training/app-links/deep-linking] opened based on intents, defaults to `false`
 * @property flushAt count of events at which we flush events, defaults to `20`
 * @property flushInterval interval in seconds at which we flush events, defaults to `30 seconds`
 * @property defaultSettings Settings object that will be used as fallback in case of network failure, defaults to empty
 * @property autoAddMeergoDestination automatically add MeergoDestination plugin, defaults to `true`
 * @property autoAddSegmentDestination same as autoAddMeergoDestination. defaults to `true`. This remains for compatibility with Segment SDK.
 * @property endpoint set the base endpoint used to construct the event dispatch and settings retrieval endpoints. Defaults to `test.example.com/api/v1`.
 * @property sessionAutoTrack automatically track session, defaults to `true`
 * @property sessionTimeout interval in milliseconds at which the session expires, defaults to `5 * 60000` (5 minutes)
 */
data class Configuration(
    val writeKey: String,
    var application: Any? = null,
    val storageProvider: StorageProvider = ConcreteStorageProvider,
    var collectDeviceId: Boolean = false,
    var trackApplicationLifecycleEvents: Boolean = false,
    var useLifecycleObserver: Boolean = false,
    var trackDeepLinks: Boolean = false,
    var flushAt: Int = 20,
    var flushInterval: Int = 30,
    var flushPolicies: List<FlushPolicy> = emptyList<FlushPolicy>(),
    var defaultSettings: Settings = Settings(),
    var autoAddMeergoDestination: Boolean = true,
    var autoAddSegmentDestination: Boolean = true,
    var endpoint: String = DEFAULT_ENDPOINT,
    var requestFactory: RequestFactory = RequestFactory(),
    var errorHandler: ErrorHandler? = null,
    var sessionAutoTrack: Boolean = true,
    var sessionTimeout: Long = 5 * 60000, // 5 minutes.
) {
    fun isValid(): Boolean {
        return writeKey.isNotBlank() && application != null
    }
}

interface CoroutineConfiguration {
    val store: Store

    val analyticsScope: CoroutineScope

    val analyticsDispatcher: CoroutineDispatcher

    val networkIODispatcher: CoroutineDispatcher

    val fileIODispatcher: CoroutineDispatcher
}

typealias ErrorHandler = (Throwable) -> Unit