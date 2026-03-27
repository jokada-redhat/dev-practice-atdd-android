package com.example.atdd.test

import android.app.Application
import android.content.Context
import com.example.atdd.AtddApplication
import io.cucumber.android.runner.CucumberAndroidJUnitRunner

class TestRunner : CucumberAndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application =
        super.newApplication(cl, AtddApplication::class.java.name, context)
}
