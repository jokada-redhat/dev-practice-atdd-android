package com.example.atdd.model

data class Book(
    val title: String,
    val author: String,
    val isbn: String,
    val publicationYear: String,
    val isAvailable: Boolean = true
)
