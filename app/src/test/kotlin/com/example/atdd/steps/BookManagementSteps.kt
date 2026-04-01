package com.example.atdd.steps

import com.example.atdd.book.InMemoryBookRepository
import com.example.atdd.book.RegisterBookRequest
import com.example.atdd.book.RegisterBookResult
import com.example.atdd.book.RegisterBookUseCase
import com.example.atdd.model.Book
import com.example.atdd.model.BookStatus
import io.cucumber.datatable.DataTable
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*

class BookManagementSteps {

    private val bookRepository = InMemoryBookRepository()
    private val registerBookUseCase = RegisterBookUseCase(bookRepository)

    private var allBooks: List<Book> = emptyList()
    private var filteredBooks: List<Book> = emptyList()
    private var registerResult: RegisterBookResult? = null

    @Before
    fun setUp() {
        bookRepository.clear()
        allBooks = emptyList()
        filteredBooks = emptyList()
        registerResult = null
    }

    @Given("書籍管理に以下の書籍が登録されている:")
    fun booksAreRegistered(dataTable: DataTable) {
        dataTable.asMaps().forEach { row ->
            val book = Book(
                id = row["isbn"]!!.hashCode().toString(),
                title = row["title"]!!,
                author = row["author"]!!,
                isbn = row["isbn"]!!,
                publicationYear = row["year"] ?: "2024",
                status = BookStatus.AVAILABLE
            )
            bookRepository.save(book)
        }
    }

    @When("書籍一覧を表示する")
    fun showBookList() {
        allBooks = bookRepository.findAll()
        filteredBooks = allBooks
    }

    @And("書籍一覧で {string} と検索する")
    fun searchBooks(query: String) {
        filteredBooks = bookRepository.search(query)
    }

    @Then("書籍一覧に {int} 件表示される")
    fun bookListShowsCount(expectedCount: Int) {
        assertEquals("表示件数が一致しません", expectedCount, filteredBooks.size)
    }

    @And("書籍一覧に書籍 {string} が含まれる")
    fun bookListContains(title: String) {
        assertTrue("書籍「$title」が一覧にありません", filteredBooks.any { it.title == title })
    }

    @When("書籍を登録する:")
    fun registerBook(dataTable: DataTable) {
        val row = dataTable.asMaps().first()
        val request = RegisterBookRequest(
            title = row["title"]!!,
            author = row["author"]!!,
            isbn = row["isbn"]!!,
            publicationYear = row["year"] ?: "2024"
        )
        registerResult = registerBookUseCase.execute(request)
    }

    @Then("書籍 {string} が書籍一覧に存在する")
    fun bookExistsInList(title: String) {
        val book = bookRepository.findByTitle(title)
        assertNotNull("書籍「$title」が見つかりません", book)
    }

    @And("書籍 {string} の著者が {string} である")
    fun bookAuthorIs(title: String, expectedAuthor: String) {
        val book = bookRepository.findByTitle(title)
        assertEquals("著者が一致しません", expectedAuthor, book?.author)
    }

    @And("書籍 {string} のISBNが {string} である")
    fun bookIsbnIs(title: String, expectedIsbn: String) {
        val book = bookRepository.findByTitle(title)
        assertEquals("ISBNが一致しません", expectedIsbn, book?.isbn)
    }

    @When("タイトル未入力で書籍を登録しようとする")
    fun registerBookWithoutTitle() {
        registerResult = registerBookUseCase.execute(
            RegisterBookRequest(title = "", author = "Author", isbn = "978-0000000000", publicationYear = "2024")
        )
    }

    @When("重複ISBNで書籍を登録しようとする:")
    fun registerBookWithDuplicateIsbn(dataTable: DataTable) {
        val row = dataTable.asMaps().first()
        registerResult = registerBookUseCase.execute(
            RegisterBookRequest(
                title = row["title"]!!,
                author = row["author"]!!,
                isbn = row["isbn"]!!,
                publicationYear = row["year"] ?: "2024"
            )
        )
    }

    @Then("書籍登録エラー {string} が表示される")
    fun bookRegistrationErrorShown(expectedMessage: String) {
        when (val result = registerResult) {
            is RegisterBookResult.ValidationError -> assertEquals(expectedMessage, result.message)
            is RegisterBookResult.Failure -> assertEquals(expectedMessage, result.errorMessage)
            else -> fail("エラーが発生していません")
        }
    }
}
