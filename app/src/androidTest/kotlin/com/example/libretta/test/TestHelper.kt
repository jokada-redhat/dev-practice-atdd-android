package com.example.libretta.test

import androidx.test.platform.app.InstrumentationRegistry
import com.example.libretta.LibrettaApplication
import mockwebserver3.MockWebServer

object TestHelper {
    fun getApp(): LibrettaApplication = InstrumentationRegistry.getInstrumentation()
        .targetContext.applicationContext as LibrettaApplication

    fun injectMockServerUrl(app: LibrettaApplication, server: MockWebServer) {
        app.baseUrl = server.url("/").toString().trimEnd('/')
    }
}
