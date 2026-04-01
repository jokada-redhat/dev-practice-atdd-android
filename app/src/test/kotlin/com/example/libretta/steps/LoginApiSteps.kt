package com.example.libretta.steps

import io.cucumber.datatable.DataTable
import io.cucumber.java.After
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class LoginApiSteps {

    private lateinit var server: MockWebServer
    private lateinit var responseBody: String
    private var responseCode: Int = 0

    @Given("ログインAPIサーバーが起動している")
    fun apiServerIsRunning() {
        server = MockWebServer()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.url.encodedPath != "/api/auth/login" || request.method != "POST") {
                    return MockResponse.Builder()
                        .code(404)
                        .body("{\"error\":\"Not Found\"}")
                        .build()
                }

                val body = request.body?.utf8() ?: ""
                val json = JSONObject(body)
                val email = json.optString("email", "")
                val password = json.optString("password", "")

                if (email.isBlank() || !email.contains("@") || password.isBlank()) {
                    return MockResponse.Builder()
                        .code(400)
                        .body("{\"error\":\"不正なリクエストです\"}")
                        .build()
                }

                if (email == "test@example.com" && password == "password123") {
                    return MockResponse.Builder()
                        .code(200)
                        .body("{\"token\":\"mock-jwt-token-12345\",\"displayName\":\"テストユーザー\"}")
                        .build()
                }

                return MockResponse.Builder()
                    .code(401)
                    .body("{\"error\":\"メールアドレスまたはパスワードが正しくありません\"}")
                    .build()
            }
        }
        server.start()
    }

    @When("POST {string} に以下のJSONを送信する:")
    fun postJsonTo(path: String, dataTable: DataTable) {
        val data = dataTable.asMap(String::class.java, String::class.java)
        val json = JSONObject(data).toString()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(server.url(path).toString())
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        responseCode = response.code
        responseBody = response.body?.string() ?: ""
    }

    @Then("レスポンスステータスが {int} である")
    fun responseStatusIs(expectedStatus: Int) {
        assertEquals("ステータスコードが一致しない", expectedStatus, responseCode)
    }

    @Then("レスポンスに {string} フィールドが含まれる")
    fun responseContainsField(fieldName: String) {
        val json = JSONObject(responseBody)
        assertTrue(
            "レスポンスに '$fieldName' フィールドがありません: $responseBody",
            json.has(fieldName)
        )
    }

    @After
    fun tearDown() {
        if (::server.isInitialized) {
            server.close()
        }
    }
}
