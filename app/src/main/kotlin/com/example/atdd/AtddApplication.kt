package com.example.atdd

import android.app.Application
import com.example.atdd.book.BookRepository
import com.example.atdd.book.InMemoryBookRepository
import com.example.atdd.loan.InMemoryLoanRepository
import com.example.atdd.loan.LoanRepository
import com.example.atdd.member.InMemoryMemberRepository
import com.example.atdd.member.MemberRepository
import com.example.atdd.model.Book
import com.example.atdd.model.BookStatus
import com.example.atdd.model.Member
import okhttp3.OkHttpClient

class AtddApplication : Application() {
    var baseUrl: String = "https://production.example.com"
    val okHttpClient: OkHttpClient = OkHttpClient()

    val memberRepository: MemberRepository = InMemoryMemberRepository()
    val bookRepository: BookRepository = InMemoryBookRepository()
    val loanRepository: LoanRepository = InMemoryLoanRepository()

    override fun onCreate() {
        super.onCreate()
        loadSampleData()
    }

    private fun loadSampleData() {
        memberRepository.save(Member("DA-8821", "Taro Yamada", "taro@example.com", loanCount = 2))
        memberRepository.save(Member("DA-1156", "Marcus Thorne", "marcus@example.com", loanCount = 0))
        memberRepository.save(Member("DA-5509", "Julian Chen", "julian@example.com", loanCount = 1))

        bookRepository.save(Book("1", "The Infinite Library", "Jorge Luis Borges", "978-0142437889", "1941", BookStatus.AVAILABLE))
        bookRepository.save(Book("2", "Neuromancer", "William Gibson", "978-0441569595", "1984", BookStatus.BORROWED))
        bookRepository.save(Book("3", "The Left Hand of Darkness", "Ursula K. Le Guin", "978-0441478125", "1969", BookStatus.AVAILABLE))
        bookRepository.save(Book("4", "Foundation", "Isaac Asimov", "978-0553293357", "1951", BookStatus.AVAILABLE))
    }
}
