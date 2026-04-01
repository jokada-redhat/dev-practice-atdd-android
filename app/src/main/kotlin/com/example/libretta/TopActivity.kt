package com.example.libretta

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.libretta.session.SessionManager
import com.example.libretta.session.SharedPreferencesSessionRepository
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

        toolbar.inflateMenu(R.menu.menu_top)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_debug -> {
                    startActivity(Intent(this, DebugSettingsActivity::class.java))
                    true
                }
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

        findViewById<MaterialCardView>(R.id.cardBookList).setOnClickListener {
            startActivity(Intent(this, BookListActivity::class.java))
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
