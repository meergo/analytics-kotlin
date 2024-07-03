package com.segment.analytics.kotlin.core

fun newSession(id: Long?, timeout: Long): SessionInfo {
        var sessionId = id
        val now = now()
        if (sessionId == null) {
            sessionId = now()
        }
        val expiration = now + timeout
        return SessionInfo(sessionId, expiration, true)
}

