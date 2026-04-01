package com.example.libretta.steps

import com.example.libretta.adapter.LoanedArtifact
import com.example.libretta.book.InMemoryBookRepository
import com.example.libretta.loan.BorrowBookRequest
import com.example.libretta.loan.BorrowBookResult
import com.example.libretta.loan.BorrowBookUseCase
import com.example.libretta.loan.InMemoryLoanRepository
import com.example.libretta.loan.ReturnBookRequest
import com.example.libretta.loan.ReturnBookResult
import com.example.libretta.loan.ReturnBookUseCase
import com.example.libretta.member.InMemoryMemberRepository
import com.example.libretta.model.Book
import com.example.libretta.model.BookStatus
import com.example.libretta.model.Member
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

class ReturnBookSteps {

    private val memberRepository = InMemoryMemberRepository()
    private val bookRepository = InMemoryBookRepository()
    private val loanRepository = InMemoryLoanRepository()

    private val borrowBookUseCase = BorrowBookUseCase(
        loanRepository = loanRepository,
        bookRepository = bookRepository,
        memberRepository = memberRepository
    )

    private val returnBookUseCase = ReturnBookUseCase(
        loanRepository = loanRepository,
        bookRepository = bookRepository,
        memberRepository = memberRepository
    )

    private var allArtifacts: List<LoanedArtifact> = emptyList()
    private var filteredArtifacts: List<LoanedArtifact> = emptyList()

    @Before
    fun setUp() {
        memberRepository.clear()
        bookRepository.clear()
        loanRepository.clear()
        allArtifacts = emptyList()
        filteredArtifacts = emptyList()
    }

    @Given("返却用に会員 {string} \\(ID: {string}) が登録されている")
    fun memberIsRegisteredForReturn(name: String, id: String) {
        memberRepository.save(
            Member(id = id, name = name, email = "$id@example.com", loanCount = 0)
        )
    }

    @And("会員 {string} が書籍 {string} \\(ISBN: {string}) を借りている")
    fun memberHasBorrowedBookWithIsbn(memberId: String, title: String, isbn: String) {
        val book = Book(
            id = title.hashCode().toString(),
            title = title,
            author = "Test Author",
            isbn = isbn,
            publicationYear = "2024",
            status = BookStatus.AVAILABLE
        )
        bookRepository.save(book)
        val result = borrowBookUseCase.execute(BorrowBookRequest(memberId = memberId, bookId = book.id))
        assertTrue("貸出が成功していません", result is BorrowBookResult.Success)
    }

    @When("返却画面を開く")
    fun openReturnScreen() {
        loadArtifacts()
        filteredArtifacts = allArtifacts
    }

    @And("検索ボックスに {string} と入力する")
    fun enterSearchQuery(query: String) {
        filteredArtifacts = filterArtifacts(query)
    }

    @When("検索ボックスをクリアする")
    fun clearSearchBox() {
        filteredArtifacts = allArtifacts
    }

    @Then("貸出一覧に {int} 件表示される")
    fun loanListShowsCount(expectedCount: Int) {
        assertEquals("表示件数が一致しません", expectedCount, filteredArtifacts.size)
    }

    @And("貸出一覧に書籍 {string} が表示される")
    fun loanListContainsBook(title: String) {
        assertTrue("書籍「$title」が一覧にありません", filteredArtifacts.any { it.book.title == title })
    }

    @And("絞り込み結果から書籍 {string} を返却する")
    fun returnBookFromFilteredList(title: String) {
        val artifact = filteredArtifacts.find { it.book.title == title }
        assertNotNull("書籍「$title」が一覧にありません", artifact)
        val result = returnBookUseCase.execute(
            ReturnBookRequest(memberId = artifact!!.member.id, bookId = artifact.book.id)
        )
        assertTrue("返却が成功していません", result is ReturnBookResult.Success)
        loadArtifacts()
        filteredArtifacts = allArtifacts
    }

    @Then("返却後の書籍 {string} のステータスが {string} である")
    fun bookStatusAfterReturn(title: String, statusString: String) {
        val book = bookRepository.findById(title.hashCode().toString())
        assertNotNull("書籍が見つかりません", book)
        val expected = BookStatus.valueOf(statusString.uppercase())
        assertEquals("書籍ステータスが一致しません", expected, book?.status)
    }

    @And("返却後の会員 {string} の貸出冊数が {int} である")
    fun memberLoanCountAfterReturn(memberId: String, loanCount: Int) {
        val member = memberRepository.findById(memberId)
        assertNotNull("会員が見つかりません", member)
        assertEquals("貸出冊数が一致しません", loanCount, member?.loanCount)
    }

    private fun loadArtifacts() {
        val loans = loanRepository.findAll().filter { !it.isReturned }
        allArtifacts = loans.mapNotNull { loan ->
            val book = bookRepository.findById(loan.bookId) ?: return@mapNotNull null
            val member = memberRepository.findById(loan.memberId) ?: return@mapNotNull null
            LoanedArtifact(loan, book, member)
        }
    }

    private fun filterArtifacts(query: String): List<LoanedArtifact> {
        if (query.isBlank()) return allArtifacts
        val q = query.lowercase()
        return allArtifacts.filter {
            it.book.title.lowercase().contains(q) ||
                it.book.isbn.lowercase().contains(q) ||
                it.member.name.lowercase().contains(q) ||
                it.member.id.lowercase().contains(q)
        }
    }
}
