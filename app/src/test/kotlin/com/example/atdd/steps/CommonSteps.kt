package com.example.atdd.steps

import io.cucumber.java.en.Then
import org.junit.Assert.assertEquals

/**
 * 複数のステップクラスで共有される共通ステップ定義
 */
class CommonSteps {

    companion object {
        var lastErrorMessage: String? = null
    }

    @Then("エラーメッセージ {string} が返される")
    fun errorMessageIsReturned(expectedMessage: String) {
        assertEquals("エラーメッセージが一致しません", expectedMessage, lastErrorMessage)
    }
}
