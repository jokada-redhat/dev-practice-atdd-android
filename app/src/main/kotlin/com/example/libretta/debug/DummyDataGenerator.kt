package com.example.libretta.debug

import com.example.libretta.book.BookRepository
import com.example.libretta.loan.LoanRepository
import com.example.libretta.member.MemberRepository
import com.example.libretta.model.Book
import com.example.libretta.model.BookStatus
import com.example.libretta.model.Loan
import com.example.libretta.model.Member
import java.time.LocalDate

class DummyDataGenerator(
    private val memberRepository: MemberRepository,
    private val bookRepository: BookRepository,
    private val loanRepository: LoanRepository
) {

    fun clearAll() {
        loanRepository.clear()
        bookRepository.clear()
        memberRepository.clear()
    }

    fun loadDummyData() {
        clearAll()
        loadMembers()
        loadBooks()
        loadLoans()
    }

    private fun loadMembers() {
        members.forEach { memberRepository.save(it) }
    }

    private fun loadBooks() {
        books.forEach { bookRepository.save(it) }
    }

    private fun loadLoans() {
        val today = LocalDate.now()
        loans(today).forEach { (loan, bookId, memberId) ->
            loanRepository.save(loan)
            bookRepository.updateStatus(bookId, BookStatus.BORROWED)
            val member = memberRepository.findById(memberId)
            if (member != null) {
                memberRepository.updateLoanCount(memberId, member.loanCount + 1)
            }
        }
    }

    companion object {
        val members = listOf(
            Member("DA-0001", "山田太郎", "yamada.taro@example.com"),
            Member("DA-0002", "佐藤花子", "sato.hanako@example.com"),
            Member("DA-0003", "鈴木一郎", "suzuki.ichiro@example.com"),
            Member("DA-0004", "田中美咲", "tanaka.misaki@example.com"),
            Member("DA-0005", "高橋健太", "takahashi.kenta@example.com"),
            Member("DA-0006", "伊藤由美", "ito.yumi@example.com"),
            Member("DA-0007", "渡辺翔太", "watanabe.shota@example.com"),
            Member("DA-0008", "中村あかり", "nakamura.akari@example.com"),
            Member("DA-0009", "小林大輔", "kobayashi.daisuke@example.com"),
            Member("DA-0010", "加藤さくら", "kato.sakura@example.com"),
            Member("DA-0011", "吉田拓海", "yoshida.takumi@example.com"),
            Member("DA-0012", "山口真理", "yamaguchi.mari@example.com"),
            Member("DA-0013", "松本悠人", "matsumoto.yuto@example.com"),
            Member("DA-0014", "井上凛", "inoue.rin@example.com"),
            Member("DA-0015", "木村蓮", "kimura.ren@example.com"),
            Member("DA-0016", "林美優", "hayashi.miyu@example.com"),
            Member("DA-0017", "清水陽太", "shimizu.yota@example.com"),
            Member("DA-0018", "森本結衣", "morimoto.yui@example.com"),
            Member("DA-0019", "池田颯太", "ikeda.sota@example.com"),
            Member("DA-0020", "橋本琴音", "hashimoto.kotone@example.com"),
        )

        val books = listOf(
            // 日本文学 (10冊)
            Book("B-001", "吾輩は猫である", "夏目漱石", "978-4-10-101001-5", "1905", BookStatus.AVAILABLE),
            Book("B-002", "坊っちゃん", "夏目漱石", "978-4-10-101002-2", "1906", BookStatus.AVAILABLE),
            Book("B-003", "人間失格", "太宰治", "978-4-10-101003-9", "1948", BookStatus.AVAILABLE),
            Book("B-004", "走れメロス", "太宰治", "978-4-10-101004-6", "1940", BookStatus.AVAILABLE),
            Book("B-005", "雪国", "川端康成", "978-4-10-101005-3", "1937", BookStatus.AVAILABLE),
            Book("B-006", "伊豆の踊子", "川端康成", "978-4-10-101006-0", "1926", BookStatus.AVAILABLE),
            Book("B-007", "羅生門", "芥川龍之介", "978-4-10-101007-7", "1915", BookStatus.AVAILABLE),
            Book("B-008", "蜘蛛の糸", "芥川龍之介", "978-4-10-101008-4", "1918", BookStatus.AVAILABLE),
            Book("B-009", "ノルウェイの森", "村上春樹", "978-4-10-101009-1", "1987", BookStatus.AVAILABLE),
            Book("B-010", "海辺のカフカ", "村上春樹", "978-4-10-101010-7", "2002", BookStatus.AVAILABLE),

            // SF・ファンタジー (10冊)
            Book("B-011", "銀河鉄道の夜", "宮沢賢治", "978-4-10-102001-4", "1934", BookStatus.AVAILABLE),
            Book("B-012", "時をかける少女", "筒井康隆", "978-4-10-102002-1", "1967", BookStatus.AVAILABLE),
            Book("B-013", "新世界より", "貴志祐介", "978-4-10-102003-8", "2008", BookStatus.AVAILABLE),
            Book("B-014", "ハーモニー", "伊藤計劃", "978-4-10-102004-5", "2008", BookStatus.AVAILABLE),
            Book("B-015", "虐殺器官", "伊藤計劃", "978-4-10-102005-2", "2007", BookStatus.AVAILABLE),
            Book("B-016", "デューン 砂の惑星", "フランク・ハーバート", "978-4-15-012001-3", "1965", BookStatus.AVAILABLE),
            Book("B-017", "ニューロマンサー", "ウィリアム・ギブスン", "978-4-15-012002-0", "1984", BookStatus.AVAILABLE),
            Book("B-018", "ファウンデーション", "アイザック・アシモフ", "978-4-15-012003-7", "1951", BookStatus.AVAILABLE),
            Book("B-019", "2001年宇宙の旅", "アーサー・C・クラーク", "978-4-15-012004-4", "1968", BookStatus.AVAILABLE),
            Book("B-020", "アンドロイドは電気羊の夢を見るか?", "フィリップ・K・ディック", "978-4-15-012005-1", "1968", BookStatus.AVAILABLE),

            // ミステリー (10冊)
            Book("B-021", "容疑者Xの献身", "東野圭吾", "978-4-16-103001-3", "2005", BookStatus.AVAILABLE),
            Book("B-022", "白夜行", "東野圭吾", "978-4-16-103002-0", "1999", BookStatus.AVAILABLE),
            Book("B-023", "模倣犯", "宮部みゆき", "978-4-16-103003-7", "2001", BookStatus.AVAILABLE),
            Book("B-024", "火車", "宮部みゆき", "978-4-16-103004-4", "1992", BookStatus.AVAILABLE),
            Book("B-025", "十角館の殺人", "綾辻行人", "978-4-16-103005-1", "1987", BookStatus.AVAILABLE),
            Book("B-026", "占星術殺人事件", "島田荘司", "978-4-16-103006-8", "1981", BookStatus.AVAILABLE),
            Book("B-027", "殺戮にいたる病", "我孫子武丸", "978-4-16-103007-5", "1992", BookStatus.AVAILABLE),
            Book("B-028", "すべてがFになる", "森博嗣", "978-4-16-103008-2", "1996", BookStatus.AVAILABLE),
            Book("B-029", "告白", "湊かなえ", "978-4-16-103009-9", "2008", BookStatus.AVAILABLE),
            Book("B-030", "氷菓", "米澤穂信", "978-4-16-103010-5", "2001", BookStatus.AVAILABLE),

            // 海外文学 (10冊)
            Book("B-031", "星の王子さま", "サン=テグジュペリ", "978-4-10-104001-2", "1943", BookStatus.AVAILABLE),
            Book("B-032", "変身", "フランツ・カフカ", "978-4-10-104002-9", "1915", BookStatus.AVAILABLE),
            Book("B-033", "老人と海", "アーネスト・ヘミングウェイ", "978-4-10-104003-6", "1952", BookStatus.AVAILABLE),
            Book("B-034", "異邦人", "アルベール・カミュ", "978-4-10-104004-3", "1942", BookStatus.AVAILABLE),
            Book("B-035", "グレート・ギャツビー", "F・スコット・フィッツジェラルド", "978-4-10-104005-0", "1925", BookStatus.AVAILABLE),
            Book("B-036", "1984年", "ジョージ・オーウェル", "978-4-10-104006-7", "1949", BookStatus.AVAILABLE),
            Book("B-037", "ライ麦畑でつかまえて", "J・D・サリンジャー", "978-4-10-104007-4", "1951", BookStatus.AVAILABLE),
            Book("B-038", "罪と罰", "フョードル・ドストエフスキー", "978-4-10-104008-1", "1866", BookStatus.AVAILABLE),
            Book("B-039", "百年の孤独", "ガブリエル・ガルシア=マルケス", "978-4-10-104009-8", "1967", BookStatus.AVAILABLE),
            Book("B-040", "車輪の下", "ヘルマン・ヘッセ", "978-4-10-104010-4", "1906", BookStatus.AVAILABLE),

            // ノンフィクション・ビジネス (10冊)
            Book("B-041", "サピエンス全史", "ユヴァル・ノア・ハラリ", "978-4-309-22671-2", "2011", BookStatus.AVAILABLE),
            Book("B-042", "ファクトフルネス", "ハンス・ロスリング", "978-4-532-17621-5", "2018", BookStatus.AVAILABLE),
            Book("B-043", "銃・病原菌・鉄", "ジャレド・ダイアモンド", "978-4-794-21005-7", "1997", BookStatus.AVAILABLE),
            Book("B-044", "思考の整理学", "外山滋比古", "978-4-480-02047-0", "1983", BookStatus.AVAILABLE),
            Book("B-045", "嫌われる勇気", "岸見一郎・古賀史健", "978-4-478-02581-9", "2013", BookStatus.AVAILABLE),
            Book("B-046", "7つの習慣", "スティーブン・R・コヴィー", "978-4-863-40010-4", "1989", BookStatus.AVAILABLE),
            Book("B-047", "影響力の武器", "ロバート・B・チャルディーニ", "978-4-416-70601-5", "1984", BookStatus.AVAILABLE),
            Book("B-048", "ゼロ・トゥ・ワン", "ピーター・ティール", "978-4-14-081695-0", "2014", BookStatus.AVAILABLE),
            Book("B-049", "イシューからはじめよ", "安宅和人", "978-4-862-76088-5", "2010", BookStatus.AVAILABLE),
            Book("B-050", "FACTFULNESS", "ハンス・ロスリング", "978-4-532-32211-6", "2019", BookStatus.AVAILABLE),
        )

        // 貸出データ: 10冊を6人の会員に貸出中
        // Triple(Loan, bookId, memberId)
        fun loans(today: LocalDate) = listOf(
            Triple(Loan("L-001", "DA-0001", "B-003", today.minusDays(5)), "B-003", "DA-0001"),   // 人間失格 → 山田太郎
            Triple(Loan("L-002", "DA-0001", "B-009", today.minusDays(3)), "B-009", "DA-0001"),   // ノルウェイの森 → 山田太郎
            Triple(Loan("L-003", "DA-0002", "B-017", today.minusDays(10)), "B-017", "DA-0002"),  // ニューロマンサー → 佐藤花子
            Triple(Loan("L-004", "DA-0002", "B-021", today.minusDays(7)), "B-021", "DA-0002"),   // 容疑者Xの献身 → 佐藤花子
            Triple(Loan("L-005", "DA-0003", "B-031", today.minusDays(12)), "B-031", "DA-0003"),  // 星の王子さま → 鈴木一郎
            Triple(Loan("L-006", "DA-0004", "B-036", today.minusDays(2)), "B-036", "DA-0004"),   // 1984年 → 田中美咲
            Triple(Loan("L-007", "DA-0004", "B-041", today.minusDays(1)), "B-041", "DA-0004"),   // サピエンス全史 → 田中美咲
            Triple(Loan("L-008", "DA-0005", "B-045", today.minusDays(8)), "B-045", "DA-0005"),   // 嫌われる勇気 → 高橋健太
            Triple(Loan("L-009", "DA-0006", "B-012", today.minusDays(4)), "B-012", "DA-0006"),   // 時をかける少女 → 伊藤由美
            Triple(Loan("L-010", "DA-0006", "B-025", today.minusDays(6)), "B-025", "DA-0006"),   // 十角館の殺人 → 伊藤由美
        )
    }
}
