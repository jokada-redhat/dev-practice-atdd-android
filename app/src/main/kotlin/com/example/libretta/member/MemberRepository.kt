package com.example.libretta.member

import com.example.libretta.model.Member

interface MemberRepository {
    fun save(member: Member): Result<Member>
    fun findById(id: String): Member?
    fun findAll(): List<Member>
    fun search(query: String): List<Member>
    fun delete(id: String): Result<Unit>
    fun clear()
}
