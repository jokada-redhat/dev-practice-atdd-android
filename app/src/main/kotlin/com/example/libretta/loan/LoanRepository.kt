package com.example.libretta.loan

import com.example.libretta.model.Loan

interface LoanRepository {
    fun save(loan: Loan): Result<Loan>
    fun findById(id: String): Loan?
    fun findByMemberId(memberId: String): List<Loan>
    fun findByBookId(bookId: String): Loan?
    fun findActiveByBookId(bookId: String): Loan?
    fun findAll(): List<Loan>
    fun delete(id: String): Result<Unit>
    fun countActiveByMemberId(memberId: String): Int
    fun findBorrowedBookIds(): Set<String>
    fun clear()
}
