package com.example.libretta.book

import com.example.libretta.model.Book

data class RegisterBookRequest(val title: String, val author: String, val isbn: String, val publicationYear: String)

sealed class RegisterBookResult {
    data class Success(val book: Book) : RegisterBookResult()
    data class ValidationError(val message: String) : RegisterBookResult()
    data class Failure(val errorMessage: String) : RegisterBookResult()
}

class RegisterBookUseCase(private val bookRepository: BookRepository) {

    fun execute(request: RegisterBookRequest): RegisterBookResult {
        if (request.title.isBlank()) {
            return RegisterBookResult.ValidationError("タイトルを入力してください")
        }
        if (request.author.isBlank()) {
            return RegisterBookResult.ValidationError("著者名を入力してください")
        }
        if (request.isbn.isBlank()) {
            return RegisterBookResult.ValidationError("ISBNを入力してください")
        }

        val existingBooks = bookRepository.findAll()
        if (existingBooks.any { it.isbn == request.isbn }) {
            return RegisterBookResult.ValidationError("このISBNは既に登録されています")
        }

        val book = Book(
            id = request.isbn.hashCode().toString(),
            title = request.title,
            author = request.author,
            isbn = request.isbn,
            publicationYear = request.publicationYear
        )

        return when (val result = bookRepository.save(book)) {
            else -> {
                if (result.isSuccess) {
                    RegisterBookResult.Success(result.getOrThrow())
                } else {
                    RegisterBookResult.Failure(result.exceptionOrNull()?.message ?: "登録に失敗しました")
                }
            }
        }
    }
}
