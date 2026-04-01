package com.example.libretta.book

import com.example.libretta.model.Book
import com.example.libretta.model.BookStatus

class SearchBooksUseCase(private val bookRepository: BookRepository) {
    fun execute(): List<Book> = bookRepository.findAll()

    fun search(query: String): List<Book> {
        if (query.isBlank()) {
            return execute()
        }
        return bookRepository.search(query)
    }

    fun filterByStatus(status: BookStatus): List<Book> = bookRepository.filterByStatus(status)

    fun filterByStatus(statusString: String): List<Book> {
        val status = when (statusString.uppercase()) {
            "AVAILABLE" -> BookStatus.AVAILABLE
            "BORROWED" -> BookStatus.BORROWED
            else -> return execute()
        }
        return filterByStatus(status)
    }
}
