package com.example.atdd.steps

import com.example.atdd.book.InMemoryBookRepository
import com.example.atdd.loan.BorrowBookRequest
import com.example.atdd.loan.BorrowBookResult
import com.example.atdd.loan.BorrowBookUseCase
import com.example.atdd.loan.InMemoryLoanRepository
import com.example.atdd.loan.ReturnBookRequest
import com.example.atdd.loan.ReturnBookResult
import com.example.atdd.loan.ReturnBookUseCase
import com.example.atdd.member.InMemoryMemberRepository
import com.example.atdd.model.Book
import com.example.atdd.model.BookStatus
import com.example.atdd.model.Member
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.*

class BorrowingFlowSteps {

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

    private var borrowResult: BorrowBookResult? = null
    private var returnResult: ReturnBookResult? = null
    private var errorMessage: String? = null

    @Before
    fun setUp() {
        memberRepository.clear()
        bookRepository.clear()
        loanRepository.clear()
        borrowResult = null
        returnResult = null
        errorMessage = null
    }

    @Given("会員 {string} \\(ID: {string}) が登録されている")
    fun memberIsRegistered(name: String, id: String) {
        val member = Member(
            id = id,
            name = name,
            email = "${id}@example.com",
            loanCount = 0
        )
        memberRepository.save(member)
    }

    @And("会員 {string} の貸出冊数は {int} である")
    fun memberLoanCountIs(memberId: String, loanCount: Int) {
        val member = memberRepository.findById(memberId)
        assertNotNull("会員が見つかりません", member)
        assertEquals("貸出冊数が一致しません", loanCount, member?.loanCount)
    }

    @And("書籍 {string} が貸出可能である")
    fun bookIsAvailable(title: String) {
        val book = Book(
            id = title.hashCode().toString(),
            title = title,
            author = "Test Author",
            isbn = "978-0000000000",
            publicationYear = "2024",
            status = BookStatus.AVAILABLE
        )
        bookRepository.save(book)
    }

    @When("会員 {string} が書籍 {string} を借りる")
    fun memberBorrowsBook(memberId: String, title: String) {
        val bookId = title.hashCode().toString()
        val request = BorrowBookRequest(memberId = memberId, bookId = bookId)
        borrowResult = borrowBookUseCase.execute(request)
    }

    @Then("書籍 {string} のステータスが {string} になる")
    fun bookStatusIs(title: String, statusString: String) {
        val bookId = title.hashCode().toString()
        val book = bookRepository.findById(bookId)
        assertNotNull("書籍が見つかりません", book)

        val expectedStatus = when (statusString.uppercase()) {
            "AVAILABLE" -> BookStatus.AVAILABLE
            "BORROWED" -> BookStatus.BORROWED
            else -> fail("不正なステータス: $statusString")
        }

        assertEquals("書籍ステータスが一致しません", expectedStatus, book?.status)
    }

    @And("会員 {string} の貸出冊数が {int} になる")
    fun memberLoanCountBecomes(memberId: String, loanCount: Int) {
        val member = memberRepository.findById(memberId)
        assertNotNull("会員が見つかりません", member)
        assertEquals("貸出冊数が一致しません", loanCount, member?.loanCount)
    }

    @And("貸出記録が作成される")
    fun loanRecordIsCreated() {
        val loans = loanRepository.findAll()
        assertTrue("貸出記録が作成されていません", loans.isNotEmpty())
        when (val result = borrowResult) {
            is BorrowBookResult.Success -> {
                val loan = loans.find { it.id == result.loan.id }
                assertNotNull("貸出記録が見つかりません", loan)
                assertNull("返却日が設定されていないべき", loan?.returnedDate)
            }
            else -> fail("貸出が成功していません")
        }
    }

    @And("書籍 {string} が既に借りられている")
    fun bookIsAlreadyBorrowed(title: String) {
        val book = Book(
            id = title.hashCode().toString(),
            title = title,
            author = "Test Author",
            isbn = "978-0000000000",
            publicationYear = "2024",
            status = BookStatus.BORROWED
        )
        bookRepository.save(book)
    }

    @When("会員 {string} が書籍 {string} を借りようとする")
    fun memberTriesToBorrowBook(memberId: String, title: String) {
        val bookId = title.hashCode().toString()
        val request = BorrowBookRequest(memberId = memberId, bookId = bookId)
        borrowResult = borrowBookUseCase.execute(request)

        // エラーメッセージを共通変数に設定
        when (val result = borrowResult) {
            is BorrowBookResult.Failure -> {
                CommonSteps.lastErrorMessage = result.errorMessage
            }
            else -> {}
        }
    }


    @And("書籍 {string} のステータスが {string} のまま変わらない")
    fun bookStatusRemainsUnchanged(title: String, statusString: String) {
        bookStatusIs(title, statusString)
    }

    @And("会員 {string} が書籍 {string} を既に借りている")
    fun memberHasAlreadyBorrowedBook(memberId: String, title: String) {
        // 書籍を登録
        bookIsAvailable(title)

        // 貸出処理
        memberBorrowsBook(memberId, title)

        // 貸出成功確認
        when (borrowResult) {
            is BorrowBookResult.Success -> {
                // OK
            }
            else -> fail("貸出が成功していません")
        }
    }

    @When("会員 {string} が書籍 {string} を返却する")
    fun memberReturnsBook(memberId: String, title: String) {
        val bookId = title.hashCode().toString()
        val request = ReturnBookRequest(memberId = memberId, bookId = bookId)
        returnResult = returnBookUseCase.execute(request)
    }

    @And("貸出記録の返却日が記録される")
    fun loanRecordHasReturnDate() {
        when (val result = returnResult) {
            is ReturnBookResult.Success -> {
                assertNotNull("返却日が記録されていません", result.loan.returnedDate)
            }
            else -> fail("返却が成功していません")
        }
    }

    @When("会員 {string} が書籍 {string} を返却しようとする")
    fun memberTriesToReturnBook(memberId: String, title: String) {
        val bookId = title.hashCode().toString()
        val request = ReturnBookRequest(memberId = memberId, bookId = bookId)
        returnResult = returnBookUseCase.execute(request)

        // エラーメッセージを共通変数に設定
        when (val result = returnResult) {
            is ReturnBookResult.Failure -> {
                CommonSteps.lastErrorMessage = result.errorMessage
            }
            else -> {}
        }
    }
}
