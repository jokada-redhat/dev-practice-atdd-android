# ATDD 実践ウォークスルー: 貸出上限機能

この文書では、「貸出上限」機能の開発を通じて ATDD のサイクルを実際にどう回したかを追体験できるようにまとめています。

## 要件

> 会員が借りられる書籍の冊数に上限（3冊）を設ける。上限に達している場合は新たな貸出を拒否し、返却すれば再び借りられるようにする。

## Step 1: 受け入れテストを書く（RED）

最初に行うのは、feature ファイルにシナリオを追加することです。この時点ではプロダクションコードは一切変更しません。

### どんなシナリオが必要か考える

要件から以下の振る舞いを洗い出します:

| 振る舞い | シナリオ名 |
|---------|-----------|
| 上限未満なら借りられる | 貸出上限ぴったりまで借りられる |
| 上限に達していたら借りられない | 貸出上限に達している場合は借りられない |
| 返却すれば再び借りられる | 返却すれば再び借りられる |

### feature ファイルに追記する

`app/src/test/resources/features/borrowing_flow.feature` に 3 つのシナリオを追加しました:

```gherkin
Scenario: 貸出上限ぴったりまで借りられる
  Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
  And 会員 "DA-8821" が 2 冊借りている状態である
  And 書籍 "Neuromancer" が登録されている
  When 会員 "DA-8821" が書籍 "Neuromancer" を借りる
  Then 会員 "DA-8821" の貸出冊数が 3 になる

Scenario: 貸出上限に達している場合は借りられない
  Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
  And 会員 "DA-8821" が 3 冊借りている状態である
  And 書籍 "Neuromancer" が登録されている
  When 会員 "DA-8821" が書籍 "Neuromancer" を借りようとする
  Then エラーメッセージ "貸出上限（3冊）に達しています" が返される

Scenario: 返却すれば再び借りられる
  Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
  And 会員 "DA-8821" が 2 冊借りている状態である
  And 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
  When 会員 "DA-8821" が書籍 "The Infinite Library" を返却する
  Then 会員 "DA-8821" の貸出冊数が 2 になる
  And 書籍 "Dune" が登録されている
  When 会員 "DA-8821" が書籍 "Dune" を借りる
  Then 会員 "DA-8821" の貸出冊数が 3 になる
```

### シナリオ設計のポイント

#### 既存ステップの再利用

新しいステップ定義はほとんど不要でした。以下のステップはすべて既存のものです:

- `会員 "X" (ID: "Y") が登録されている`
- `書籍 "X" が登録されている`
- `会員 "X" が書籍 "Y" を借りる` / `借りようとする`
- `会員 "X" の貸出冊数が N になる`
- `エラーメッセージ "..." が返される`

#### 宣言的な Given の活用

「2冊借りている状態」を表現するのに、2冊分の書籍登録と貸出操作を書く代わりに:

```gherkin
# こう書かずに:
And 書籍 "Book A" が登録されている
And 書籍 "Book B" が登録されている
When 会員 "DA-8821" が書籍 "Book A" を借りる
And 会員 "DA-8821" が書籍 "Book B" を借りる

# こう書く:
And 会員 "DA-8821" が 2 冊借りている状態である
```

「借りる」行為自体は別のシナリオでテスト済みなので、ここでは宣言的に状態をセットアップするだけで十分です。テストの関心は「上限チェック」であり「借りる手順」ではありません。

### この時点でコミット

```
test: 貸出上限の受け入れテストを追加
```

テストを実行すると、貸出上限のチェックが未実装なので「貸出上限ぴったりまで借りられる」は通りますが「貸出上限に達している場合は借りられない」は失敗します。これが RED の状態です。

## Step 2: 実装する（GREEN）

テストを通すために、`BorrowBookUseCase` に貸出上限チェックを追加します。

### 変更箇所

`app/src/main/kotlin/com/example/libretta/loan/BorrowBookUseCase.kt`:

```kotlin
class BorrowBookUseCase(
    private val loanRepository: LoanRepository,
    private val bookRepository: BookRepository,
    private val memberRepository: MemberRepository
) {
    private companion object {
        const val BORROWING_LIMIT = 3
    }

    fun execute(request: BorrowBookRequest): BorrowBookResult {
        // 会員の存在確認
        memberRepository.findById(request.memberId)
            ?: return BorrowBookResult.Failure("会員が見つかりません")

        // 貸出上限チェック（★追加）
        val activeLoanCount = loanRepository.countActiveByMemberId(request.memberId)
        if (activeLoanCount >= BORROWING_LIMIT) {
            return BorrowBookResult.Failure("貸出上限（${BORROWING_LIMIT}冊）に達しています")
        }

        // 書籍の存在確認
        bookRepository.findById(request.bookId)
            ?: return BorrowBookResult.Failure("書籍が見つかりません")

        // ...以下略
    }
}
```

### 実装のポイント

- **最小限の変更**: 定数 `BORROWING_LIMIT` と 4 行の if チェックだけ
- **既存のパターンに従う**: 他のバリデーション（会員存在確認、書籍存在確認）と同じく `BorrowBookResult.Failure` を返す
- **チェックの順序**: 会員の存在確認の直後に配置。存在しない会員の上限チェックは無意味なので

### テスト実行

```bash
./gradlew test   # 全テストパス
./gradlew lint   # 警告ゼロ
```

全シナリオが GREEN になりました。

### この時点でコミット

```
impl: BorrowBookUseCase に貸出上限チェックを追加
```

## Step 3: リファクタリング

今回の実装は十分シンプルだったため、大きなリファクタリングは不要でした。

ただし、後日の全体的なリファクタリングで以下の改善を行いました:

- feature ファイルの Given 表現を整理（`貸出可能である` → `登録されている`）
- 曖昧なステップの明確化（`書籍 X が既に借りられている` → 誰が借りているか明示）
- 冗長な Given の削除

これらはテストの動作を変えずに可読性を改善する変更です。

## コミット履歴の全体像

```
test:     貸出上限の受け入れテストを追加        ← RED: テスト追加
impl:     BorrowBookUseCase に貸出上限チェックを追加  ← GREEN: 実装
refactor: 貸出上限シナリオの Given 表現を整理     ← REFACTOR: 表現改善
```

テスト → 実装 → リファクタリングの 3 フェーズがコミット履歴から明確に読み取れます。

## 振り返り: この機能開発で学べること

### 1. シナリオが設計を導く

シナリオを先に書くことで「どんなケースをカバーすべきか」が実装前に明確になります。今回は 3 つの振る舞い（上限未満、上限到達、返却後の復帰）を洗い出してから実装に入りました。

### 2. 既存のステップが資産になる

既に定義されているステップ（会員登録、書籍登録、貸出、返却、エラーメッセージ検証）をそのまま再利用でき、新しいステップ定義はほぼ不要でした。ATDD を続けるほどステップの資産が増え、新機能のテストが書きやすくなります。

### 3. 実装の変更は最小限

feature ファイルに 24 行のシナリオを追加しましたが、プロダクションコードの変更は定数定義と 4 行の if チェックだけでした。受け入れテストが「何を作るか」を明確にしてくれるので、余計なコードを書かずに済みます。

### 4. リファクタリングは安心して行える

受け入れテストがあるので、表現の改善やコードの整理を安心して行えます。テストが通り続ける限り、振る舞いが壊れていないことが保証されます。
