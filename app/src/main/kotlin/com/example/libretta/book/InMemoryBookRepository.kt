package com.example.libretta.book

import com.example.libretta.model.Book
import com.example.libretta.model.BookStatus

class InMemoryBookRepository : BookRepository {
    private val books = mutableMapOf<String, Book>()

    override fun save(book: Book): Result<Book> {
        books[book.id] = book
        return Result.success(book)
    }

    override fun findById(id: String): Book? {
        return books[id]
    }

    override fun findByTitle(title: String): Book? {
        return books.values.find { it.title == title }
    }

    override fun findAll(): List<Book> {
        return books.values.toList()
    }

    override fun search(query: String): List<Book> {
        return books.values.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.author.contains(query, ignoreCase = true) ||
            it.isbn.contains(query, ignoreCase = true)
        }
    }

    override fun filterByStatus(status: BookStatus): List<Book> {
        return books.values.filter { it.status == status }
    }

    override fun updateStatus(bookId: String, status: BookStatus): Result<Unit> {
        val book = books[bookId]
            ?: return Result.failure(NoSuchElementException("書籍が見つかりません"))

        books[bookId] = book.copy(status = status)
        return Result.success(Unit)
    }

    override fun delete(id: String): Result<Unit> {
        if (books.remove(id) == null) {
            return Result.failure(NoSuchElementException("書籍が見つかりません"))
        }
        return Result.success(Unit)
    }

    override fun clear() {
        books.clear()
    }
}
