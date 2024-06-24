package com.segment.analytics.kotlin.core

fun newSession(id: Long?, timeout: Long): SessionInfo {
        var sessionId = id
        val now = java.lang.System.currentTimeMillis()
        if (sessionId == null) {
            sessionId = java.lang.System.currentTimeMillis()
        }
        val expiration = now + timeout
        return SessionInfo(sessionId, expiration, true)
}

