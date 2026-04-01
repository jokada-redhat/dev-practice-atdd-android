package com.example.libretta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libretta.adapter.LoanedArtifact
import com.example.libretta.adapter.LoanedArtifactAdapter
import com.example.libretta.loan.ReturnBookRequest
import com.example.libretta.loan.ReturnBookResult
import com.example.libretta.loan.ReturnBookUseCase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LoanedArtifactsActivity : AppCompatActivity() {

    private val app by lazy { application as LibrettaApplication }
    private val returnBookUseCase by lazy {
        ReturnBookUseCase(app.loanRepository, app.bookRepository, app.memberRepository)
    }
    private lateinit var adapter: LoanedArtifactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loaned_artifacts)

        setupToolbar()
        setupRecyclerView()
        loadArtifacts()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = LoanedArtifactAdapter()
        adapter.setOnReturnClickListener { artifact -> confirmReturn(artifact) }
        findViewById<RecyclerView>(R.id.recyclerViewLoans).apply {
            layoutManager = LinearLayoutManager(this@LoanedArtifactsActivity)
            adapter = this@LoanedArtifactsActivity.adapter
        }
    }

    private fun confirmReturn(artifact: LoanedArtifact) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.return_confirm_title)
            .setMessage(getString(R.string.return_confirm_message, artifact.book.title, artifact.member.name))
            .setPositiveButton(R.string.return_button) { _, _ -> executeReturn(artifact) }
            .setNegativeButton(R.string.borrow_confirm_cancel, null)
            .show()
    }

    private fun executeReturn(artifact: LoanedArtifact) {
        val request = ReturnBookRequest(memberId = artifact.member.id, bookId = artifact.book.id)
        when (val result = returnBookUseCase.execute(request)) {
            is ReturnBookResult.Success -> {
                Toast.makeText(this, R.string.return_success, Toast.LENGTH_SHORT).show()
                loadArtifacts()
            }

            is ReturnBookResult.Failure -> {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadArtifacts() {
        val loans = app.loanRepository.findAll().filter { !it.isReturned }
        val artifacts = loans.mapNotNull { loan ->
            val book = app.bookRepository.findById(loan.bookId) ?: return@mapNotNull null
            val member = app.memberRepository.findById(loan.memberId) ?: return@mapNotNull null
            LoanedArtifact(loan, book, member)
        }

        adapter.updateArtifacts(artifacts)

        val textCount = findViewById<TextView>(R.id.textArtifactCount)
        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmptyState)

        textCount.text = getString(R.string.artifacts_in_circulation, artifacts.size)

        if (artifacts.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            findViewById<com.google.android.material.button.MaterialButton>(R.id.buttonGoToBorrowing)
                .setOnClickListener {
                    startActivity(Intent(this, MemberListActivity::class.java))
                }
        } else {
            layoutEmpty.visibility = View.GONE
        }
    }
}
