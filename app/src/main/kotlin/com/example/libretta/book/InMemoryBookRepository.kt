package com.example.libretta.book

import com.example.libretta.model.Book

class InMemoryBookRepository : BookRepository {
    private val books = mutableMapOf<String, Book>()

    override fun save(book: Book): Result<Book> {
        books[book.id] = book
        return Result.success(book)
    }

    override fun findById(id: String): Book? = books[id]

    override fun findByTitle(title: String): Book? = books.values.find { it.title == title }

    override fun findAll(): List<Book> = books.values.toList()

    override fun search(query: String): List<Book> = books.values.filter {
        it.title.contains(query, ignoreCase = true) ||
            it.author.contains(query, ignoreCase = true) ||
            it.isbn.contains(query, ignoreCase = true)
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
