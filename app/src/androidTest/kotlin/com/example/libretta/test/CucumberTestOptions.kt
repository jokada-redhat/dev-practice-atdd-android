package com.example.libretta.test

import io.cucumber.junit.CucumberOptions

@CucumberOptions(
    features = ["features"],
    glue = ["com.example.libretta.steps.ui"],
    tags = "not @wip"
)
class CucumberTestOptions
