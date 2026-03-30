package com.example.atdd.model

data class Member(
    val id: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val address: String = "",
    val loanCount: Int = 0
)
