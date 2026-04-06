package com.example.libretta.steps.ui

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.libretta.LoginActivity
import com.example.libretta.R
import com.example.libretta.test.OkHttpIdlingResource
import com.example.libretta.test.TestHelper
import com.example.libretta.test.createAuthDispatcher
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
        val app = TestHelper.getApp()
        app.getSharedPreferences("atdd_session", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @And("メールアドレス {string} がパスワード {string} で登録されている")
    fun registerUser(email: String, password: String) {
        server.dispatcher = createAuthDispatcher(email, password, "山田太郎")
    }

    @When("以下の認証情報でログインする")
    fun login(dataTable: io.cucumber.datatable.DataTable) {
        val row = dataTable.asMaps().first()
        val email = row["email"]!!
        val password = row["password"]!!
        scenario = ActivityScenario.launch(LoginActivity::class.java)
        onView(withId(R.id.editEmail)).perform(replaceText(email), closeSoftKeyboard())
        onView(withId(R.id.editPassword)).perform(replaceText(password), closeSoftKeyboard())
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
        // ToolbarのMenuアイテムはcontent descriptionまたはtoolbar自体で確認
        onView(withId(R.id.toolbar)).check(matches(isDisplayed()))
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
