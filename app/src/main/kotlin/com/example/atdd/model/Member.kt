package com.example.atdd.model

data class Member(
    val id: String,
    val name: String,
    val loanCount: Int = 0
)
