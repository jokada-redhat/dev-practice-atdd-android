package com.example.libretta

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.libretta.adapter.BookAdapter
import com.example.libretta.loan.BorrowBookRequest
import com.example.libretta.loan.BorrowBookResult
import com.example.libretta.loan.BorrowBookUseCase
import com.example.libretta.model.Book
import com.example.libretta.model.BookStatus
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class BookCatalogActivity : AppCompatActivity() {

    private lateinit var bookAdapter: BookAdapter
    private val app by lazy { application as LibrettaApplication }
    private val borrowBookUseCase by lazy {
        BorrowBookUseCase(app.loanRepository, app.bookRepository, app.memberRepository)
    }
    private var selectedMemberId: String = ""
    private var selectedMemberName: String = "山田太郎"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_catalog)

        // Intentから選択されたメンバー情報を取得
        selectedMemberName = intent.getStringExtra("memberName") ?: "Taro Yamada"
        selectedMemberId = intent.getStringExtra("memberId") ?: ""

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
        val allBooks = app.bookRepository.findAll()

        bookAdapter = BookAdapter(allBooks) { book ->
            if (book.isAvailable) {
                showBorrowConfirmDialog(book)
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
            bookAdapter.updateBooks(app.bookRepository.findAll())
        }

        buttonAvailable.setOnClickListener {
            setActiveButton(buttonAvailable, buttonAll, buttonBorrowed)
            bookAdapter.updateBooks(app.bookRepository.filterByStatus(BookStatus.AVAILABLE))
        }

        buttonBorrowed.setOnClickListener {
            setActiveButton(buttonBorrowed, buttonAll, buttonAvailable)
            bookAdapter.updateBooks(app.bookRepository.filterByStatus(BookStatus.BORROWED))
        }
    }

    private fun showBorrowConfirmDialog(book: com.example.libretta.model.Book) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.borrow_confirm_title)
            .setMessage(getString(R.string.borrow_confirm_message, book.title, selectedMemberName))
            .setPositiveButton(R.string.borrow_confirm_yes) { _, _ ->
                executeBorrow(book)
            }
            .setNegativeButton(R.string.borrow_confirm_cancel, null)
            .show()
    }

    private fun executeBorrow(book: com.example.libretta.model.Book) {
        val request = BorrowBookRequest(memberId = selectedMemberId, bookId = book.id)
        when (val result = borrowBookUseCase.execute(request)) {
            is BorrowBookResult.Success -> {
                val confirmIntent = Intent(this, LoanConfirmationActivity::class.java).apply {
                    putExtra("bookTitle", book.title)
                    putExtra("bookAuthor", book.author)
                    putExtra("memberName", selectedMemberName)
                    putExtra("memberId", selectedMemberId)
                }
                startActivity(confirmIntent)
                bookAdapter.updateBooks(app.bookRepository.findAll())
            }
            is BorrowBookResult.Failure -> {
                Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
            }
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
