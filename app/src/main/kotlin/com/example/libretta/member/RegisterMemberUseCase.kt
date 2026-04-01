package com.example.libretta.member

import com.example.libretta.model.Member

data class RegisterMemberRequest(val name: String)

sealed class RegisterMemberResult {
    data class Success(val member: Member) : RegisterMemberResult()
    data class Failure(val errorMessage: String) : RegisterMemberResult()
    data class ValidationError(val message: String) : RegisterMemberResult()
}

class RegisterMemberUseCase(private val memberRepository: MemberRepository) {
    fun execute(request: RegisterMemberRequest): RegisterMemberResult {
        // バリデーション
        if (request.name.isBlank()) {
            return RegisterMemberResult.ValidationError("名前を入力してください")
        }

        // IDを生成 (DA-XXXX形式)
        val id = generateMemberId()

        val member = Member(
            id = id,
            name = request.name
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
}
