package com.example.atdd

import android.app.Application
import okhttp3.OkHttpClient

class AtddApplication : Application() {
    var baseUrl: String = "https://production.example.com"
    val okHttpClient: OkHttpClient = OkHttpClient()
}
