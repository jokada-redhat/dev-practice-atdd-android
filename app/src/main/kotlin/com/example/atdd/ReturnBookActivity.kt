package com.example.atdd

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.atdd.loan.ReturnBookRequest
import com.example.atdd.loan.ReturnBookResult
import com.example.atdd.loan.ReturnBookUseCase
import com.example.atdd.model.Book
import com.example.atdd.model.Member
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class ReturnBookActivity : AppCompatActivity() {

    private val app by lazy { application as AtddApplication }
    private val returnBookUseCase by lazy {
        ReturnBookUseCase(app.loanRepository, app.bookRepository, app.memberRepository)
    }

    private var foundBook: Book? = null
    private var foundMember: Member? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_return_book)

        setupToolbar()
        setupIsbnEntry()
        setupMemberIdEntry()
        setupReturnButton()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupIsbnEntry() {
        val editIsbn = findViewById<TextInputEditText>(R.id.editIsbn)
        val isbnLayout = editIsbn.parent.parent as TextInputLayout

        // endIcon（検索アイコン）タップで検索
        isbnLayout.setEndIconOnClickListener { searchBook() }

        // IME の Search アクションで検索
        editIsbn.setOnEditorActionListener { _, _, _ ->
            searchBook()
            true
        }
    }

    private fun searchBook() {
        val editIsbn = findViewById<TextInputEditText>(R.id.editIsbn)
        val cardFoundBook = findViewById<MaterialCardView>(R.id.cardFoundBook)
        val isbn = editIsbn.text.toString().trim()

        if (isbn.isEmpty()) return

        val books = app.bookRepository.search(isbn)
        foundBook = books.firstOrNull()
        if (foundBook != null) {
            findViewById<TextView>(R.id.textFoundBookTitle).text = foundBook!!.title
            findViewById<TextView>(R.id.textFoundBookAuthor).text = foundBook!!.author
            cardFoundBook.visibility = View.VISIBLE
        } else {
            cardFoundBook.visibility = View.GONE
            Toast.makeText(this, R.string.book_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMemberIdEntry() {
        val editMemberId = findViewById<TextInputEditText>(R.id.editMemberId)
        val memberIdLayout = editMemberId.parent.parent as TextInputLayout

        // endIcon（検索アイコン）タップで検索
        memberIdLayout.setEndIconOnClickListener { searchMember() }

        // IME の Search アクションで検索
        editMemberId.setOnEditorActionListener { _, _, _ ->
            searchMember()
            true
        }
    }

    private fun searchMember() {
        val editMemberId = findViewById<TextInputEditText>(R.id.editMemberId)
        val cardFoundMember = findViewById<MaterialCardView>(R.id.cardFoundMember)
        val memberId = editMemberId.text.toString().trim()

        if (memberId.isEmpty()) return

        foundMember = app.memberRepository.findById(memberId)
        if (foundMember != null) {
            val initials = foundMember!!.name.split(" ")
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")
            findViewById<TextView>(R.id.textMemberInitials).text = initials
            findViewById<TextView>(R.id.textFoundMemberName).text = foundMember!!.name
            findViewById<TextView>(R.id.textFoundMemberId).text =
                getString(R.string.member_id_prefix, foundMember!!.id)
            cardFoundMember.visibility = View.VISIBLE
        } else {
            cardFoundMember.visibility = View.GONE
            Toast.makeText(this, R.string.member_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupReturnButton() {
        val buttonReturn = findViewById<MaterialButton>(R.id.buttonReturn)
        val layoutSuccess = findViewById<LinearLayout>(R.id.layoutSuccess)
        val buttonContinue = findViewById<MaterialButton>(R.id.buttonContinueReturn)
        val buttonDone = findViewById<MaterialButton>(R.id.buttonReturnDone)

        buttonReturn.setOnClickListener {
            val book = foundBook
            val member = foundMember

            if (book == null || member == null) {
                Toast.makeText(this, R.string.return_both_required, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = ReturnBookRequest(memberId = member.id, bookId = book.id)
            when (val result = returnBookUseCase.execute(request)) {
                is ReturnBookResult.Success -> {
                    buttonReturn.visibility = View.GONE
                    layoutSuccess.visibility = View.VISIBLE
                }
                is ReturnBookResult.Failure -> {
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }

        buttonContinue.setOnClickListener {
            resetForm()
        }

        buttonDone.setOnClickListener {
            finish()
        }
    }

    private fun resetForm() {
        foundBook = null
        foundMember = null
        findViewById<TextInputEditText>(R.id.editIsbn).setText("")
        findViewById<TextInputEditText>(R.id.editMemberId).setText("")
        findViewById<MaterialCardView>(R.id.cardFoundBook).visibility = View.GONE
        findViewById<MaterialCardView>(R.id.cardFoundMember).visibility = View.GONE
        findViewById<MaterialButton>(R.id.buttonReturn).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.layoutSuccess).visibility = View.GONE
    }
}
