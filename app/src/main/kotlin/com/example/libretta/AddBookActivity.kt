package com.example.libretta

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.libretta.book.RegisterBookRequest
import com.example.libretta.book.RegisterBookResult
import com.example.libretta.book.RegisterBookUseCase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddBookActivity : AppCompatActivity() {

    private val app by lazy { application as LibrettaApplication }
    private val registerBookUseCase by lazy { RegisterBookUseCase(app.bookRepository) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_book)

        setupToolbar()
        setupForm()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupForm() {
        val editTitle = findViewById<TextInputEditText>(R.id.editBookTitle)
        val editAuthor = findViewById<TextInputEditText>(R.id.editBookAuthor)
        val editIsbn = findViewById<TextInputEditText>(R.id.editBookIsbn)
        val editYear = findViewById<TextInputEditText>(R.id.editBookYear)
        val buttonAdd = findViewById<MaterialButton>(R.id.buttonAddBook)

        buttonAdd.setOnClickListener {
            val request = RegisterBookRequest(
                title = editTitle.text.toString().trim(),
                author = editAuthor.text.toString().trim(),
                isbn = editIsbn.text.toString().trim(),
                publicationYear = editYear.text.toString().trim()
            )

            when (val result = registerBookUseCase.execute(request)) {
                is RegisterBookResult.Success -> {
                    Toast.makeText(this, getString(R.string.book_added_success), Toast.LENGTH_LONG).show()
                    finish()
                }

                is RegisterBookResult.ValidationError -> {
                    editTitle.error = result.message
                }

                is RegisterBookResult.Failure -> {
                    Toast.makeText(this, result.errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
