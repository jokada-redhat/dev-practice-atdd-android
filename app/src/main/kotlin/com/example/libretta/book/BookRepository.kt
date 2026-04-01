package com.example.libretta.book

import com.example.libretta.model.Book
import com.example.libretta.model.BookStatus

interface BookRepository {
    fun save(book: Book): Result<Book>
    fun findById(id: String): Book?
    fun findByTitle(title: String): Book?
    fun findAll(): List<Book>
    fun search(query: String): List<Book>
    fun filterByStatus(status: BookStatus): List<Book>
    fun updateStatus(bookId: String, status: BookStatus): Result<Unit>
    fun delete(id: String): Result<Unit>
    fun clear()
}
