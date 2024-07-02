package com.segment.analytics.kotlin.core

import com.segment.analytics.kotlin.core.utils.StubPlugin
import com.segment.analytics.kotlin.core.utils.clearPersistentStorage
import com.segment.analytics.kotlin.core.utils.mockHTTPClient
import com.segment.analytics.kotlin.core.utils.testAnalytics
import io.mockk.*
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StrategyTests {
    private val strategies = arrayOf("ABC", "AB-C", "A-B-C", "AC-B")
    private val autoTrack = arrayOf(true, false)

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    init {
        Telemetry.enable = false
    }

    @BeforeEach
    fun setup() {
        clearPersistentStorage()
        mockHTTPClient()
    }

    @ParameterizedTest(name = "Test strategy {0} with autoTrack set to {1}")
    @MethodSource("provideStrategyAndAutoTrack")
    fun `Test identify and reset with each strategy, both with and without session`(strategy: String, autoTrack: Boolean) {

        val config = Configuration(
            writeKey = "123",
            application = "Test",
            sessionAutoTrack = autoTrack,
            autoAddSegmentDestination = false
        )
        val analytics = testAnalytics(config, strategy, testScope, testDispatcher)
        val mockPlugin = spyk(StubPlugin())
        analytics.add(mockPlugin)

        var sessionId = analytics.getSessionId()
        var anonymousId = analytics.anonymousId()
        val userTraits = buildJsonObject {
            put("score", 729)
        }
        analytics.userInfo.traits = userTraits
        val original = buildJsonObject {
            put("sessionId", sessionId)
            put("anonymousId", anonymousId)
            put("traits", userTraits)
        }

        analytics.identify("5F20MB18", buildJsonObject { put("name", "Susan") })
        val identify = slot<IdentifyEvent>()
        verify { mockPlugin.identify(capture(identify)) }
        val event = identify.captured
        assertEquals(event.userId, "5F20MB18")
        if (!autoTrack) {
            assertTrue("sessionId" !in event.context)
            assertTrue("sessionStart" !in event.context)
            assertEquals(analytics.getSessionId(), null)
        }

        if (strategy.indexOf("-B") > -1) {
            if (autoTrack) {
                assertTrue(event.context["sessionId"]?.jsonPrimitive?.longOrNull != sessionId)
            }
            assertTrue(event.anonymousId != anonymousId)
            assertEquals(event.traits, buildJsonObject { put("name", "Susan") })
        } else {
            if (autoTrack) {
                assertEquals(event.context["sessionId"]?.jsonPrimitive?.longOrNull, sessionId)
            }
            assertEquals(event.anonymousId, anonymousId)
            assertEquals(event.traits, buildJsonObject {
                put("name", "Susan")
                put("score", 729)
            })
        }
        assertEquals(analytics.anonymousId(), event.anonymousId)
        assertEquals(analytics.userInfo.userId, event.userId)
        assertEquals(analytics.userInfo.traits, event.traits)

        sessionId = analytics.getSessionId()
        anonymousId = analytics.anonymousId()

        analytics.reset()

        if (!autoTrack) {
            assertEquals(analytics.getSessionId(), null)
        }

        if (strategy == "AC-B") {
            if (autoTrack) {
                assertEquals(analytics.getSessionId(), original["sessionId"]?.jsonPrimitive?.longOrNull)
            }
            assertEquals(analytics.anonymousId(), original["anonymousId"]?.jsonPrimitive?.contentOrNull)
            assertEquals(analytics.userInfo.traits, original["traits"])
        } else if (strategy.indexOf("-C") > -1) {
            if (autoTrack) {
                assertTrue(analytics.getSessionId() != original["sessionId"]?.jsonPrimitive?.longOrNull)
                assertTrue(analytics.getSessionId() != sessionId)
            }
            assertTrue(analytics.anonymousId() != original["anonymousId"]?.jsonPrimitive?.contentOrNull)
            assertTrue(analytics.anonymousId() != anonymousId)
            assertEquals(analytics.userInfo.traits, null)
        } else {
            if (autoTrack) {
                assertEquals(analytics.getSessionId(), sessionId)
            }
            assertEquals(analytics.anonymousId(), anonymousId)
            assertEquals(analytics.userInfo.traits, null)
        }

        analytics.startSession(215271912L)
        anonymousId = analytics.anonymousId()
        analytics.reset(true)
        assertTrue(anonymousId != analytics.anonymousId())
        assertEquals(analytics.getSessionId(), null)
    }

    private fun provideStrategyAndAutoTrack(): Stream<Arguments> {
        return Stream.of(
            Arguments.of(strategies[0], autoTrack[0]),
            Arguments.of(strategies[0], autoTrack[1]),
            Arguments.of(strategies[1], autoTrack[0]),
            Arguments.of(strategies[1], autoTrack[1]),
            Arguments.of(strategies[2], autoTrack[0]),
            Arguments.of(strategies[2], autoTrack[1]),
            Arguments.of(strategies[3], autoTrack[0]),
            Arguments.of(strategies[3], autoTrack[1]),
        )

    }
}