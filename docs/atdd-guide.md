# ATDD（受け入れテスト駆動開発）ガイド

## 目次

1. [ATDD とは何か](#1-atdd-とは何か)
2. [TDD・BDD との違い](#2-tddbdd-との違い)
3. [ATDD のサイクル](#3-atdd-のサイクル)
4. [Gherkin の書き方](#4-gherkin-の書き方)
5. [良いシナリオを書くためのポイント](#5-良いシナリオを書くためのポイント)
6. [本プロジェクトでの実践](#6-本プロジェクトでの実践)
   - [タグによるテスト実行の絞り込み（スモークテストなど）](#タグによるテスト実行の絞り込みスモークテストなど)
7. [よくある質問](#7-よくある質問)

---

## 1. ATDD とは何か

**ATDD（Acceptance Test Driven Development）** は、機能の実装を始める前に「受け入れ条件」を自動テストとして定義し、そのテストが通ることをゴールに開発を進める手法です。

核となる考え方はシンプルです:

> **「完成」の定義を先に決めてから作り始める。**

受け入れ条件はビジネス要件をそのまま反映したテストなので、開発者・テスター・ビジネス担当者が「何を作るか」について共通認識を持てます。

### ATDD がもたらすもの

- **認識のずれの防止**: 作る前に「何が正しい動作か」を全員で合意する
- **生きたドキュメント**: テストがそのまま仕様書になる。コードと乖離しない
- **リグレッション防止**: 既存機能が壊れたらすぐ気付ける
- **スコープの明確化**: 「このシナリオが通れば完了」というゴールが明確

## 2. TDD・BDD との違い

| 観点 | TDD | BDD | ATDD |
|------|-----|-----|------|
| **テストの粒度** | ユニット（関数・クラス） | 振る舞い（機能） | 受け入れ条件（ユーザー視点） |
| **誰が書くか** | 開発者 | 開発者 | 開発者 + ビジネス担当者 |
| **言語** | プログラミング言語 | 自然言語に近い DSL | 自然言語（Gherkin） |
| **目的** | 設計の改善 | 振る舞いの定義 | 要件の合意と検証 |
| **スコープ** | 内部実装 | 機能単位 | ビジネス要件単位 |

これらは対立する概念ではなく **補完関係** にあります。ATDD で受け入れテストを書き、その中で TDD を使って個々のクラスを実装し、BDD の記法（Given-When-Then）でシナリオを記述する、という組み合わせが一般的です。

## 3. ATDD のサイクル

ATDD は以下のサイクルで進みます:

```
1. 受け入れ条件を定義（feature ファイル作成）
   ↓
2. テストを実行 → 失敗（RED）
   ↓
3. ステップ定義を実装
   ↓
4. プロダクションコードを実装 → テスト成功（GREEN）
   ↓
5. リファクタリング
   ↓
（次の機能へ → 1 に戻る）
```

### ポイント: テストと実装を分けてコミットする

ATDD のプロセスを明確にするため、テストの追加と実装を別のコミットにします:

```
test: 貸出上限の受け入れテストを追加    ← まずテスト（RED）
impl: BorrowBookUseCase に貸出上限チェックを追加  ← 次に実装（GREEN）
refactor: 貸出冊数の判定ロジックを抽出   ← 最後にリファクタ
```

これにより、git の履歴から「何を作ろうとしたか（テスト）」と「どう作ったか（実装）」が分離されて読みやすくなります。

## 4. Gherkin の書き方

ATDD では **Gherkin** という記法でシナリオを記述します。`.feature` ファイルに書きます。

### 基本構造

```gherkin
Feature: 機能の名前
  機能の説明文（任意）

  Scenario: シナリオの名前
    Given 前提条件（テストの初期状態を用意する）
    When  操作（ユーザーが行うアクション）
    Then  期待結果（どうなるべきか）
```

### Given-When-Then の役割

| キーワード | 役割 | 例 |
|-----------|------|-----|
| **Given** | 前提条件のセットアップ | `Given 会員 "山田太郎" (ID: "DA-8821") が登録されている` |
| **When** | ユーザーのアクション | `When 会員 "DA-8821" が書籍 "Dune" を借りる` |
| **Then** | 期待する結果の検証 | `Then 会員 "DA-8821" の貸出冊数が 1 になる` |
| **And** | 直前のキーワードの続き | `And 貸出記録が作成される` |

### 実際の例

本プロジェクトの `borrowing_flow.feature` から:

```gherkin
Feature: 貸し出しフロー
  会員が書籍を借りることができる

  Scenario: 会員が書籍を借りる
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 書籍 "The Infinite Library" が登録されている
    When 会員 "DA-8821" が書籍 "The Infinite Library" を借りる
    Then 書籍 "The Infinite Library" は貸出中である
    And 会員 "DA-8821" の貸出冊数が 1 になる
    And 貸出記録が作成される
```

### その他の Gherkin 機能

#### Background（共通の前提条件）

全シナリオで共通の Given をまとめられます:

```gherkin
Background:
  Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
  And 書籍 "The Infinite Library" が登録されている

Scenario: 書籍を借りる
  When 会員 "DA-8821" が書籍 "The Infinite Library" を借りる
  Then ...

Scenario: 書籍を返却する
  Given 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
  When 会員 "DA-8821" が書籍 "The Infinite Library" を返却する
  Then ...
```

#### DataTable（表形式のデータ）

複数のデータをまとめて渡せます:

```gherkin
Scenario: 登録済みの書籍が一覧表示される
  Given 書籍管理に以下の書籍が登録されている:
    | title               | author        | isbn           | year |
    | The Infinite Library | Jorge Borges  | 978-1234567890 | 2020 |
    | Foundation           | Isaac Asimov  | 978-0553293357 | 1951 |
  When 書籍一覧を表示する
  Then 書籍一覧に 2 件表示される
```

#### タグ（テストのグループ化）

シナリオにタグを付けて、実行対象を絞り込めます:

```gherkin
@smoke
Scenario: 会員が書籍を借りる
  ...
```

タグの活用例:

| タグ | 用途 |
|------|------|
| `@smoke` | スモークテスト（主要機能の疎通確認） |
| `@wip` | 作業中のシナリオ |
| `@api` | API 関連のシナリオ |
| `@slow` | 実行に時間がかかるシナリオ |

タグによるテスト実行の絞り込み方法は [タグによるテスト実行の絞り込み](#タグによるテスト実行の絞り込みスモークテストなど) を参照してください。

## 5. 良いシナリオを書くためのポイント

### 5.1 ビジネス言語で書く

```gherkin
# 悪い例（実装の詳細が漏れている）
Given データベースに memberId="DA-8821" のレコードが INSERT されている
When POST /api/loans に {"memberId": "DA-8821", "bookId": "123"} を送信する

# 良い例（ビジネス要件を反映）
Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
When 会員 "DA-8821" が書籍 "The Infinite Library" を借りる
```

### 5.2 Given はセットアップ、Then は検証

Given と Then で同じ言葉を使うと紛らわしくなります。セットアップと検証で表現を使い分けます:

```gherkin
# Given（セットアップ）: 「登録されている」= この書籍が存在する
Given 書籍 "The Infinite Library" が登録されている

# Then（検証）: 「貸出可能である」= 貸出可能な状態になった
Then 書籍 "The Infinite Library" は貸出可能である
```

### 5.3 一度テストした振る舞いは Given で宣言的に使う

「書籍を借りる」シナリオが既にある場合、他のシナリオでは借りる手順を繰り返す必要はありません:

```gherkin
# 冗長（借りる手順をいちいち書いている）
Given 会員 "DA-8821" が登録されている
And 書籍 "Book 1" が登録されている
And 書籍 "Book 2" が登録されている
When 会員 "DA-8821" が書籍 "Book 1" を借りる
And 会員 "DA-8821" が書籍 "Book 2" を借りる
Then 会員 "DA-8821" の貸出冊数が 2 になる

# 簡潔（状態を宣言するだけ）
Given 会員 "DA-8821" が登録されている
And 会員 "DA-8821" が 2 冊借りている状態である
Then 会員 "DA-8821" の貸出冊数が 2 になる
```

この手法は、テストの関心が「借りる行為」ではなく「借りた後の状態」にある場合に有効です。

### 5.4 曖昧な表現を避ける

```gherkin
# 曖昧（誰が借りているのか不明）
Given 書籍 "Neuromancer" が既に借りられている

# 明確（誰が借りているか分かる）
Given 会員 "田中次郎" (ID: "DA-1156") が登録されている
And 会員 "DA-1156" が書籍 "Neuromancer" を既に借りている
```

### 5.5 自明な検証は書かない

```gherkin
# 冗長（「既に借りている」の直後に貸出冊数1を確認する必要はない）
Given 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
And 会員 "DA-8821" の現在の貸出冊数は 1 である
When 会員 "DA-8821" が書籍 "The Infinite Library" を返却する

# 簡潔（1冊借りているのは前の行から自明）
Given 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
When 会員 "DA-8821" が書籍 "The Infinite Library" を返却する
```

### 5.6 各シナリオは独立させる

シナリオ間で状態を共有してはいけません。各シナリオは単独で実行でき、同じ結果が得られる必要があります。

## 6. 本プロジェクトでの実践

### ディレクトリ構成

```
app/src/
├── main/kotlin/...                    # プロダクションコード
├── test/
│   ├── resources/features/            # feature ファイル（ユニットテスト層）
│   │   ├── borrowing_flow.feature
│   │   ├── book_management.feature
│   │   └── ...
│   └── kotlin/.../steps/              # ステップ定義
│       ├── BorrowingFlowSteps.kt
│       ├── BookManagementSteps.kt
│       └── CommonSteps.kt
└── androidTest/
    ├── assets/features/               # feature ファイル（UI テスト層）
    └── kotlin/.../steps/ui/           # UI ステップ定義（Espresso）
```

### feature ファイルからステップ定義への対応

feature ファイルの各行は、ステップ定義クラスのメソッドに対応します:

**feature ファイル:**
```gherkin
Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
```

**ステップ定義 (Kotlin):**
```kotlin
@Given("会員 {string} \\(ID: {string}) が登録されている")
fun memberIsRegistered(name: String, id: String) {
    val member = Member(id = id, name = name)
    memberRepository.save(member)
}
```

`{string}` や `{int}` はパラメータプレースホルダーで、feature ファイルの値がメソッド引数に渡されます。

### 開発の流れ

#### Step 1: feature ファイルを作成する

`app/src/test/resources/features/` に `.feature` ファイルを作成します。

```gherkin
Feature: 貸出上限
  会員が借りられる冊数には上限がある

  Scenario: 貸出上限に達している場合は借りられない
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 会員 "DA-8821" が 3 冊借りている状態である
    And 書籍 "Neuromancer" が登録されている
    When 会員 "DA-8821" が書籍 "Neuromancer" を借りようとする
    Then エラーメッセージ "貸出上限（3冊）に達しています" が返される
```

#### Step 2: テストを実行して失敗を確認する

```bash
./gradlew test
```

未実装のステップがあれば Cucumber が「undefined step」としてスニペットを提示してくれます。

#### Step 3: ステップ定義を実装する

`app/src/test/kotlin/.../steps/` にステップ定義クラスを作成・追記します。既存のステップが再利用できる場合はそのまま使います。

#### Step 4: プロダクションコードを実装する

テストが通るように `app/src/main/kotlin/` のコードを実装します。

#### Step 5: テストと Lint を確認する

```bash
./gradlew test                    # JVM ユニットテスト実行
./gradlew connectedAndroidTest    # UI テスト実行（エミュレータまたは実機が必要）
./gradlew lint                    # 静的解析
```

> **注意**: `connectedAndroidTest` は Android エミュレータまたは実機が接続・起動されている必要があります。
> エミュレータの起動は Android Studio から行うか、コマンドラインで `emulator -avd <AVD名>` を実行してください。

### タグによるテスト実行の絞り込み（スモークテストなど）

Cucumber はタグ式でシナリオをフィルタリングできます。`@smoke` タグが付いたシナリオだけを実行する（スモークテスト）場合は以下のように指定します。

#### JVM ユニットテスト（Gradle システムプロパティ）

```bash
# @smoke タグのみ実行
./gradlew test -Dcucumber.filter.tags="@smoke"

# @wip を除外（デフォルト推奨）
./gradlew test -Dcucumber.filter.tags="not @wip"

# 複数タグの OR 条件
./gradlew test -Dcucumber.filter.tags="@smoke or @api"
```

#### UI テスト（Gradle プロジェクトプロパティ）

`connectedAndroidTest` では `-P` オプションでタグを指定します。デフォルトは `not @wip`（@wip 以外すべて実行）です:

```bash
# @smoke タグのみ実行
./gradlew connectedAndroidTest -PcucumberTags="@smoke"

# デフォルト（@wip 以外すべて実行）
./gradlew connectedAndroidTest

# 複数タグの OR 条件
./gradlew connectedAndroidTest -PcucumberTags="@smoke or @api"
```

#### タグ式の構文

Cucumber のタグ式は論理演算子をサポートしています:

| 式 | 意味 |
|----|------|
| `@smoke` | `@smoke` タグを持つシナリオ |
| `not @wip` | `@wip` タグを持たないシナリオ |
| `@smoke and not @slow` | `@smoke` かつ `@slow` でないシナリオ |
| `@smoke or @api` | `@smoke` または `@api` のシナリオ |

#### どのシナリオに @smoke を付けるべきか

スモークテストは「主要機能が壊れていないかの疎通確認」が目的です。以下の基準で付与します:

- 各 Feature の代表的な正常系シナリオ（1〜2個）
- ユーザーが最も頻繁に使う操作のシナリオ
- CI で毎回実行しても負担にならない程度の数に絞る

### コミットメッセージ規約

| プレフィックス | フェーズ | 例 |
|---------------|---------|-----|
| `test:` | テスト追加 | `test: 貸出上限の受け入れテストを追加` |
| `impl:` | 実装 | `impl: BorrowBookUseCase に貸出上限チェックを追加` |
| `refactor:` | リファクタリング | `refactor: feature ファイルの Given/Then 表現を整理` |
| `docs:` | ドキュメント | `docs: ATDD ガイドを追加` |
| `build:` | ビルド設定 | `build: Cucumber 依存を追加` |
| `api:` | API 変更 | `api: 貸出 API のレスポンス形式を変更` |

## 7. よくある質問

### Q: シナリオはどのくらい細かく書くべき？

1 シナリオにつき 1 つのルール（振る舞い）をテストします。複数のことを検証するシナリオは、分割を検討してください。

### Q: 正常系と異常系、どちらを先に書く？

正常系を先に書きます。基本的な動作が確認できてから、異常系（エラーケース・境界値）を追加していきます。

### Q: ステップ定義はどこまで再利用すべき？

同じ表現のステップは自然に再利用されます（Cucumber が自動的にマッチングします）。ただし、無理に再利用するために表現を歪めるのは避けてください。読みやすさが最優先です。

### Q: UI テストとユニットテストの feature ファイルは分ける？

本プロジェクトでは分けています。ビジネスロジックのテストは `src/test/resources/features/`（`./gradlew test` で実行、高速・安定）、UI の振る舞いテストは `src/androidTest/assets/features/`（`./gradlew connectedAndroidTest` で実行、エミュレータまたは実機が必要）に配置します。

### Q: feature ファイルは日本語で書いてもいい？

はい。Gherkin はキーワード（Given/When/Then）以外は自由な言語で書けます。本プロジェクトではシナリオ本文を日本語で記述しています。
