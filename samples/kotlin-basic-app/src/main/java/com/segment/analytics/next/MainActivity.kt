package com.segment.analytics.next

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.segment.analytics.kotlin.core.Analytics

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        Analytics.debugLogsEnabled = true
        val app = application as MainApplication
        val analytics = app.analytics

        val startSessionButton: Button = findViewById(R.id.startSessionButton)
        startSessionButton.setOnClickListener {
            analytics.startSession(null)
        }

        val endSessionButton: Button = findViewById(R.id.endSessionButton)
        endSessionButton.setOnClickListener {
            analytics.endSession()
        }

        val clickButton: Button = findViewById(R.id.clickButton)
        clickButton.setOnClickListener {
            analytics.track("button clicked")
        }

        val identifyButton: Button = findViewById(R.id.identifyButton)
        identifyButton.setOnClickListener {
            analytics.identify("user1234")
        }
        
        val resetButton: Button = findViewById(R.id.resetButton)
        resetButton.setOnClickListener {
            analytics.reset()
        }
    }

}