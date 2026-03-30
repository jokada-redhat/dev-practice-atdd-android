package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LoanConfirmationActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var remainingSeconds = AUTO_REDIRECT_SECONDS
    private lateinit var textAutoRedirect: TextView

    private val countdownRunnable = object : Runnable {
        override fun run() {
            remainingSeconds--
            if (remainingSeconds <= 0) {
                goHome()
            } else {
                textAutoRedirect.text = getString(R.string.auto_redirect_message, remainingSeconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan_confirmation)

        textAutoRedirect = findViewById(R.id.textAutoRedirect)
        setupLoanDetails()
        setupGoHomeButton()
        startCountdown()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(countdownRunnable)
    }

    private fun setupLoanDetails() {
        val bookTitle = intent.getStringExtra("bookTitle") ?: ""
        val bookAuthor = intent.getStringExtra("bookAuthor") ?: ""
        val memberName = intent.getStringExtra("memberName") ?: ""
        val memberId = intent.getStringExtra("memberId") ?: ""

        findViewById<TextView>(R.id.textConfirmBookTitle).text = bookTitle
        findViewById<TextView>(R.id.textConfirmBookAuthor).text = bookAuthor
        findViewById<TextView>(R.id.textConfirmMemberName).text = memberName
        findViewById<TextView>(R.id.textConfirmMemberId).text =
            getString(R.string.member_id_prefix, memberId)

        val dueDate = LocalDate.now().plusWeeks(2)
        findViewById<TextView>(R.id.textConfirmDueDate).text =
            dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun setupGoHomeButton() {
        findViewById<MaterialButton>(R.id.buttonGoHome).setOnClickListener {
            goHome()
        }
    }

    private fun startCountdown() {
        textAutoRedirect.text = getString(R.string.auto_redirect_message, remainingSeconds)
        handler.postDelayed(countdownRunnable, 1000)
    }

    private fun goHome() {
        handler.removeCallbacks(countdownRunnable)
        startActivity(
            Intent(this, TopActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
        finish()
    }

    private companion object {
        const val AUTO_REDIRECT_SECONDS = 30
    }
}
