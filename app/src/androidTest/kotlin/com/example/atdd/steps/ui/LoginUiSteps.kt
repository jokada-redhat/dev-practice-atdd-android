package com.example.atdd.steps.ui

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.atdd.LoginActivity
import com.example.atdd.R
import com.example.atdd.test.OkHttpIdlingResource
import com.example.atdd.test.TestHelper
import com.example.atdd.test.createAuthDispatcher
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import mockwebserver3.MockWebServer

class LoginUiSteps {

    private lateinit var server: MockWebServer
    private lateinit var idlingResource: OkHttpIdlingResource
    private var scenario: ActivityScenario<LoginActivity>? = null

    @Before("@login")
    fun setUp() {
        server = MockWebServer()
        server.start(0)
        val app = TestHelper.getApp()
        TestHelper.injectMockServerUrl(app, server)
        idlingResource = OkHttpIdlingResource.create("OkHttp", app.okHttpClient)
        IdlingRegistry.getInstance().register(idlingResource)
    }

    @After("@login")
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(idlingResource)
        server.close()
        scenario?.close()
    }

    @Given("未ログイン状態になっている")
    fun clearSession() {
        val context = TestHelper.getApp()
        context.getSharedPreferences("atdd_session", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @And("メールアドレス {string} がパスワード {string} で登録されている")
    fun registerUser(email: String, password: String) {
        server.dispatcher = createAuthDispatcher(email, password, "山田太郎")
    }

    @When("メールアドレス {string} とパスワード {string} でログインする")
    fun login(email: String, password: String) {
        scenario = ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.editEmail)).perform(typeText(email), closeSoftKeyboard())
        onView(withId(R.id.editPassword)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.buttonLogin)).perform(click())
        // OkHttp IdlingResource は HTTP 完了のみ待機する。
        // コルーチンの Main ディスパッチ + Activity 遷移を待つため短い待機を入れる。
        Thread.sleep(ACTIVITY_TRANSITION_WAIT_MS)
    }

    @Then("表示名 {string} がトップページに表示されている")
    fun verifyDisplayName(displayName: String) {
        onView(withId(R.id.textDisplayName)).check(matches(withText(displayName)))
    }

    @And("ログアウトボタンが表示されている")
    fun verifyLogoutButtonDisplayed() {
        onView(withId(R.id.buttonLogout)).check(matches(isDisplayed()))
    }

    @Then("エラーメッセージ {string} が表示されている")
    fun verifyErrorMessage(message: String) {
        onView(withId(R.id.textError)).check(matches(withText(message)))
        onView(withId(R.id.textError)).check(matches(isDisplayed()))
    }

    private companion object {
        const val ACTIVITY_TRANSITION_WAIT_MS = 2000L
    }
}
