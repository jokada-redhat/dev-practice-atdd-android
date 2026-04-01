package com.example.libretta.auth

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthApiClient(private val httpClient: OkHttpClient, private val baseUrl: String) : AuthRepository {

    override fun login(request: LoginRequest): LoginResult {
        val json = JSONObject().apply {
            put("email", request.email)
            put("password", request.password)
        }

        val httpRequest = Request.Builder()
            .url("$baseUrl/api/auth/login")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            httpClient.newCall(httpRequest).execute().use { response ->
                val body = response.body?.string() ?: ""
                val responseJson = JSONObject(body)

                when (response.code) {
                    200 -> LoginResult.Success(
                        responseJson.getString("token"),
                        responseJson.getString("displayName")
                    )

                    401 -> LoginResult.Failure(
                        responseJson.optString("error", "メールアドレスまたはパスワードが正しくありません")
                    )

                    else -> LoginResult.Failure("予期しないエラーが発生しました")
                }
            }
        } catch (e: Exception) {
            LoginResult.Failure("通信エラーが発生しました")
        }
    }
}
