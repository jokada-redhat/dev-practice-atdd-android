package com.example.libretta.loan

import com.example.libretta.model.Loan
import java.time.LocalDate

class InMemoryLoanRepository : LoanRepository {
    private val loans = mutableMapOf<String, Loan>()

    override fun save(loan: Loan): Result<Loan> {
        loans[loan.id] = loan
        return Result.success(loan)
    }

    override fun findById(id: String): Loan? {
        return loans[id]
    }

    override fun findByMemberId(memberId: String): List<Loan> {
        return loans.values.filter { it.memberId == memberId }
    }

    override fun findByBookId(bookId: String): Loan? {
        return loans.values.find { it.bookId == bookId }
    }

    override fun findActiveByBookId(bookId: String): Loan? {
        return loans.values.find { it.bookId == bookId && !it.isReturned }
    }

    override fun findAll(): List<Loan> {
        return loans.values.toList()
    }

    override fun returnBook(loanId: String, returnedDate: LocalDate): Result<Loan> {
        val loan = loans[loanId]
            ?: return Result.failure(NoSuchElementException("貸出記録が見つかりません"))

        if (loan.isReturned) {
            return Result.failure(IllegalStateException("この書籍は既に返却済みです"))
        }

        val updatedLoan = loan.copy(returnedDate = returnedDate)
        loans[loanId] = updatedLoan
        return Result.success(updatedLoan)
    }

    override fun delete(id: String): Result<Unit> {
        if (loans.remove(id) == null) {
            return Result.failure(NoSuchElementException("貸出記録が見つかりません"))
        }
        return Result.success(Unit)
    }

    override fun clear() {
        loans.clear()
    }
}
