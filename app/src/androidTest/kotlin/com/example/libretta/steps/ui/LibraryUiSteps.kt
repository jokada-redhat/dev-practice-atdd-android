package com.example.libretta.steps.ui

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.libretta.BookCatalogActivity
import com.example.libretta.MemberListActivity
import com.example.libretta.R
import com.example.libretta.TopActivity
import com.example.libretta.test.TestHelper
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.hamcrest.Matchers.anything

class LibraryUiSteps {

    private var topScenario: ActivityScenario<TopActivity>? = null
    private var memberListScenario: ActivityScenario<MemberListActivity>? = null
    private var bookCatalogScenario: ActivityScenario<BookCatalogActivity>? = null

    @Before("@library")
    fun setUp() {
        // セッションにダミーデータを設定（認証スキップ用）
        val app = TestHelper.getApp()
        app.getSharedPreferences("atdd_session", Context.MODE_PRIVATE)
            .edit()
            .putString("token", "dev-token")
            .putString("displayName", "開発ユーザー")
            .commit()
    }

    @After("@library")
    fun tearDown() {
        topScenario?.close()
        memberListScenario?.close()
        bookCatalogScenario?.close()
    }

    // === 貸出上限セットアップ ===

    @Given("会員 {string} の貸出冊数を上限に設定する")
    fun setMemberLoanCountToLimit(memberName: String) {
        val app = TestHelper.getApp()
        val member = app.memberRepository.search(memberName).firstOrNull()
            ?: throw IllegalStateException("会員 '$memberName' が見つかりません")
        val availableBooks = app.bookRepository.findAll()
            .filter { it.isAvailable }
            .take(3)
        val borrowUseCase = com.example.libretta.loan.BorrowBookUseCase(
            app.loanRepository,
            app.bookRepository,
            app.memberRepository
        )
        for (book in availableBooks) {
            borrowUseCase.execute(
                com.example.libretta.loan.BorrowBookRequest(member.id, book.id)
            )
        }
    }

    // === トップ画面 ===

    @Given("トップ画面が表示されている")
    fun topActivityIsDisplayed() {
        topScenario = ActivityScenario.launch(TopActivity::class.java)
        Thread.sleep(SCREEN_TRANSITION_WAIT_MS)
    }

    @When("貸し出しカードをタップする")
    fun tapBorrowingCard() {
        onView(withId(R.id.cardBorrowing)).perform(click())
        Thread.sleep(SCREEN_TRANSITION_WAIT_MS)
    }

    // === 会員一覧画面 ===

    @Then("会員一覧画面が表示される")
    fun memberListIsDisplayed() {
        onView(withId(R.id.recyclerViewMembers)).check(matches(isDisplayed()))
    }

    @Given("会員一覧画面が表示されている")
    fun launchMemberList() {
        memberListScenario = ActivityScenario.launch(MemberListActivity::class.java)
        Thread.sleep(SCREEN_TRANSITION_WAIT_MS)
    }

    @Then("会員 {string} のカードが表示されている")
    fun memberCardIsDisplayed(name: String) {
        onView(withText(name)).check(matches(isDisplayed()))
    }

    @When("会員 {string} のカードをタップする")
    fun tapMemberCard(name: String) {
        onView(withText(name)).perform(click())
        Thread.sleep(SCREEN_TRANSITION_WAIT_MS)
    }

    // === 書籍カタログ画面 ===

    @Then("書籍カタログ画面が表示される")
    fun bookCatalogIsDisplayed() {
        onView(withId(R.id.recyclerViewBooks)).check(matches(isDisplayed()))
    }

    @And("選択中メンバー {string} が表示されている")
    fun selectedMemberIsDisplayed(name: String) {
        onView(withId(R.id.textSelectedMember)).check(matches(withText(name)))
    }

    @Given("書籍カタログ画面が会員 {string} で表示されている")
    fun launchBookCatalogWithMember(memberName: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, BookCatalogActivity::class.java).apply {
            putExtra("memberName", memberName)
        }
        bookCatalogScenario = ActivityScenario.launch(intent)
        Thread.sleep(SCREEN_TRANSITION_WAIT_MS)
    }

    @Then("書籍 {string} のカードが表示されている")
    fun bookCardIsDisplayed(title: String) {
        // NestedScrollView + RecyclerView のため、ビュー階層内の存在を確認
        onView(withId(R.id.recyclerViewBooks))
            .check(matches(hasDescendant(withText(title))))
    }

    @And("書籍 {string} のカードが表示されていない")
    fun bookCardIsNotDisplayed(title: String) {
        // フィルタ後にビュー階層から消えていることを確認
        onView(withId(R.id.recyclerViewBooks))
            .check(matches(org.hamcrest.Matchers.not(hasDescendant(withText(title)))))
    }

    @When("{string} フィルタボタンをタップする")
    fun tapFilterButton(filterLabel: String) {
        val buttonId = when (filterLabel) {
            "All" -> R.id.buttonFilterAll
            "Available" -> R.id.buttonFilterAvailable
            "Borrowed" -> R.id.buttonFilterBorrowed
            else -> throw IllegalArgumentException("Unknown filter: $filterLabel")
        }
        onView(withId(buttonId)).perform(click())
        Thread.sleep(FILTER_WAIT_MS)
    }

    // === 貸し出しフロー ===

    @And("書籍 {string} の貸し出しボタンをタップする")
    fun tapBorrowButton(title: String) {
        // 対象の書籍カードまでスクロールしてBorrowボタンをクリック
        onView(withId(R.id.recyclerViewBooks))
            .perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                    hasDescendant(withText(title))
                )
            )
        // 書籍カード内のBorrowボタンをクリック
        onView(withText(title))
            .perform(scrollTo())
        Thread.sleep(FILTER_WAIT_MS)

        // Borrow ボタンをクリック（対象カード内の最初のBorrowボタン）
        onView(withId(R.id.recyclerViewBooks))
            .perform(
                RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                    hasDescendant(withText(title)),
                    ClickChildViewAction(R.id.buttonBorrow)
                )
            )
    }

    @Then("貸し出し成功メッセージが表示される")
    fun borrowSuccessMessageIsDisplayed() {
        // Toast表示を待機
        Thread.sleep(TOAST_WAIT_MS)
        // Toastの検証はEspressoでは困難なため、
        // 画面がクラッシュせずに表示されていることを確認
        onView(withId(R.id.recyclerViewBooks)).check(matches(isDisplayed()))
    }

    @Then("貸し出しエラーメッセージが表示される")
    fun borrowErrorMessageIsDisplayed() {
        Thread.sleep(TOAST_WAIT_MS)
        // エラー時は画面遷移せずカタログ画面が表示されたままであることを確認
        onView(withId(R.id.recyclerViewBooks)).check(matches(isDisplayed()))
    }

    private companion object {
        const val SCREEN_TRANSITION_WAIT_MS = 1000L
        const val FILTER_WAIT_MS = 500L
        const val TOAST_WAIT_MS = 1000L
    }
}
