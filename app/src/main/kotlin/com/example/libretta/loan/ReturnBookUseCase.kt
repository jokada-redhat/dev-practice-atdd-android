package com.example.libretta.loan

import com.example.libretta.book.BookRepository
import com.example.libretta.member.MemberRepository
import com.example.libretta.model.Loan

data class ReturnBookRequest(val memberId: String, val bookId: String)

sealed class ReturnBookResult {
    data class Success(val loan: Loan) : ReturnBookResult()
    data class Failure(val errorMessage: String) : ReturnBookResult()
}

class ReturnBookUseCase(
    private val loanRepository: LoanRepository,
    private val bookRepository: BookRepository,
    private val memberRepository: MemberRepository
) {
    fun execute(request: ReturnBookRequest): ReturnBookResult {
        // 会員の存在確認
        memberRepository.findById(request.memberId)
            ?: return ReturnBookResult.Failure("会員が見つかりません")

        // 書籍の存在確認
        bookRepository.findById(request.bookId)
            ?: return ReturnBookResult.Failure("書籍が見つかりません")

        // アクティブな貸出記録を検索
        val activeLoan = loanRepository.findActiveByBookId(request.bookId)
            ?: return ReturnBookResult.Failure("この書籍は貸し出されていません")

        // 会員IDの一致確認
        if (activeLoan.memberId != request.memberId) {
            return ReturnBookResult.Failure("この書籍は別の会員が借りています")
        }

        // 返却処理（貸出記録を削除）
        val deleteResult = loanRepository.delete(activeLoan.id)
        if (deleteResult.isFailure) {
            return ReturnBookResult.Failure(
                deleteResult.exceptionOrNull()?.message ?: "返却処理に失敗しました"
            )
        }

        return ReturnBookResult.Success(activeLoan)
    }
}
