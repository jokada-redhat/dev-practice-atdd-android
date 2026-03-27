package com.example.atdd.test

import io.cucumber.junit.CucumberOptions

@CucumberOptions(
    features = ["features"],
    glue = ["com.example.atdd.steps.ui"]
)
class CucumberTestOptions
