package com.example.libretta.book

import com.example.libretta.loan.LoanRepository
import com.example.libretta.model.Book

class SearchBooksUseCase(private val bookRepository: BookRepository, private val loanRepository: LoanRepository) {
    fun execute(): List<Book> = bookRepository.findAll()

    fun search(query: String): List<Book> {
        if (query.isBlank()) {
            return execute()
        }
        return bookRepository.search(query)
    }

    fun filterByStatus(statusString: String): List<Book> {
        val borrowedBookIds = loanRepository.findBorrowedBookIds()
        return when (statusString.uppercase()) {
            "AVAILABLE" -> bookRepository.findAll().filter { it.id !in borrowedBookIds }
            "BORROWED" -> bookRepository.findAll().filter { it.id in borrowedBookIds }
            else -> execute()
        }
    }
}
