package com.example.atdd

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.atdd.debug.DummyDataGenerator
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DebugSettingsActivity : AppCompatActivity() {

    private val app by lazy { application as AtddApplication }
    private val generator by lazy {
        DummyDataGenerator(app.memberRepository, app.bookRepository, app.loanRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_settings)

        setupToolbar()
        setupButtons()
        updateCounts()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        findViewById<MaterialButton>(R.id.buttonLoadDummy).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.debug_load_dummy_confirm_title)
                .setMessage(R.string.debug_load_dummy_confirm_message)
                .setPositiveButton(R.string.debug_execute) { _, _ ->
                    generator.loadDummyData()
                    updateCounts()
                    Toast.makeText(this, R.string.debug_load_dummy_success, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.borrow_confirm_cancel, null)
                .show()
        }

        findViewById<MaterialButton>(R.id.buttonClearAll).setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.debug_clear_confirm_title)
                .setMessage(R.string.debug_clear_confirm_message)
                .setPositiveButton(R.string.debug_execute) { _, _ ->
                    generator.clearAll()
                    updateCounts()
                    Toast.makeText(this, R.string.debug_clear_success, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.borrow_confirm_cancel, null)
                .show()
        }
    }

    private fun updateCounts() {
        val bookCount = app.bookRepository.findAll().size
        val memberCount = app.memberRepository.findAll().size
        val loanCount = app.loanRepository.findAll().filter { !it.isReturned }.size

        findViewById<TextView>(R.id.textBookCount).text =
            getString(R.string.debug_book_count, bookCount)
        findViewById<TextView>(R.id.textMemberCount).text =
            getString(R.string.debug_member_count, memberCount)
        findViewById<TextView>(R.id.textLoanCount).text =
            getString(R.string.debug_loan_count, loanCount)
    }
}
