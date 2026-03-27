package com.example.atdd.test

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import org.json.JSONObject

fun createAuthDispatcher(registeredEmail: String, registeredPassword: String, displayName: String): Dispatcher =
    object : Dispatcher() {
        override fun dispatch(request: RecordedRequest): MockResponse {
            if (request.url.encodedPath != "/api/auth/login" || request.method != "POST") {
                return MockResponse.Builder().code(404)
                    .body("""{"error":"Not Found"}""").build()
            }
            val body = request.body?.utf8() ?: ""
            val json = JSONObject(body)
            val email = json.optString("email", "")
            val password = json.optString("password", "")
            return if (email == registeredEmail && password == registeredPassword) {
                MockResponse.Builder().code(200)
                    .body("""{"token":"mock-token","displayName":"$displayName"}""").build()
            } else {
                MockResponse.Builder().code(401)
                    .body("""{"error":"メールアドレスまたはパスワードが正しくありません"}""").build()
            }
        }
    }
