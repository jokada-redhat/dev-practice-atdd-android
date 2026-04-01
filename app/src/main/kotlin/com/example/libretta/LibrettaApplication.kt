package com.example.libretta

import android.app.Application
import com.example.libretta.book.BookRepository
import com.example.libretta.book.InMemoryBookRepository
import com.example.libretta.debug.DummyDataGenerator
import com.example.libretta.loan.InMemoryLoanRepository
import com.example.libretta.loan.LoanRepository
import com.example.libretta.member.InMemoryMemberRepository
import com.example.libretta.member.MemberRepository
import okhttp3.OkHttpClient

class LibrettaApplication : Application() {
    var baseUrl: String = "https://production.example.com"
    val okHttpClient: OkHttpClient = OkHttpClient()

    val memberRepository: MemberRepository = InMemoryMemberRepository()
    val bookRepository: BookRepository = InMemoryBookRepository()
    val loanRepository: LoanRepository = InMemoryLoanRepository()

    override fun onCreate() {
        super.onCreate()
        DummyDataGenerator(memberRepository, bookRepository, loanRepository).loadDummyData()
    }
}
