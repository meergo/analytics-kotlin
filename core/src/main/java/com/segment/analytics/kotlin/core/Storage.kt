package com.segment.analytics.kotlin.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import sovran.kotlin.Store
import java.io.File

/**
 * Storage interface that abstracts storage of
 * - user data
 * - segment settings
 * - segment events
 * - other configs
 *
 * Constraints:
 * - Segment Events must be stored on a file, following the batch format
 * - all storage is in terms of String (to make API simple)
 * - storage is restricted to keys declared in `Storage.Constants`
 */
interface Storage {
    companion object {
        /** Our servers only accept payloads < 32KB.  */
        const val MAX_PAYLOAD_SIZE = 32000 // 32KB.

        /**
         * Our servers only accept batches < 500KB. This limit is 475KB to account for extra data that
         * is not present in payloads themselves, but is added later, such as `sentAt`, `integrations` and other json tokens.
         */
        const val MAX_BATCH_SIZE = 475000 // 475KB.
    }

    enum class Constants(val rawVal: String) {
        UserId("segment.userId"),
        Traits("segment.traits"),
        AnonymousId("segment.anonymousId"),
        Settings("segment.settings"),
        Events("segment.events"),
        AppVersion("segment.app.version"),
        AppBuild("segment.app.build"),
        LegacyAppBuild("build"),
        DeviceId("segment.device.id"),
        SessionId("segment.sessionId"),
        SessionExpiration("segment.sessionExpiration"),
        SessionStart("segment.sessionStart"),
        Suspended("segment.suspended"),
    }

    val storageDirectory: File

    suspend fun subscribeToStore()
    suspend fun write(key: Constants, value: String)
    fun read(key: Constants): String?
    fun remove(key: Constants): Boolean
    fun removeFile(filePath: String): Boolean

    /**
     * Direct writes to a new file, and close the current file.
     * This function is useful in cases such as `flush`, that
     * we want to finish writing the current file, and have it
     * flushed to server.
     */
    suspend fun rollover()

    suspend fun userInfoUpdate(userInfo: UserInfo) {
        write(Constants.AnonymousId, userInfo.anonymousId)

        userInfo.userId?.let {
            write(Constants.UserId, it)
        } ?: run {
            remove(Constants.UserId)
        }

        userInfo.traits?.let {
            write(Constants.Traits, Json.encodeToString(JsonObject.serializer(), it))
        } ?: run {
            remove(Constants.Traits)
        }
    }

    suspend fun sessionInfoUpdate(sessionInfo: SessionInfo) {
        sessionInfo.id?.let {
            write(Constants.SessionId, it.toString())
        } ?: run {
            remove (Constants.SessionId)
            remove (Constants.SessionExpiration)
            remove (Constants.SessionStart)
            return
        }
        write(Constants.SessionExpiration, sessionInfo.expiration.toString())
        write(Constants.SessionStart, sessionInfo.start.toString())
    }

    suspend fun systemUpdate(system: System) {
        system.settings?.let {
            write(Constants.Settings, Json.encodeToString(Settings.serializer(), it))
        } ?: run {
            remove(Constants.Settings)
        }
    }

    suspend fun suspend(sessionId: Long?, sessionExpiration: Long, sessionStart: Boolean, userAnonymousId: String, userTraits: JsonObject?) {
        var suspendedSession: SuspendedSession? = null
        if (sessionId != null) {
            suspendedSession = SuspendedSession(sessionId, sessionExpiration, sessionStart)
        }
        val suspended = Suspended(suspendedSession, userAnonymousId, userTraits)
        write(Constants.Suspended, Json.encodeToString(suspended))
    }

    fun removeSuspended() {
        remove(Constants.Suspended)
    }

    fun restore(): List<Any?> {
        var session: SuspendedSession? = null
        var userAnonymousId = ""
        var userTraits: JsonObject? = null
        val suspended = read(Constants.Suspended)
        if (suspended != null) {
            val s = Json.decodeFromString<Suspended>(suspended)
            session = s.session
            userAnonymousId = s.userAnonymousId
            userTraits = s.userTraits
        }
        if (session == null) {
            session = SuspendedSession(null, 0, false)
        }
        remove(Constants.Suspended)
        return listOf(session.id, session.expiration, session.start, userAnonymousId, userTraits)
    }

}

fun parseFilePaths(filePathStr: String?): List<String> {
    return if (filePathStr.isNullOrEmpty()) {
        emptyList()
    } else {
        filePathStr.split(",").map { it.trim() }
    }
}

/**
 * Interface to provide a Storage Instance to the analytics client
 * Motivation:
 *  In order to support various platforms, plus making testing simpler, we abstract the storage
 *  provider via this interface
 */
interface StorageProvider {
    fun getStorage(
        analytics: Analytics,
        store: Store,
        writeKey: String,
        ioDispatcher: CoroutineDispatcher,
        application: Any
    ): Storage
}

@Serializable
data class SuspendedSession(val id: Long?, val expiration: Long, val start: Boolean)

@Serializable
data class Suspended(val session: SuspendedSession?, val userAnonymousId: String, val userTraits: JsonObject?)