package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.atdd.session.SessionManager
import com.example.atdd.session.SharedPreferencesSessionRepository

class TopActivity : AppCompatActivity() {

    private val sessionManager by lazy {
        SessionManager(SharedPreferencesSessionRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)

        findViewById<TextView>(R.id.textDisplayName).text =
            sessionManager.getDisplayName()

        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            sessionManager.clearSession()

            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
