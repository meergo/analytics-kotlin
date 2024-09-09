package com.meergo.analytics.kotlin.core.platform.plugins.logger

class ConsoleLogger: Logger {
    override fun parseLog(log: LogMessage) {
        println("[Meergo ${log.kind.toString()} ${log.message}")
    }

}