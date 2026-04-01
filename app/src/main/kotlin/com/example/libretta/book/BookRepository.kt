package com.example.libretta.book

import com.example.libretta.model.Book

interface BookRepository {
    fun save(book: Book): Result<Book>
    fun findById(id: String): Book?
    fun findByTitle(title: String): Book?
    fun findAll(): List<Book>
    fun search(query: String): List<Book>
    fun delete(id: String): Result<Unit>
    fun clear()
}
