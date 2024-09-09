package com.meergo.analytics.kotlin.core.platform.plugins.logger

import com.meergo.analytics.kotlin.core.Analytics

// Internal log usage
fun Analytics.Companion.meergoLog(message: String, kind: LogKind = LogKind.ERROR) {
   val logger = logger
   val logMessage = LogMessage(kind, message=message)
   when (kind){
      LogKind.DEBUG -> {
         if (debugLogsEnabled) {
            logger.parseLog(logMessage)
         }
      }
      else -> logger.parseLog(logMessage)
   }
}
