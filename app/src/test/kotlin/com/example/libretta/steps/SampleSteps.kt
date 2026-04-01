package com.example.libretta.steps

import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertTrue

class SampleSteps {

    private var appInitialized = false
    private var appStarted = false

    @Given("アプリが初期化されている")
    fun appIsInitialized() {
        appInitialized = true
    }

    @When("アプリを起動する")
    fun startApp() {
        assertTrue("アプリが初期化されていません", appInitialized)
        appStarted = true
    }

    @Then("メイン画面が表示される")
    fun mainScreenIsDisplayed() {
        assertTrue("アプリが起動していません", appStarted)
    }
}
