package com.example.atdd.book

import com.example.atdd.model.Book
import com.example.atdd.model.BookStatus

class SearchBooksUseCase(
    private val bookRepository: BookRepository
) {
    fun execute(): List<Book> {
        return bookRepository.findAll()
    }

    fun search(query: String): List<Book> {
        if (query.isBlank()) {
            return execute()
        }
        return bookRepository.search(query)
    }

    fun filterByStatus(status: BookStatus): List<Book> {
        return bookRepository.filterByStatus(status)
    }

    fun filterByStatus(statusString: String): List<Book> {
        val status = when (statusString.uppercase()) {
            "AVAILABLE" -> BookStatus.AVAILABLE
            "BORROWED" -> BookStatus.BORROWED
            else -> return execute()
        }
        return filterByStatus(status)
    }
}
