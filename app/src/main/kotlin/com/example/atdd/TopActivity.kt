package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.atdd.session.SessionManager
import com.example.atdd.session.SharedPreferencesSessionRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView

class TopActivity : AppCompatActivity() {

    private val sessionManager by lazy {
        SessionManager(SharedPreferencesSessionRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)

        setupToolbar()
        setupDisplayName()
        setupActionCards()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    handleLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupDisplayName() {
        findViewById<TextView>(R.id.textDisplayName).text =
            sessionManager.getDisplayName()
    }

    private fun setupActionCards() {
        findViewById<MaterialCardView>(R.id.cardBorrowing).setOnClickListener {
            startActivity(Intent(this, MemberListActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardReturns).setOnClickListener {
            startActivity(Intent(this, ReturnBookActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cardCheckStatus).setOnClickListener {
            startActivity(Intent(this, LoanedArtifactsActivity::class.java))
        }
    }

    private fun handleLogout() {
        sessionManager.clearSession()

        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
