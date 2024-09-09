package com.meergo.analytics.kotlin.core.compat

import kotlinx.serialization.json.JsonObject

interface JsonSerializable {
    fun serialize() : JsonObject
}