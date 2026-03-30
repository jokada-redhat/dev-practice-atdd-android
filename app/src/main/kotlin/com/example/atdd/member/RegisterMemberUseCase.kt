package com.example.atdd.member

import com.example.atdd.model.Member
import java.util.UUID

data class RegisterMemberRequest(
    val name: String,
    val email: String,
    val phone: String = "",
    val address: String = ""
)

sealed class RegisterMemberResult {
    data class Success(val member: Member) : RegisterMemberResult()
    data class Failure(val errorMessage: String) : RegisterMemberResult()
    data class ValidationError(val message: String) : RegisterMemberResult()
}

class RegisterMemberUseCase(
    private val memberRepository: MemberRepository
) {
    fun execute(request: RegisterMemberRequest): RegisterMemberResult {
        // バリデーション
        if (request.name.isBlank()) {
            return RegisterMemberResult.ValidationError("名前を入力してください")
        }
        if (request.email.isBlank()) {
            return RegisterMemberResult.ValidationError("メールアドレスを入力してください")
        }
        if (!isValidEmail(request.email)) {
            return RegisterMemberResult.ValidationError("有効なメールアドレスを入力してください")
        }

        // IDを生成 (DA-XXXX形式)
        val id = generateMemberId()

        val member = Member(
            id = id,
            name = request.name,
            email = request.email,
            phone = request.phone,
            address = request.address,
            loanCount = 0
        )

        return when (val result = memberRepository.save(member)) {
            is Result.Success -> RegisterMemberResult.Success(result.getOrThrow())
            is Result.Failure -> RegisterMemberResult.Failure(
                result.exceptionOrNull()?.message ?: "会員登録に失敗しました"
            )
        }
    }

    private fun generateMemberId(): String {
        val randomNumber = (1000..9999).random()
        return "DA-$randomNumber"
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
