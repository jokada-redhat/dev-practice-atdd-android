package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atdd.adapter.MemberAdapter
import com.example.atdd.model.Member
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MemberListActivity : AppCompatActivity() {

    private lateinit var memberAdapter: MemberAdapter
    private val sampleMembers = listOf(
        Member("DA-8821", "Taro Yamada", "taro@example.com", loanCount = 2),
        Member("DA-1156", "Marcus Thorne", "marcus@example.com", loanCount = 0),
        Member("DA-5509", "Julian Chen", "julian@example.com", loanCount = 1)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_list)

        setupToolbar()
        setupRecyclerView()
        setupFab()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewMembers)

        memberAdapter = MemberAdapter(sampleMembers) { member ->
            // メンバー選択時に書籍一覧画面に遷移
            val intent = Intent(this, BookCatalogActivity::class.java).apply {
                putExtra("memberName", member.name)
            }
            startActivity(intent)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MemberListActivity)
            adapter = memberAdapter
        }
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddMember).setOnClickListener {
            startActivity(Intent(this, AddMemberActivity::class.java))
        }
    }
}
