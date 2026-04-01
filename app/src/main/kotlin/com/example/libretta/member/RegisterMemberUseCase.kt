package com.example.libretta.member

import com.example.libretta.model.Member
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

        val result = memberRepository.save(member)
        return if (result.isSuccess) {
            RegisterMemberResult.Success(result.getOrThrow())
        } else {
            RegisterMemberResult.Failure(
                result.exceptionOrNull()?.message ?: "会員登録に失敗しました"
            )
        }
    }

    private fun generateMemberId(): String {
        repeat(MAX_ID_GENERATION_ATTEMPTS) {
            val randomNumber = (1000..9999).random()
            val id = "DA-$randomNumber"
            if (memberRepository.findById(id) == null) {
                return id
            }
        }
        return "DA-${System.currentTimeMillis() % 100000}"
    }

    private companion object {
        const val MAX_ID_GENERATION_ATTEMPTS = 10
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
    }
}
