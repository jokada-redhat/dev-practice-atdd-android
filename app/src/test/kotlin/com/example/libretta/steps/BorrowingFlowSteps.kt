package com.example.libretta.steps

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
import com.example.libretta.model.Member
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

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
            name = name
        )
        memberRepository.save(member)
    }

    @And("書籍 {string} が登録されている")
    fun bookIsRegistered(title: String) {
        val book = Book(
            id = title.hashCode().toString(),
            title = title,
            author = "Test Author",
            isbn = "978-0000000000",
            publicationYear = "2024"
        )
        bookRepository.save(book)
    }

    @And("会員 {string} が {int} 冊借りている状態である")
    fun memberHasBorrowedNBooks(memberId: String, count: Int) {
        for (i in 1..count) {
            val title = "Dummy Book $i"
            bookIsRegistered(title)
            memberBorrowsBook(memberId, title)
        }
    }

    @When("会員 {string} が書籍 {string} を借りる")
    fun memberBorrowsBook(memberId: String, title: String) {
        val bookId = title.hashCode().toString()
        val request = BorrowBookRequest(memberId = memberId, bookId = bookId)
        borrowResult = borrowBookUseCase.execute(request)
    }

    @Then("書籍 {string} は貸出中である")
    fun bookIsBorrowed(title: String) {
        val bookId = title.hashCode().toString()
        val activeLoan = loanRepository.findActiveByBookId(bookId)
        assertNotNull("書籍が貸出中ではありません", activeLoan)
    }

    @Then("書籍 {string} は貸出可能である")
    fun bookIsAvailableAfterAction(title: String) {
        val bookId = title.hashCode().toString()
        val activeLoan = loanRepository.findActiveByBookId(bookId)
        assertNull("書籍がまだ貸出中です", activeLoan)
    }

    @And("会員 {string} の貸出冊数が {int} になる")
    fun memberLoanCountBecomes(memberId: String, loanCount: Int) {
        val member = memberRepository.findById(memberId)
        assertNotNull("会員が見つかりません", member)
        val actualCount = loanRepository.countActiveByMemberId(memberId)
        assertEquals("貸出冊数が一致しません", loanCount, actualCount)
    }

    @And("貸出記録が作成される")
    fun loanRecordIsCreated() {
        val loans = loanRepository.findAll()
        assertTrue("貸出記録が作成されていません", loans.isNotEmpty())
        when (val result = borrowResult) {
            is BorrowBookResult.Success -> {
                val loan = loans.find { it.id == result.loan.id }
                assertNotNull("貸出記録が見つかりません", loan)
            }

            else -> fail("貸出が成功していません")
        }
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

    @And("会員 {string} が書籍 {string} を既に借りている")
    fun memberHasAlreadyBorrowedBook(memberId: String, title: String) {
        // 書籍を登録
        bookIsRegistered(title)

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

    @And("貸出記録が削除される")
    fun loanRecordIsDeleted() {
        when (val result = returnResult) {
            is ReturnBookResult.Success -> {
                val loan = loanRepository.findById(result.loan.id)
                assertNull("貸出記録が削除されていません", loan)
            }

            else -> fail("返却が成功していません")
        }
    }

    @When("会員 {string} が書籍 {string} を返却しようとする")
    fun memberTriesToReturnBook(memberId: String, title: String) {
        val bookId = title.hashCode().toString()
        val request = ReturnBookRequest(memberId = memberId, bookId = bookId)
        returnResult = returnBookUseCase.execute(request)

        when (val result = returnResult) {
            is ReturnBookResult.Failure -> {
                CommonSteps.lastErrorMessage = result.errorMessage
            }

            else -> {}
        }
    }

    @When("存在しない会員 {string} が書籍 {string} を借りようとする")
    fun nonExistentMemberTriesToBorrow(memberId: String, title: String) {
        val bookId = title.hashCode().toString()
        val request = BorrowBookRequest(memberId = memberId, bookId = bookId)
        borrowResult = borrowBookUseCase.execute(request)

        when (val result = borrowResult) {
            is BorrowBookResult.Failure -> {
                CommonSteps.lastErrorMessage = result.errorMessage
            }

            else -> {}
        }
    }

    @When("会員 {string} が存在しない書籍を借りようとする")
    fun memberTriesToBorrowNonExistentBook(memberId: String) {
        val request = BorrowBookRequest(memberId = memberId, bookId = "non-existent-id")
        borrowResult = borrowBookUseCase.execute(request)

        when (val result = borrowResult) {
            is BorrowBookResult.Failure -> {
                CommonSteps.lastErrorMessage = result.errorMessage
            }

            else -> {}
        }
    }

    // --- 貸出一覧からの返却 ---

    @When("貸出一覧で書籍 {string} の返却ボタンを押す")
    fun returnBookFromList(title: String) {
        val bookId = title.hashCode().toString()
        val activeLoan = loanRepository.findActiveByBookId(bookId)
        assertNotNull("アクティブな貸出記録が見つかりません", activeLoan)

        val request = ReturnBookRequest(memberId = activeLoan!!.memberId, bookId = bookId)
        returnResult = returnBookUseCase.execute(request)
    }

    @Then("貸出一覧から書籍 {string} が消える")
    fun bookDisappearsFromLoanedList(title: String) {
        val bookId = title.hashCode().toString()
        val activeLoans = loanRepository.findAll()
        val found = activeLoans.any { it.bookId == bookId }
        assertFalse("書籍がまだ貸出一覧に残っています", found)
    }

    @Then("貸出一覧の件数表示が {string} になる")
    fun loanedListCountDisplayIs(expectedText: String) {
        val activeLoans = loanRepository.findAll()
        val actualText = when (activeLoans.size) {
            0 -> "0冊 貸し出し中"
            1 -> "1冊 貸し出し中"
            else -> "${activeLoans.size}冊 貸し出し中"
        }
        assertEquals("件数表示が一致しません", expectedText, actualText)
    }

    @Then("貸出一覧に {string} と表示される")
    fun loanedListShowsMessage(expectedMessage: String) {
        val activeLoans = loanRepository.findAll()
        assertTrue("貸出一覧が空ではありません", activeLoans.isEmpty())
    }
}
