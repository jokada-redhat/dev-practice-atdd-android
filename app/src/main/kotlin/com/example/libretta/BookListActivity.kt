package com.example.libretta

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libretta.adapter.BookListAdapter
import com.example.libretta.model.Book
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class BookListActivity : AppCompatActivity() {

    private val app by lazy { application as LibrettaApplication }
    private lateinit var adapter: BookListAdapter
    private var allBooks: List<Book> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        setupToolbar()
        setupRecyclerView()
        setupSearch()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        loadBooks()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = BookListAdapter(loanRepository = app.loanRepository)
        findViewById<RecyclerView>(R.id.recyclerViewBooks).apply {
            layoutManager = LinearLayoutManager(this@BookListActivity)
            adapter = this@BookListActivity.adapter
        }
    }

    private fun setupSearch() {
        findViewById<TextInputEditText>(R.id.editSearch).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterBooks(s?.toString().orEmpty())
            }
        })
    }

    private fun setupFab() {
        findViewById<FloatingActionButton>(R.id.fabAddBook).setOnClickListener {
            startActivity(Intent(this, AddBookActivity::class.java))
        }
    }

    private fun loadBooks() {
        allBooks = app.bookRepository.findAll()
        val query = findViewById<TextInputEditText>(R.id.editSearch).text?.toString().orEmpty()
        filterBooks(query)
    }

    private fun filterBooks(query: String) {
        val filtered = if (query.isBlank()) {
            allBooks
        } else {
            app.bookRepository.search(query)
        }
        adapter.updateBooks(filtered)
        updateUI(filtered)
    }

    private fun updateUI(books: List<Book>) {
        val textCount = findViewById<TextView>(R.id.textBookCount)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBooks)
        val layoutEmpty = findViewById<LinearLayout>(R.id.layoutEmptyState)

        textCount.text = getString(R.string.book_list_count, books.size)

        if (books.isEmpty()) {
            recyclerView.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
        }
    }
}
