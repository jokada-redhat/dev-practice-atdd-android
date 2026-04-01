package com.example.libretta.loan

import com.example.libretta.book.BookRepository
import com.example.libretta.member.MemberRepository
import com.example.libretta.model.Loan
import java.time.LocalDate
import java.util.UUID

data class BorrowBookRequest(val memberId: String, val bookId: String)

sealed class BorrowBookResult {
    data class Success(val loan: Loan) : BorrowBookResult()
    data class Failure(val errorMessage: String) : BorrowBookResult()
}

class BorrowBookUseCase(
    private val loanRepository: LoanRepository,
    private val bookRepository: BookRepository,
    private val memberRepository: MemberRepository
) {
    fun execute(request: BorrowBookRequest): BorrowBookResult {
        // 会員の存在確認
        memberRepository.findById(request.memberId)
            ?: return BorrowBookResult.Failure("会員が見つかりません")

        // 書籍の存在確認
        bookRepository.findById(request.bookId)
            ?: return BorrowBookResult.Failure("書籍が見つかりません")

        // 書籍の貸出状態確認
        if (loanRepository.findActiveByBookId(request.bookId) != null) {
            return BorrowBookResult.Failure("この書籍は既に貸出中です")
        }

        // 貸出記録を作成
        val loan = Loan(
            id = UUID.randomUUID().toString(),
            memberId = request.memberId,
            bookId = request.bookId,
            borrowedDate = LocalDate.now()
        )

        // 貸出記録を保存
        val saveResult = loanRepository.save(loan)
        if (saveResult.isFailure) {
            return BorrowBookResult.Failure(
                saveResult.exceptionOrNull()?.message ?: "貸出記録の保存に失敗しました"
            )
        }

        return BorrowBookResult.Success(loan)
    }
}
