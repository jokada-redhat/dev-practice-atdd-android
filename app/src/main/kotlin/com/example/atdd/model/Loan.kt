package com.example.atdd.model

import java.time.LocalDate

data class Loan(
    val id: String,
    val memberId: String,
    val bookId: String,
    val borrowedDate: LocalDate,
    val returnedDate: LocalDate? = null
) {
    val isReturned: Boolean
        get() = returnedDate != null
}
