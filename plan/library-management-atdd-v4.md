---
name: Library Management ATDD (v4)
overview: >
  図書館管理機能の ATDD 実装計画。
  v3 (ログイン・セッション管理) 完了後、Digital Archivist デザインシステムを適用した
  図書館管理機能 (会員管理・書籍管理・貸出フロー) を ATDD プロセスで実装する。
  デザインは Stitch で作成した "Book Loan System" を基準に、
  段階的移行 (Option B: ハイブリッド) で進める。
decisions:
  - design-system: Digital Archivist (Stitch) を段階適用 (カラーパレット + No-Line Rule)
  - atdd-process: Feature ファイル先行 → ステップ定義 → プロダクション実装
  - scope: イテレーション3 (Feature + JVM テスト) → イテレーション4 (UI受け入れテスト + 実装)
  - ui-test: cucumber-android (7.18.1) で UI 受け入れテスト
  - architecture: Repository パターン (v3 の AuthRepository を踏襲)
---

# Library Management ATDD (v4)

## 変更履歴

- **v4.0** (2026-03-30): 初版。Digital Archivist デザイン適用済み UI 実装完了。
  ATDD プロセスで機能実装を進める計画を策定。

## 現在の実装状況

### ✅ 完了済み (v3 + プロトタイプ実装)

#### v3: ログイン・セッション管理 (ATDD 完了)
- ログインAPI・ドメインロジック (JVM テスト全緑)
- セッション管理 (SharedPreferences 永続化)
- LoginActivity / TopActivity 実装
- cucumber-android によるUI受け入れテスト (正常系・エラー系)
- 開発用認証スキップ機能 (BuildConfig.SKIP_AUTH)

#### プロトタイプ実装 (v4 計画外の先行実装)
- **デザインシステム**: Digital Archivist カラーパレット + No-Line Rule 適用
- **TopActivity**: 3つのアクションカード (貸し出し/返却/ステータス確認)
- **会員管理**: MemberListActivity (一覧・検索・FAB) + AddMemberActivity (登録フォーム)
- **書籍管理**: BookCatalogActivity (検索・フィルター・一覧・Active Selection)
- **貸し出しフロー**: TopActivity → MemberList → BookCatalog → Toast (成功)

**現状の問題点**:
- ATDD プロセスを経ていない (Feature ファイル未作成)
- ビジネスロジック未実装 (サンプルデータのみ、Repository なし)
- UI受け入れテスト未実装 (手動確認のみ)

---

## v4 計画の目的

1. **ATDD プロセスの再開**: プロトタイプ実装を Feature ファイルベースで再構築
2. **ビジネスロジック実装**: Repository パターンで永続化層を追加
3. **UI受け入れテスト**: cucumber-android で貸し出しフロー全体をテスト
4. **デザイン品質向上**: Stitch デザインシステムの段階的適用継続

---

## デザインシステム方針 (Option B: ハイブリッド)

