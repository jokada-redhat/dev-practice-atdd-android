package com.example.atdd.model

enum class BookStatus {
    AVAILABLE,
    BORROWED
}

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val isbn: String,
    val publicationYear: String,
    val status: BookStatus = BookStatus.AVAILABLE
) {
    val isAvailable: Boolean
        get() = status == BookStatus.AVAILABLE
}
