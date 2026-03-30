package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atdd.adapter.LoanedArtifact
import com.example.atdd.adapter.LoanedArtifactAdapter
import com.google.android.material.appbar.MaterialToolbar

class LoanedArtifactsActivity : AppCompatActivity() {

    private val app by lazy { application as AtddApplication }
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
        findViewById<RecyclerView>(R.id.recyclerViewLoans).apply {
            layoutManager = LinearLayoutManager(this@LoanedArtifactsActivity)
            adapter = this@LoanedArtifactsActivity.adapter
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
