package com.example.atdd.member

import com.example.atdd.model.Member

class ListMembersUseCase(
    private val memberRepository: MemberRepository
) {
    fun execute(): List<Member> {
        return memberRepository.findAll()
    }

    fun search(query: String): List<Member> {
        if (query.isBlank()) {
            return execute()
        }
        return memberRepository.search(query)
    }
}
