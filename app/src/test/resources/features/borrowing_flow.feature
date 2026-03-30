Feature: 貸し出しフロー
  会員が書籍を借りることができる

  Scenario: 会員が書籍を借りる
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 会員 "DA-8821" の貸出冊数は 0 である
    And 書籍 "The Infinite Library" が貸出可能である
    When 会員 "DA-8821" が書籍 "The Infinite Library" を借りる
    Then 書籍 "The Infinite Library" のステータスが "BORROWED" になる
    And 会員 "DA-8821" の貸出冊数が 1 になる
    And 貸出記録が作成される

  Scenario: 既に借りられている書籍は借りられない
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 書籍 "Neuromancer" が既に借りられている
    When 会員 "DA-8821" が書籍 "Neuromancer" を借りようとする
    Then エラーメッセージ "この書籍は既に貸出中です" が返される
    And 書籍 "Neuromancer" のステータスが "BORROWED" のまま変わらない

  Scenario: 複数の書籍を借りる
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 会員 "DA-8821" の貸出冊数は 0 である
    And 書籍 "The Infinite Library" が貸出可能である
    And 書籍 "Foundation" が貸出可能である
    When 会員 "DA-8821" が書籍 "The Infinite Library" を借りる
    And 会員 "DA-8821" が書籍 "Foundation" を借りる
    Then 会員 "DA-8821" の貸出冊数が 2 になる
    And 書籍 "The Infinite Library" のステータスが "BORROWED" になる
    And 書籍 "Foundation" のステータスが "BORROWED" になる

  Scenario: 会員が書籍を返却する
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
    And 会員 "DA-8821" の貸出冊数は 1 である
    When 会員 "DA-8821" が書籍 "The Infinite Library" を返却する
    Then 書籍 "The Infinite Library" のステータスが "AVAILABLE" になる
    And 会員 "DA-8821" の貸出冊数が 0 になる
    And 貸出記録の返却日が記録される

  Scenario: 借りていない書籍は返却できない
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 書籍 "The Infinite Library" が貸出可能である
    When 会員 "DA-8821" が書籍 "The Infinite Library" を返却しようとする
    Then エラーメッセージ "この書籍は貸し出されていません" が返される
