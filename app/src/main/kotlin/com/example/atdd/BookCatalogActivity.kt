package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.atdd.adapter.BookAdapter
import com.example.atdd.loan.BorrowBookRequest
import com.example.atdd.loan.BorrowBookResult
import com.example.atdd.loan.BorrowBookUseCase
import com.example.atdd.model.Book
import com.example.atdd.model.BookStatus
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class BookCatalogActivity : AppCompatActivity() {

    private lateinit var bookAdapter: BookAdapter
    private val app by lazy { application as AtddApplication }
    private val borrowBookUseCase by lazy {
        BorrowBookUseCase(app.loanRepository, app.bookRepository, app.memberRepository)
    }
    private var selectedMemberId: String = ""
    private var selectedMemberName: String = "Taro Yamada"

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
                val request = BorrowBookRequest(memberId = selectedMemberId, bookId = book.id)
                when (val result = borrowBookUseCase.execute(request)) {
                    is BorrowBookResult.Success -> {
                        // 貸し出し確認画面へ遷移
                        val confirmIntent = Intent(this, LoanConfirmationActivity::class.java).apply {
                            putExtra("bookTitle", book.title)
                            putExtra("bookAuthor", book.author)
                            putExtra("memberName", selectedMemberName)
                            putExtra("memberId", selectedMemberId)
                        }
                        startActivity(confirmIntent)
                        // リスト更新
                        bookAdapter.updateBooks(app.bookRepository.findAll())
                    }
                    is BorrowBookResult.Failure -> {
                        Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
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
