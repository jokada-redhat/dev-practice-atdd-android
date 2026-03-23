package com.example.atdd

import io.cucumber.junit.Cucumber
import io.cucumber.junit.CucumberOptions
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    features = ["src/test/resources/features"],
    glue = ["com.example.atdd.steps"],
    plugin = ["pretty", "html:build/reports/cucumber.html"]
)
class RunCucumberTest
