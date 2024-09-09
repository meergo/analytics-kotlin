package com.meergo.analytics.next

import android.app.Application
import com.meergo.analytics.kotlin.android.Analytics
import com.meergo.analytics.kotlin.core.*

class MainApplication : Application() {

    lateinit var analytics: Analytics

    override fun onCreate() {
        super.onCreate()

        analytics = Analytics("", applicationContext) {
            this.endpoint = ""
            this.collectDeviceId = true
            this.trackApplicationLifecycleEvents = true
            this.trackDeepLinks = true
            this.flushAt = 1
            this.flushInterval = 0
        }
    }
}