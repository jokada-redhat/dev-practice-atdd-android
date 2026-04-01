package com.example.atdd

import android.app.Application
import com.example.atdd.book.BookRepository
import com.example.atdd.book.InMemoryBookRepository
import com.example.atdd.debug.DummyDataGenerator
import com.example.atdd.loan.InMemoryLoanRepository
import com.example.atdd.loan.LoanRepository
import com.example.atdd.member.InMemoryMemberRepository
import com.example.atdd.member.MemberRepository
import okhttp3.OkHttpClient

class AtddApplication : Application() {
    var baseUrl: String = "https://production.example.com"
    val okHttpClient: OkHttpClient = OkHttpClient()

    var skipAuth: Boolean = BuildConfig.SKIP_AUTH

    val memberRepository: MemberRepository = InMemoryMemberRepository()
    val bookRepository: BookRepository = InMemoryBookRepository()
    val loanRepository: LoanRepository = InMemoryLoanRepository()

    override fun onCreate() {
        super.onCreate()
        DummyDataGenerator(memberRepository, bookRepository, loanRepository).loadDummyData()
    }
}
