package com.meergo.analytics.kotlin.core.platform.plugins

import com.meergo.analytics.kotlin.core.*
import com.meergo.analytics.kotlin.core.platform.DestinationPlugin
import com.meergo.analytics.kotlin.core.platform.EventPipeline
import com.meergo.analytics.kotlin.core.platform.Plugin
import com.meergo.analytics.kotlin.core.platform.VersionedPlugin
import com.meergo.analytics.kotlin.core.platform.policies.CountBasedFlushPolicy
import com.meergo.analytics.kotlin.core.platform.policies.FlushPolicy
import com.meergo.analytics.kotlin.core.platform.policies.FrequencyFlushPolicy
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import sovran.kotlin.Subscriber

@Serializable
data class MeergoSettings(
    var apiKey: String,
    var endpoint: String? = null
)

/**
 * Meergo Analytics plugin that is used to send events to Meergo's tracking api, in the choice of region.
 * How it works
 * - Plugin receives `endpoint` settings
 * - We store events into a file with the batch api format
 * - We upload events on a dedicated thread using the batch api
 */
class MeergoDestination: DestinationPlugin(), VersionedPlugin, Subscriber {

    private var pipeline: EventPipeline? = null
    var flushPolicies: List<FlushPolicy> = emptyList()
    override val key: String = "Meergo"

    override fun track(payload: TrackEvent): BaseEvent {
        enqueue(payload)
        return payload
    }

    override fun identify(payload: IdentifyEvent): BaseEvent {
        enqueue(payload)
        return payload
    }

    override fun screen(payload: ScreenEvent): BaseEvent {
        enqueue(payload)
        return payload
    }

    override fun group(payload: GroupEvent): BaseEvent {
        enqueue(payload)
        return payload
    }

    override fun alias(payload: AliasEvent): BaseEvent {
        enqueue(payload)
        return payload
    }


    private fun enqueue(payload: BaseEvent) {
        pipeline?.put(payload)
    }

    override fun setup(analytics: Analytics) {
        super.setup(analytics)

        // convert flushAt and flushIntervals into FlushPolicies
        flushPolicies = if (analytics.configuration.flushPolicies.isEmpty()) {
            listOf(
                CountBasedFlushPolicy(analytics.configuration.flushAt),
                FrequencyFlushPolicy(analytics.configuration.flushInterval * 1000L)
            )
        } else {
            analytics.configuration.flushPolicies
        }

        // Add DestinationMetadata enrichment plugin
        add(DestinationMetadataPlugin())

        with(analytics) {
            pipeline = EventPipeline(
                analytics,
                key,
                configuration.writeKey,
                flushPolicies,
                configuration.endpoint
            )

            analyticsScope.launch(analyticsDispatcher) {
                store.subscribe(
                    subscriber = this@MeergoDestination,
                    stateClazz = System::class,
                    initialState = true,
                    handler = this@MeergoDestination::onEnableToggled
                )
            }
        }
    }

    override fun update(settings: Settings, type: Plugin.UpdateType) {
        super.update(settings, type)
        if (settings.hasIntegrationSettings(this)) {
            // only populate the endpoint value if it exists
            settings.destinationSettings<MeergoSettings>(key)?.endpoint?.let {
                pipeline?.endpoint = it
            }
        }
    }

    override fun flush() {
        pipeline?.flush()
    }

    override fun version(): String {
        return Constants.LIBRARY_VERSION
    }

    internal fun onEnableToggled(state: System) {
        if (state.enabled) {
            pipeline?.start()
        }
        else {
            pipeline?.stop()
        }
    }
}