package com.example.libretta

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libretta.adapter.MemberAdapter
import com.example.libretta.model.Member
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MemberListActivity : AppCompatActivity() {

    private lateinit var memberAdapter: MemberAdapter
    private val app by lazy { application as LibrettaApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_list)

        setupToolbar()
        setupRecyclerView()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        memberAdapter.updateMembers(app.memberRepository.findAll())
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

        memberAdapter = MemberAdapter(app.memberRepository.findAll()) { member ->
            // メンバー選択時に書籍一覧画面に遷移
            val intent = Intent(this, BookCatalogActivity::class.java).apply {
                putExtra("memberName", member.name)
                putExtra("memberId", member.id)
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
