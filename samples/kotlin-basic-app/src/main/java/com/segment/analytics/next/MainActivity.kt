package com.segment.analytics.next

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.segment.analytics.kotlin.core.Analytics

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        Analytics.debugLogsEnabled = true
    }

}