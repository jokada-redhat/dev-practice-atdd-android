package com.example.libretta.steps

import com.example.libretta.book.InMemoryBookRepository
import com.example.libretta.book.SearchBooksUseCase
import com.example.libretta.model.Book
import com.example.libretta.model.BookStatus
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.datatable.DataTable
import org.junit.Assert.*

class BookCatalogSteps {

    private val bookRepository = InMemoryBookRepository()
    private val searchBooksUseCase = SearchBooksUseCase(bookRepository)

    private var bookList: List<Book> = emptyList()
    private var searchResults: List<Book> = emptyList()

    @Before
    fun setUp() {
        bookRepository.clear()
        bookList = emptyList()
        searchResults = emptyList()
    }

    @Given("以下の書籍が登録されている:")
    fun booksAreRegistered(dataTable: DataTable) {
        val books = dataTable.asMaps()
        for (row in books) {
            val status = when (row["status"]?.uppercase()) {
                "AVAILABLE" -> BookStatus.AVAILABLE
                "BORROWED" -> BookStatus.BORROWED
                else -> BookStatus.AVAILABLE
            }

            val book = Book(
                id = row["title"]!!.hashCode().toString(), // IDは title から生成
                title = row["title"]!!,
                author = row["author"]!!,
                isbn = row["isbn"]!!,
                publicationYear = row["publicationYear"]!!,
                status = status
            )
            bookRepository.save(book)
        }
    }

    @When("書籍一覧を取得する")
    fun getBookList() {
        bookList = searchBooksUseCase.execute()
    }

    @Then("書籍リストに {int} 件の書籍が含まれている")
    fun bookListContainsCount(count: Int) {
        assertEquals("書籍数が一致しません", count, bookList.size)
    }

    @When("書籍一覧を {string} でフィルタする")
    fun filterBooksByStatus(statusString: String) {
        bookList = searchBooksUseCase.filterByStatus(statusString)
    }

    @And("書籍リストに {string} が含まれていない")
    fun bookListDoesNotContain(title: String) {
        assertFalse(
            "書籍リストに $title が含まれていないべき",
            bookList.any { it.title == title }
        )
    }

    @And("書籍リストに {string} が含まれている")
    fun bookListContains(title: String) {
        assertTrue(
            "書籍リストに $title が含まれているべき",
            bookList.any { it.title == title }
        )
    }

    @When("書籍を {string} で検索する")
    fun searchBooks(query: String) {
        searchResults = searchBooksUseCase.search(query)
    }

    @Then("検索結果に {int} 件の書籍が含まれている")
    fun searchResultsContainCount(count: Int) {
        assertEquals("検索結果数が一致しません", count, searchResults.size)
    }

    @And("書籍検索結果に {string} が含まれている")
    fun bookSearchResultsContain(title: String) {
        assertTrue(
            "検索結果に $title が含まれているべき",
            searchResults.any { it.title == title }
        )
    }
}
