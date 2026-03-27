package com.example.atdd.test

import androidx.test.platform.app.InstrumentationRegistry
import com.example.atdd.AtddApplication
import mockwebserver3.MockWebServer

object TestHelper {
    fun getApp(): AtddApplication = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as AtddApplication

    fun injectMockServerUrl(app: AtddApplication, server: MockWebServer) {
        app.baseUrl = server.url("/").toString().trimEnd('/')
    }
}
