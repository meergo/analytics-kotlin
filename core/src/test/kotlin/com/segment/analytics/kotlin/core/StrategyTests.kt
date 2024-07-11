package com.segment.analytics.kotlin.core

import com.segment.analytics.kotlin.core.utils.StubPlugin
import com.segment.analytics.kotlin.core.utils.clearPersistentStorage
import com.segment.analytics.kotlin.core.utils.mockHTTPClient
import com.segment.analytics.kotlin.core.utils.testAnalytics
import io.mockk.clearAllMocks
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.UUID
import java.util.stream.Stream
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StrategyTests {
    private val strategies = arrayOf("ABC", "AB-C", "A-B-C", "AC-B")
    private val autoTrack = arrayOf(true, false)

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    init {
        clearAllMocks()
        Telemetry.enable = false
    }

    @BeforeEach
    fun setup() {
        clearPersistentStorage()
        mockHTTPClient()
        globalTime = null
        globalStrategy = null
    }

    @AfterAll
    fun cleanup() {
        globalTime = null
        globalStrategy = null
    }

    @Test
    fun `startSession argument validation`() {
        globalStrategy = "AB-C"
        val config = Configuration(
            writeKey = "123",
            application = "Test",
            sessionAutoTrack = false,
            autoAddMeergoDestination = false
        )
        val analytics = testAnalytics(config, testScope, testDispatcher)
        val mockPlugin = spyk(StubPlugin())
        analytics.add(mockPlugin)
        analytics.userInfo = UserInfo(UUID.randomUUID().toString(), "274084295", buildJsonObject { put("first_name", "Susan") })
        var ids = listOf<Any?>(null, 1L, 100L, Long.MAX_VALUE)
        for (id in ids) {
            analytics.startSession(id)
        }
        ids = listOf("a", emptyJsonObject, -100L, -10.56, -1, 0, -0, 0.1, 23.904, Long.MAX_VALUE + 1)
        for (id in ids) {
            val error = assertThrows<Error> {
                analytics.startSession(id)
            }
            assertEquals("sessionId must be a positive Long", error.message)
        }
    }

    @Test
    fun `sessions with auto tracking`() {
        globalTime = FakeTime()
        globalStrategy = "AB-C"
        val config = Configuration(
            writeKey = "123",
            application = "Test",
            sessionAutoTrack = true,
            autoAddMeergoDestination = false
        )
        val analytics = testAnalytics(config, testScope, testDispatcher)
        val mockPlugin = spyk(StubPlugin())
        analytics.add(mockPlugin)
        var sessionId = now()
        assertEquals(analytics.getSessionId(), sessionId)
        globalTime!!.tick(2.minutes.toLong(DurationUnit.MILLISECONDS))
        assertEquals(analytics.getSessionId(), sessionId)
        globalTime!!.tick(5.minutes.toLong(DurationUnit.MILLISECONDS))
        assertEquals(analytics.getSessionId(), null)
        analytics.track("click")
        sessionId = now()
        assertEquals(analytics.getSessionId(), sessionId)
        analytics.reset()
        assertEquals(analytics.getSessionId(), null)
    }

    @Test
    fun `sessions without auto tracking`() {
        globalTime = FakeTime()
        globalStrategy = "AB-C"
        val config = Configuration(
            writeKey = "123",
            application = "Test",
            sessionAutoTrack = false,
            autoAddMeergoDestination = false
        )
        val analytics = testAnalytics(config, testScope, testDispatcher)
        val mockPlugin = spyk(StubPlugin())
        analytics.add(mockPlugin)
        globalTime!!.tick(10.milliseconds.toLong(DurationUnit.MILLISECONDS))
        assertEquals(analytics.getSessionId(), null)
        globalTime!!.tick(2.minutes.toLong(DurationUnit.MILLISECONDS))
        assertEquals(analytics.getSessionId(), null)
        globalTime!!.tick(5.minutes.toLong(DurationUnit.MILLISECONDS))
        assertEquals(analytics.getSessionId(), null)
        analytics.track("click")
        globalTime!!.tick(100.milliseconds.toLong(DurationUnit.MILLISECONDS))
        assertEquals(analytics.getSessionId(), null)
        globalTime!!.tick(300.milliseconds.toLong(DurationUnit.MILLISECONDS))
        analytics.startSession(728472643L)
        assertEquals(analytics.getSessionId(), 728472643L)
        globalTime!!.tick(10.minutes.toLong(DurationUnit.MILLISECONDS))
        assertEquals(analytics.getSessionId(), 728472643L)
        analytics.endSession()
        assertEquals(analytics.getSessionId(), null)
        analytics.track("click")
        globalTime!!.tick(100.milliseconds.toLong(DurationUnit.MILLISECONDS))
        assertEquals(analytics.getSessionId(), null)
        globalTime!!.tick(300.milliseconds.toLong(DurationUnit.MILLISECONDS))
        analytics.startSession(728819037L)
        assertEquals(analytics.getSessionId(), 728819037L)
        analytics.reset()
        assertEquals(analytics.getSessionId(), null)
    }

    @ParameterizedTest(name = "strategy {0} with autoTrack set to {1}")
    @MethodSource("provideStrategyAndAutoTrack")
    fun `identify and reset with each strategy, both with and without session`(strategy: String, autoTrack: Boolean) {
        globalStrategy = strategy
        val config = Configuration(
            writeKey = "123",
            application = "Test",
            sessionAutoTrack = autoTrack,
            autoAddMeergoDestination = false
        )
        val analytics = testAnalytics(config, testScope, testDispatcher)
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

    @Test
    fun `changing User ID resets traits and Anonymous ID`() {
        globalStrategy = "AB-C"
        val config = Configuration(
            writeKey = "123",
            application = "Test",
            sessionAutoTrack = false,
            autoAddMeergoDestination = false
        )
        val analytics = testAnalytics(config, testScope, testDispatcher)
        val mockPlugin = spyk(StubPlugin())
        analytics.add(mockPlugin)
        analytics.userInfo = UserInfo(UUID.randomUUID().toString(), "274084295", buildJsonObject { put("first_name", "Susan") })
        analytics.identify("920577314")
        val identify = slot<IdentifyEvent>()
        verify { mockPlugin.identify(capture(identify)) }
        val event = identify.captured
        assertEquals(analytics.userInfo.traits, emptyJsonObject)
        val newAnonymousId = analytics.userInfo.anonymousId
        assertEquals(event.traits, emptyJsonObject)
        assertEquals(event.anonymousId, newAnonymousId)
    }
}