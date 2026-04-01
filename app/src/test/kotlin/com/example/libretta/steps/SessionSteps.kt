package com.example.libretta.steps

import com.example.libretta.session.SessionManager
import com.example.libretta.session.SessionRepository
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class InMemorySessionRepository : SessionRepository {
    private var token: String? = null
    private var displayName: String? = null

    override fun saveToken(token: String) {
        this.token = token
    }

    override fun getToken(): String? = token

    override fun saveDisplayName(displayName: String) {
        this.displayName = displayName
    }

    override fun getDisplayName(): String? = displayName

    override fun clear() {
        token = null
        displayName = null
    }
}

class SessionSteps {

    private val sessionRepository = InMemorySessionRepository()
    private val sessionManager = SessionManager(sessionRepository)

    @Given("セッションが空である")
    fun sessionIsEmpty() {
        sessionRepository.clear()
    }

    @Given("トークン {string} と表示名 {string} でセッションが保存されている")
    fun sessionIsSaved(token: String, displayName: String) {
        sessionManager.saveSession(token, displayName)
    }

    @When("トークン {string} と表示名 {string} でセッションを保存する")
    fun saveSession(token: String, displayName: String) {
        sessionManager.saveSession(token, displayName)
    }

    @When("セッションをクリアする")
    fun clearSession() {
        sessionManager.clearSession()
    }

    @Then("ログイン済みと判定される")
    fun isLoggedIn() {
        assertTrue("ログイン済みであるべき", sessionManager.isLoggedIn())
    }

    @Then("未ログインと判定される")
    fun isNotLoggedIn() {
        assertFalse("未ログインであるべき", sessionManager.isLoggedIn())
    }

    @And("保存されたトークンは {string} である")
    fun savedTokenIs(expected: String) {
        assertEquals(expected, sessionRepository.getToken())
    }

    @And("保存された表示名は {string} である")
    fun savedDisplayNameIs(expected: String) {
        assertEquals(expected, sessionRepository.getDisplayName())
    }
}
