package com.example.atdd.loan

import com.example.atdd.book.BookRepository
import com.example.atdd.member.MemberRepository
import com.example.atdd.model.BookStatus
import com.example.atdd.model.Loan
import java.time.LocalDate

data class ReturnBookRequest(
    val memberId: String,
    val bookId: String
)

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
        val member = memberRepository.findById(request.memberId)
            ?: return ReturnBookResult.Failure("会員が見つかりません")

        // 書籍の存在確認
        val book = bookRepository.findById(request.bookId)
            ?: return ReturnBookResult.Failure("書籍が見つかりません")

        // アクティブな貸出記録を検索
        val activeLoan = loanRepository.findActiveByBookId(request.bookId)
            ?: return ReturnBookResult.Failure("この書籍は貸し出されていません")

        // 会員IDの一致確認
        if (activeLoan.memberId != request.memberId) {
            return ReturnBookResult.Failure("この書籍は別の会員が借りています")
        }

        // 返却処理
        val returnedDate = LocalDate.now()
        val returnedLoan = when (val result = loanRepository.returnBook(activeLoan.id, returnedDate)) {
            is Result.Success -> result.getOrThrow()
            is Result.Failure -> {
                return ReturnBookResult.Failure(
                    result.exceptionOrNull()?.message ?: "返却処理に失敗しました"
                )
            }
        }

        // 書籍のステータスを更新
        when (val updateResult = bookRepository.updateStatus(request.bookId, BookStatus.AVAILABLE)) {
            is Result.Failure -> {
                // ロールバック
                loanRepository.save(activeLoan)
                return ReturnBookResult.Failure(
                    updateResult.exceptionOrNull()?.message ?: "書籍ステータスの更新に失敗しました"
                )
            }
            else -> {}
        }

        // 会員の貸出冊数を更新
        val newLoanCount = maxOf(0, member.loanCount - 1)
        when (val updateResult = memberRepository.updateLoanCount(request.memberId, newLoanCount)) {
            is Result.Failure -> {
                // ロールバック
                loanRepository.save(activeLoan)
                bookRepository.updateStatus(request.bookId, BookStatus.BORROWED)
                return ReturnBookResult.Failure(
                    updateResult.exceptionOrNull()?.message ?: "会員情報の更新に失敗しました"
                )
            }
            else -> {}
        }

        return ReturnBookResult.Success(returnedLoan)
    }
}
