package com.example.atdd.steps.ui

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class ClickChildViewAction(private val childViewId: Int) : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return allOf()
    }

    override fun getDescription(): String {
        return "Click on a child view with id $childViewId"
    }

    override fun perform(uiController: UiController, view: View) {
        val childView = view.findViewById<View>(childViewId)
            ?: throw IllegalStateException("No view found with id $childViewId")
        childView.performClick()
    }
}
