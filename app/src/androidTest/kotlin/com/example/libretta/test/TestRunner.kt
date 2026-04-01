package com.example.libretta.test

import android.app.Application
import android.content.Context
import com.example.libretta.LibrettaApplication
import io.cucumber.android.runner.CucumberAndroidJUnitRunner

class TestRunner : CucumberAndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application =
        super.newApplication(cl, LibrettaApplication::class.java.name, context)
}
