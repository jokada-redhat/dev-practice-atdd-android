package com.example.libretta.steps

import com.example.libretta.member.InMemoryMemberRepository
import com.example.libretta.member.ListMembersUseCase
import com.example.libretta.member.RegisterMemberRequest
import com.example.libretta.member.RegisterMemberResult
import com.example.libretta.member.RegisterMemberUseCase
import com.example.libretta.model.Member
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import io.cucumber.datatable.DataTable
import org.junit.Assert.*

class MemberManagementSteps {

    private val memberRepository = InMemoryMemberRepository()
    private val registerMemberUseCase = RegisterMemberUseCase(memberRepository)
    private val listMembersUseCase = ListMembersUseCase(memberRepository)

    private var registerResult: RegisterMemberResult? = null
    private var memberList: List<Member> = emptyList()
    private var searchResults: List<Member> = emptyList()
    private var errorMessage: String? = null

    @Before
    fun setUp() {
        memberRepository.clear()
        registerResult = null
        memberList = emptyList()
        searchResults = emptyList()
        errorMessage = null
    }

    @Given("会員リストが空である")
    fun memberListIsEmpty() {
        memberRepository.clear()
    }

    @When("会員 {string} をメールアドレス {string} で登録する")
    fun registerMemberWithEmail(name: String, email: String) {
        val request = RegisterMemberRequest(name = name, email = email)
        registerResult = registerMemberUseCase.execute(request)
    }

    @Then("会員リストに {string} が含まれている")
    fun memberListContains(name: String) {
        memberList = listMembersUseCase.execute()
        assertTrue(
            "会員リストに $name が含まれているべき",
            memberList.any { it.name == name }
        )
    }

    @And("会員 {string} の貸出冊数は {int} である")
    fun memberLoanCountIs(name: String, loanCount: Int) {
        memberList = listMembersUseCase.execute()
        val member = memberList.find { it.name == name }
        assertNotNull("会員 $name が見つかりません", member)
        assertEquals("貸出冊数が一致しません", loanCount, member?.loanCount)
    }

    @Given("以下の会員が登録されている:")
    fun membersAreRegistered(dataTable: DataTable) {
        val members = dataTable.asMaps()
        for (row in members) {
            val member = Member(
                id = row["id"]!!,
                name = row["name"]!!,
                email = row["email"]!!,
                loanCount = row["loanCount"]?.toInt() ?: 0
            )
            memberRepository.save(member)
        }
    }

    @When("会員一覧を取得する")
    fun getMemberList() {
        memberList = listMembersUseCase.execute()
    }

    @Then("会員リストに {int} 件の会員が含まれている")
    fun memberListContainsCount(count: Int) {
        assertEquals("会員数が一致しません", count, memberList.size)
    }

    @And("会員リストの先頭は {string} である")
    fun firstMemberIs(name: String) {
        assertTrue("会員リストが空です", memberList.isNotEmpty())
        assertEquals("先頭の会員名が一致しません", name, memberList.first().name)
    }

    @When("会員を {string} で検索する")
    fun searchMembers(query: String) {
        searchResults = listMembersUseCase.search(query)
    }

    @Then("検索結果に {int} 件の会員が含まれている")
    fun searchResultsContainCount(count: Int) {
        assertEquals("検索結果数が一致しません", count, searchResults.size)
    }

    @And("会員検索結果に {string} が含まれている")
    fun memberSearchResultsContain(name: String) {
        assertTrue(
            "検索結果に $name が含まれているべき",
            searchResults.any { it.name == name }
        )
    }

    @Given("会員 {string} がメールアドレス {string} で既に登録されている")
    fun memberAlreadyRegistered(name: String, email: String) {
        val request = RegisterMemberRequest(name = name, email = email)
        registerMemberUseCase.execute(request)
    }

    @When("会員 {string} をメールアドレス {string} で登録しようとする")
    fun tryToRegisterMemberWithEmail(name: String, email: String) {
        val request = RegisterMemberRequest(name = name, email = email)
        registerResult = registerMemberUseCase.execute(request)

        // エラーメッセージを共通変数に設定
        when (val result = registerResult) {
            is RegisterMemberResult.Failure -> {
                CommonSteps.lastErrorMessage = result.errorMessage
            }
            is RegisterMemberResult.ValidationError -> {
                CommonSteps.lastErrorMessage = result.message
            }
            else -> {}
        }
    }


    @And("会員リストに {string} が含まれていない")
    fun memberListDoesNotContain(name: String) {
        memberList = listMembersUseCase.execute()
        assertFalse(
            "会員リストに $name が含まれていないべき",
            memberList.any { it.name == name }
        )
    }

    @When("名前が空で登録しようとする")
    fun tryToRegisterWithEmptyName() {
        val request = RegisterMemberRequest(name = "", email = "test@example.com")
        registerResult = registerMemberUseCase.execute(request)
        when (val result = registerResult) {
            is RegisterMemberResult.ValidationError -> {
                CommonSteps.lastErrorMessage = result.message
            }
            else -> {}
        }
    }

    @Then("バリデーションエラー {string} が返される")
    fun validationErrorIsReturned(expectedMessage: String) {
        assertEquals("バリデーションエラーが一致しません", expectedMessage, CommonSteps.lastErrorMessage)
    }
}
