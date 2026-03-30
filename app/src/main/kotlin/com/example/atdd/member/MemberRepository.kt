package com.example.atdd.member

import com.example.atdd.model.Member

interface MemberRepository {
    fun save(member: Member): Result<Member>
    fun findById(id: String): Member?
    fun findByEmail(email: String): Member?
    fun findAll(): List<Member>
    fun search(query: String): List<Member>
    fun updateLoanCount(memberId: String, newCount: Int): Result<Unit>
    fun delete(id: String): Result<Unit>
    fun clear()
}
