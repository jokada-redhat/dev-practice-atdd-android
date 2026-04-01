package com.example.libretta.model

import java.time.LocalDate

data class Loan(val id: String, val memberId: String, val bookId: String, val borrowedDate: LocalDate)
