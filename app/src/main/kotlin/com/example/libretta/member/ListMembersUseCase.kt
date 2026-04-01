package com.example.libretta.member

import com.example.libretta.model.Member

class ListMembersUseCase(private val memberRepository: MemberRepository) {
    fun execute(): List<Member> = memberRepository.findAll()

    fun search(query: String): List<Member> {
        if (query.isBlank()) {
            return execute()
        }
        return memberRepository.search(query)
    }
}
