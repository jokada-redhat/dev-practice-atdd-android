package com.example.libretta.steps

import com.example.libretta.auth.AuthSkipper
import com.example.libretta.session.SessionManager
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class SkipAuthSteps {

    private val sessionRepository = InMemorySessionRepository()
    private val sessionManager = SessionManager(sessionRepository)

    @Given("認証スキップテスト用にセッションが空である")
    fun sessionIsEmpty() {
        sessionRepository.clear()
    }

    @When("認証スキップが有効な状態でログインチェックを行う")
    fun skipAuthEnabled() {
        AuthSkipper.applyIfNeeded(skipAuth = true, sessionManager)
    }

    @When("認証スキップが無効な状態でログインチェックを行う")
    fun skipAuthDisabled() {
        AuthSkipper.applyIfNeeded(skipAuth = false, sessionManager)
    }

    @Then("ダミーセッションでログイン済みと判定される")
    fun isLoggedIn() {
        assertTrue("ログイン済みであるべき", sessionManager.isLoggedIn())
    }

    @Then("セッションは空のままで未ログインと判定される")
    fun isNotLoggedIn() {
        assertFalse("未ログインであるべき", sessionManager.isLoggedIn())
    }

    @Then("表示名が {string} で保存されている")
    fun displayNameIs(expected: String) {
        assertEquals(expected, sessionRepository.getDisplayName())
    }
}
