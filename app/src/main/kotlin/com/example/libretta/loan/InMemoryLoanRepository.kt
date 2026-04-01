package com.example.libretta.loan

import com.example.libretta.model.Loan

class InMemoryLoanRepository : LoanRepository {
    private val loans = mutableMapOf<String, Loan>()

    override fun save(loan: Loan): Result<Loan> {
        loans[loan.id] = loan
        return Result.success(loan)
    }

    override fun findById(id: String): Loan? = loans[id]

    override fun findByMemberId(memberId: String): List<Loan> = loans.values.filter { it.memberId == memberId }

    override fun findByBookId(bookId: String): Loan? = loans.values.find { it.bookId == bookId }

    override fun findActiveByBookId(bookId: String): Loan? = loans.values.find { it.bookId == bookId }

    override fun findAll(): List<Loan> = loans.values.toList()

    override fun countActiveByMemberId(memberId: String): Int = loans.values.count { it.memberId == memberId }

    override fun findBorrowedBookIds(): Set<String> = loans.values.map { it.bookId }.toSet()

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
