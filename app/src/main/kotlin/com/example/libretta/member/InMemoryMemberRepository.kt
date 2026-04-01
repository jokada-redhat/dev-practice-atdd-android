package com.example.libretta.member

import com.example.libretta.model.Member

class InMemoryMemberRepository : MemberRepository {
    private val members = mutableMapOf<String, Member>()

    override fun save(member: Member): Result<Member> {
        // メールアドレスの重複チェック
        val existingMember = findByEmail(member.email)
        if (existingMember != null && existingMember.id != member.id) {
            return Result.failure(IllegalArgumentException("このメールアドレスは既に登録されています"))
        }

        members[member.id] = member
        return Result.success(member)
    }

    override fun findById(id: String): Member? {
        return members[id]
    }

    override fun findByEmail(email: String): Member? {
        return members.values.find { it.email == email }
    }

    override fun findAll(): List<Member> {
        return members.values.toList()
    }

    override fun search(query: String): List<Member> {
        return members.values.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true) ||
            it.id.contains(query, ignoreCase = true)
        }
    }

    override fun updateLoanCount(memberId: String, newCount: Int): Result<Unit> {
        val member = members[memberId]
            ?: return Result.failure(NoSuchElementException("会員が見つかりません"))

        members[memberId] = member.copy(loanCount = newCount)
        return Result.success(Unit)
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