### ✅ 適用済み
- **カラーパレット**: Digital Archivist (#1A43BF primary 等)
- **No-Line Rule**: strokeWidth="0dp" (tonal layering で境界表現)
- **余白調整**: カード間 20dp, ガター 24dp

### 🟡 保留 (v5 以降)
- **タイポグラフィ**: Manrope + Work Sans フォント追加
- **Glassmorphism**: Toolbar の backdrop blur
- **完全な余白**: 40dp ガター (現状 24dp)

### 📐 Stitch デザイン参照
- プロジェクト: "Book Loan System" (Stitch ID: 15495286656205262218)
- 主要画面:
  - Book Catalog (No Footer)
  - Member List (Neutral)
  - Add New Member (No Footer)

---

## イテレーション設計

### イテレーション 3: Feature + JVM テスト + Repository 実装

**ゴール**:
- ATDD プロセスで Feature ファイル先行
- JVM テスト全緑 (ドメインロジック + Repository)
- `./gradlew test` 成功

**スコープ**:
1. Feature ファイル作成 (3本)
   - `member_management.feature` (会員登録・一覧)
   - `book_catalog.feature` (書籍一覧・フィルタ)
   - `borrowing_flow.feature` (貸し出しフロー: 会員選択 → 書籍選択 → 貸出)

2. ステップ定義 (JVM テスト)
   - `MemberManagementSteps.kt`
   - `BookCatalogSteps.kt`
   - `BorrowingFlowSteps.kt`

3. ドメインモデル拡張
   - `Member` (id, name, email, phone, address, loanCount)
   - `Book` (id, title, author, isbn, publicationYear, status: AVAILABLE/BORROWED)
   - `Loan` (id, memberId, bookId, borrowedDate, returnedDate?)

4. Repository パターン実装
   - `MemberRepository` interface
   - `BookRepository` interface
   - `LoanRepository` interface
   - InMemory 実装 (JVM テスト用)

5. UseCase 実装
   - `RegisterMemberUseCase`
   - `ListMembersUseCase`
   - `SearchBooksUseCase`
   - `BorrowBookUseCase`

**完了条件**:
- `./gradlew test` 全緑
- Feature シナリオが全てパス
- InMemoryRepository で動作確認

---

### イテレーション 4: UI受け入れテスト + 実装統合

**ゴール**:
- cucumber-android で UI受け入れテスト全緑
- プロトタイプ UI と Repository を統合
- `./gradlew connectedDebugAndroidTest` 緑

**スコープ**:
1. androidTest 用 Feature ファイル作成
   - `app/src/androidTest/assets/features/member_ui.feature`
   - `app/src/androidTest/assets/features/book_catalog_ui.feature`
   - `app/src/androidTest/assets/features/borrowing_flow_ui.feature`

2. UI ステップ定義 (Espresso)
   - `MemberUiSteps.kt`
   - `BookCatalogUiSteps.kt`
   - `BorrowingFlowUiSteps.kt`

3. Activity 統合
   - MemberListActivity: InMemoryMemberRepository 注入
   - AddMemberActivity: RegisterMemberUseCase 呼び出し
   - BookCatalogActivity: InMemoryBookRepository 注入
   - 貸し出しボタン: BorrowBookUseCase 呼び出し

4. テストヘルパー拡張
   - `TestHelper.injectRepositories()` - Repository 注入
   - `TestDataFactory` - サンプルデータ生成

**完了条件**:
- `./gradlew connectedDebugAndroidTest` 緑
- 貸し出しフローがE2Eで動作
- UI テストが安定稼働 (IdlingResource 適用)

---

## タスク一覧

### イテレーション 3: Feature + JVM テスト

| ID | タスク | 担当 | 工数 | 依存 | ステータス |
|----|--------|------|------|------|-----------|
| F-1 | member_management.feature 作成 | Dev-A | S | - | ⬜ 未着手 |
| F-2 | book_catalog.feature 作成 | Dev-A | S | - | ⬜ 未着手 |
| F-3 | borrowing_flow.feature 作成 | Dev-A | S | - | ⬜ 未着手 |
| D-1 | ドメインモデル拡張 (Member, Book, Loan) | Dev-B | S | - | ⬜ 未着手 |
| D-2 | Repository interface 定義 x3 | Dev-B | S | D-1 | ⬜ 未着手 |
| D-3 | InMemory Repository 実装 x3 | Dev-B | M | D-2 | ⬜ 未着手 |
| U-1 | RegisterMemberUseCase 実装 | Dev-C | S | D-3 | ⬜ 未着手 |
| U-2 | ListMembersUseCase 実装 | Dev-C | S | D-3 | ⬜ 未着手 |
| U-3 | SearchBooksUseCase 実装 | Dev-C | S | D-3 | ⬜ 未着手 |
| U-4 | BorrowBookUseCase 実装 | Dev-C | M | D-3 | ⬜ 未着手 |
| S-1 | MemberManagementSteps 実装 | Dev-A | M | F-1, U-1, U-2 | ⬜ 未着手 |
| S-2 | BookCatalogSteps 実装 | Dev-A | M | F-2, U-3 | ⬜ 未着手 |
| S-3 | BorrowingFlowSteps 実装 | Dev-A | M | F-3, U-4 | ⬜ 未着手 |
| V-1 | JVM テスト全緑化 | 合流 | M | S-1, S-2, S-3 | ⬜ 未着手 |

**工数見積**: S=1-2h, M=3-4h

---

### イテレーション 4: UI受け入れテスト

| ID | タスク | 担当 | 工数 | 依存 | ステータス |
|----|--------|------|------|------|-----------|
| UI-1 | member_ui.feature 作成 | Dev-A | S | V-1 | ⬜ 未着手 |
| UI-2 | book_catalog_ui.feature 作成 | Dev-A | S | V-1 | ⬜ 未着手 |
| UI-3 | borrowing_flow_ui.feature 作成 | Dev-A | S | V-1 | ⬜ 未着手 |
| UI-4 | MemberUiSteps 実装 | Dev-B | M | UI-1 | ⬜ 未着手 |
| UI-5 | BookCatalogUiSteps 実装 | Dev-B | M | UI-2 | ⬜ 未着手 |
| UI-6 | BorrowingFlowUiSteps 実装 | Dev-B | L | UI-3 | ⬜ 未着手 |
| I-1 | MemberListActivity Repository 統合 | Dev-C | S | UI-4 | ⬜ 未着手 |
| I-2 | AddMemberActivity UseCase 統合 | Dev-C | S | UI-4 | ⬜ 未着手 |
| I-3 | BookCatalogActivity Repository 統合 | Dev-C | S | UI-5 | ⬜ 未着手 |
| I-4 | 貸し出しボタン UseCase 統合 | Dev-C | M | UI-6 | ⬜ 未着手 |
| T-1 | TestHelper.injectRepositories 実装 | Dev-C | S | - | ⬜ 未着手 |
| T-2 | TestDataFactory 実装 | Dev-C | S | - | ⬜ 未着手 |
| V-2 | connectedDebugAndroidTest 緑化 | 合流 | L | I-1~I-4, T-1, T-2 | ⬜ 未着手 |

**工数見積**: S=1-2h, M=3-4h, L=5-6h

---

## Feature ファイル概要 (イテレーション3)

### 1. member_management.feature

```gherkin
Feature: 会員管理
  図書館の会員を登録・一覧表示・検索できる

  Scenario: 新規会員を登録する
    Given 会員リストが空である
    When 会員 "山田太郎" をメールアドレス "taro@example.com" で登録する
    Then 会員リストに "山田太郎" が含まれている
    And 会員 "山田太郎" の貸出冊数は 0 である

  Scenario: 会員一覧を表示する
    Given 以下の会員が登録されている:
      | id       | name         | loanCount |
      | DA-8821  | Taro Yamada  | 2         |
      | DA-1156  | Marcus Thorne| 0         |
    When 会員一覧を取得する
    Then 会員リストに 2 件の会員が含まれている
```

### 2. book_catalog.feature

```gherkin
Feature: 書籍カタログ
  図書館の書籍を一覧表示・検索・フィルタリングできる

  Scenario: 全書籍を表示する
    Given 以下の書籍が登録されている:
      | title                | author           | status    |
      | The Infinite Library | Jorge Luis Borges| AVAILABLE |
      | Neuromancer          | William Gibson   | BORROWED  |
    When 書籍一覧を取得する
    Then 書籍リストに 2 件の書籍が含まれている

  Scenario: 貸出可能な書籍のみ表示する
    Given 以下の書籍が登録されている:
      | title                | author           | status    |
      | The Infinite Library | Jorge Luis Borges| AVAILABLE |
      | Neuromancer          | William Gibson   | BORROWED  |
    When 書籍一覧を "AVAILABLE" でフィルタする
    Then 書籍リストに 1 件の書籍が含まれている
```

### 3. borrowing_flow.feature

```gherkin
Feature: 貸し出しフロー
  会員が書籍を借りることができる

  Scenario: 会員が書籍を借りる
    Given 会員 "山田太郎" (ID: DA-8821) が登録されている
    And 書籍 "The Infinite Library" が貸出可能である
    When 会員 "DA-8821" が書籍 "The Infinite Library" を借りる
    Then 書籍 "The Infinite Library" のステータスが "BORROWED" になる
    And 会員 "DA-8821" の貸出冊数が 1 増える
    And 貸出記録が作成される

  Scenario: 既に借りられている書籍は借りられない
    Given 会員 "山田太郎" (ID: DA-8821) が登録されている
    And 書籍 "Neuromancer" が既に借りられている
    When 会員 "DA-8821" が書籍 "Neuromancer" を借りようとする
    Then エラーメッセージ "この書籍は既に貸出中です" が返される
```

---

## アーキテクチャ設計

### Repository パターン (v3 の AuthRepository を踏襲)

```
UI Layer (Activity)
  ↓ call
UseCase Layer
  ↓ call
Repository Layer (Interface)
  ↓ implements
  - InMemoryRepository (JVM テスト用)
  - SharedPreferencesRepository (実機用、将来)
  - ApiRepository (サーバー連携、将来)
```

### ディレクトリ構成

```
app/src/main/kotlin/com/example/atdd/
├── auth/              # v3 (既存)
│   ├── AuthRepository.kt
│   ├── LoginUseCase.kt
│   └── ...
├── member/            # v4 (新規)
│   ├── MemberRepository.kt
│   ├── RegisterMemberUseCase.kt
│   ├── ListMembersUseCase.kt
│   └── InMemoryMemberRepository.kt
├── book/              # v4 (新規)
│   ├── BookRepository.kt
│   ├── SearchBooksUseCase.kt
│   └── InMemoryBookRepository.kt
├── loan/              # v4 (新規)
│   ├── LoanRepository.kt
│   ├── BorrowBookUseCase.kt
│   └── InMemoryLoanRepository.kt
├── model/             # v4 (既存拡張)
│   ├── Member.kt
│   ├── Book.kt
│   └── Loan.kt       # 新規
├── MemberListActivity.kt
├── BookCatalogActivity.kt
└── ...
```

---

## テスト戦略

### JVM テスト (イテレーション3)
- Feature ファイル駆動
- InMemoryRepository でモック不要
- `./gradlew test` で全緑維持

### UI受け入れテスト (イテレーション4)
- cucumber-android + Espresso
- MockWebServer は不使用 (InMemoryRepository で完結)
- IdlingResource で非同期待ち合わせ (v3 の OkHttpIdlingResource を参考)

---

## リスクと対策

| リスク | 影響度 | 対策 |
|--------|--------|------|
| プロトタイプ UI と Repository 統合の複雑度 | 中 | イテレーション3 で Repository を先に完成させる |
| UI テストの flakiness | 中 | IdlingResource + animationsDisabled 徹底 |
| Feature ファイルの粒度調整 | 低 | イテレーション3 で早期フィードバック |

---

## 次のアクション

1. **イテレーション3 開始**: F-1 (member_management.feature 作成) から着手
2. **ATDD プロセス再開**: Feature 先行 → ステップ定義 → 実装
3. **JVM テスト全緑化**: Repository + UseCase 実装完了
4. **イテレーション4**: UI受け入れテスト + Activity 統合

---

## 参考リンク

- v3 計画書: `plan/cucumber-espresso-login-atdd-v3.md`
- Stitch プロジェクト: "Book Loan System" (ID: 15495286656205262218)
- デザイン比較レポート: `/tmp/stitch_comparison.md` (ローカル)
