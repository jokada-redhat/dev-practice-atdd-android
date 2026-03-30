package com.example.atdd.loan

import com.example.atdd.book.BookRepository
import com.example.atdd.member.MemberRepository
import com.example.atdd.model.BookStatus
import com.example.atdd.model.Loan
import java.time.LocalDate
import java.util.UUID

data class BorrowBookRequest(
    val memberId: String,
    val bookId: String
)

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
        val member = memberRepository.findById(request.memberId)
            ?: return BorrowBookResult.Failure("会員が見つかりません")

        // 書籍の存在確認
        val book = bookRepository.findById(request.bookId)
            ?: return BorrowBookResult.Failure("書籍が見つかりません")

        // 書籍の貸出状態確認
        if (book.status == BookStatus.BORROWED) {
            return BorrowBookResult.Failure("この書籍は既に貸出中です")
        }

        // 貸出記録を作成
        val loan = Loan(
            id = UUID.randomUUID().toString(),
            memberId = request.memberId,
            bookId = request.bookId,
            borrowedDate = LocalDate.now(),
            returnedDate = null
        )

        // 貸出記録を保存
        when (val saveResult = loanRepository.save(loan)) {
            is Result.Failure -> {
                return BorrowBookResult.Failure(
                    saveResult.exceptionOrNull()?.message ?: "貸出記録の保存に失敗しました"
                )
            }
            else -> {}
        }

        // 書籍のステータスを更新
        when (val updateResult = bookRepository.updateStatus(request.bookId, BookStatus.BORROWED)) {
            is Result.Failure -> {
                // ロールバック
                loanRepository.delete(loan.id)
                return BorrowBookResult.Failure(
                    updateResult.exceptionOrNull()?.message ?: "書籍ステータスの更新に失敗しました"
                )
            }
            else -> {}
        }

        // 会員の貸出冊数を更新
        when (val updateResult = memberRepository.updateLoanCount(request.memberId, member.loanCount + 1)) {
            is Result.Failure -> {
                // ロールバック
                loanRepository.delete(loan.id)
                bookRepository.updateStatus(request.bookId, BookStatus.AVAILABLE)
                return BorrowBookResult.Failure(
                    updateResult.exceptionOrNull()?.message ?: "会員情報の更新に失敗しました"
                )
            }
            else -> {}
        }

        return BorrowBookResult.Success(loan)
    }
}
