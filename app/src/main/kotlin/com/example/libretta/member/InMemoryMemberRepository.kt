package com.example.libretta.member

import com.example.libretta.model.Member

class InMemoryMemberRepository : MemberRepository {
    private val members = mutableMapOf<String, Member>()

    override fun save(member: Member): Result<Member> {
        members[member.id] = member
        return Result.success(member)
    }

    override fun findById(id: String): Member? = members[id]

    override fun findAll(): List<Member> = members.values.toList()

    override fun search(query: String): List<Member> = members.values.filter {
        it.name.contains(query, ignoreCase = true) ||
            it.id.contains(query, ignoreCase = true)
    }

    override fun delete(id: String): Result<Unit> {
        if (members.remove(id) == null) {
            return Result.failure(NoSuchElementException("会員が見つかりません"))
        }
        return Result.success(Unit)
    }

    override fun clear() {
        members.clear()
    }
}
