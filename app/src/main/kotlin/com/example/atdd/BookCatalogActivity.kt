package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atdd.adapter.BookAdapter
import com.example.atdd.model.Book
import com.example.atdd.model.BookStatus
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class BookCatalogActivity : AppCompatActivity() {

    private lateinit var bookAdapter: BookAdapter
    private val allBooks = listOf(
        Book("1", "The Infinite Library", "Jorge Luis Borges", "978-0142437889", "1941", BookStatus.AVAILABLE),
        Book("2", "Neuromancer", "William Gibson", "978-0441569595", "1984", BookStatus.BORROWED),
        Book("3", "The Left Hand of Darkness", "Ursula K. Le Guin", "978-0441478125", "1969", BookStatus.AVAILABLE),
        Book("4", "Foundation", "Isaac Asimov", "978-0553293357", "1951", BookStatus.AVAILABLE)
    )

    private var selectedMemberName: String = "Taro Yamada" // デフォルト値

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_catalog)

        // Intentから選択されたメンバー名を取得
        selectedMemberName = intent.getStringExtra("memberName") ?: "Taro Yamada"

        setupToolbar()
        setupActiveSelection()
        setupRecyclerView()
        setupFilterButtons()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupActiveSelection() {
        findViewById<TextView>(R.id.textSelectedMember).text = selectedMemberName

        findViewById<MaterialButton>(R.id.buttonChangeMember).setOnClickListener {
            // メンバー選択画面に戻る
            finish()
        }
    }

    private fun setupRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewBooks)

        bookAdapter = BookAdapter(allBooks) { book ->
            if (book.isAvailable) {
                Toast.makeText(
                    this,
                    "${book.title} ${getString(R.string.book_borrowed_success)}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@BookCatalogActivity)
            adapter = bookAdapter
        }
    }

    private fun setupFilterButtons() {
        val buttonAll = findViewById<MaterialButton>(R.id.buttonFilterAll)
        val buttonAvailable = findViewById<MaterialButton>(R.id.buttonFilterAvailable)
        val buttonBorrowed = findViewById<MaterialButton>(R.id.buttonFilterBorrowed)

        // デフォルトで All を選択状態に
        setActiveButton(buttonAll, buttonAvailable, buttonBorrowed)

        buttonAll.setOnClickListener {
            setActiveButton(buttonAll, buttonAvailable, buttonBorrowed)
            bookAdapter.updateBooks(allBooks)
        }

        buttonAvailable.setOnClickListener {
            setActiveButton(buttonAvailable, buttonAll, buttonBorrowed)
            bookAdapter.updateBooks(allBooks.filter { it.isAvailable })
        }

        buttonBorrowed.setOnClickListener {
            setActiveButton(buttonBorrowed, buttonAll, buttonAvailable)
            bookAdapter.updateBooks(allBooks.filter { !it.isAvailable })
        }
    }

    private fun setActiveButton(active: MaterialButton, vararg others: MaterialButton) {
        // アクティブなボタンを Tonal スタイルに
        active.setBackgroundColor(getColor(R.color.primary))
        active.setTextColor(getColor(R.color.on_primary))

        // 他のボタンを Outlined スタイルに
        others.forEach { button ->
            button.setBackgroundColor(getColor(android.R.color.transparent))
            button.setTextColor(getColor(R.color.primary))
        }
    }
}
