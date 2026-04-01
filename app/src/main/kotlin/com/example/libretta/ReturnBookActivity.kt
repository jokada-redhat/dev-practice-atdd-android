package com.example.libretta

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.textfield.TextInputEditText

class ReturnBookActivity : AppCompatActivity() {

    private val app by lazy { application as LibrettaApplication }
    private val returnBookUseCase by lazy {
        ReturnBookUseCase(app.loanRepository, app.bookRepository, app.memberRepository)
    }

    private lateinit var adapter: LoanedArtifactAdapter
    private var allArtifacts: List<LoanedArtifact> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_book)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
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
            layoutManager = LinearLayoutManager(this@ReturnBookActivity)
            adapter = this@ReturnBookActivity.adapter
        }
    }

    private fun setupSearch() {
        findViewById<TextInputEditText>(R.id.editSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterArtifacts(s?.toString().orEmpty())
            }
        })
    }

    private fun loadArtifacts() {
        val loans = app.loanRepository.findAll().filter { !it.isReturned }
        allArtifacts = loans.mapNotNull { loan ->
            val book = app.bookRepository.findById(loan.bookId) ?: return@mapNotNull null
            val member = app.memberRepository.findById(loan.memberId) ?: return@mapNotNull null
            LoanedArtifact(loan, book, member)
        }

        val query = findViewById<TextInputEditText>(R.id.editSearch).text?.toString().orEmpty()
        filterArtifacts(query)
    }

    private fun filterArtifacts(query: String) {
        val filtered = if (query.isBlank()) {
            allArtifacts
        } else {
            val q = query.lowercase()
            allArtifacts.filter {
                it.book.title.lowercase().contains(q) ||
                    it.book.isbn.lowercase().contains(q) ||
                    it.member.name.lowercase().contains(q) ||
                    it.member.id.lowercase().contains(q)
            }
        }

        adapter.updateArtifacts(filtered)
        updateUI(filtered)
    }

    private fun updateUI(artifacts: List<LoanedArtifact>) {
        val textCount = findViewById<TextView>(R.id.textResultCount)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLoans)
        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmptyState)

        textCount.text = getString(R.string.return_result_count, artifacts.size)

        if (artifacts.isEmpty()) {
            recyclerView.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
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
}
