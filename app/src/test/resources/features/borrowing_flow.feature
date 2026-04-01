Feature: 貸し出しフロー
  会員が書籍を借りることができる

  @smoke
  Scenario: 会員が書籍を借りる
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 書籍 "The Infinite Library" が登録されている
    When 会員 "DA-8821" が書籍 "The Infinite Library" を借りる
    Then 書籍 "The Infinite Library" は貸出中である
    And 会員 "DA-8821" の貸出冊数が 1 になる
    And 貸出記録が作成される

  Scenario: 既に借りられている書籍は借りられない
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 会員 "田中次郎" (ID: "DA-1156") が登録されている
    And 会員 "DA-1156" が書籍 "Neuromancer" を既に借りている
    When 会員 "DA-8821" が書籍 "Neuromancer" を借りようとする
    Then エラーメッセージ "この書籍は既に貸出中です" が返される

  Scenario: 複数の書籍を借りている会員の貸出冊数が正しい
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 会員 "DA-8821" が 2 冊借りている状態である
    Then 会員 "DA-8821" の貸出冊数が 2 になる

  Scenario: 会員が書籍を返却する
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
    When 会員 "DA-8821" が書籍 "The Infinite Library" を返却する
    Then 書籍 "The Infinite Library" は貸出可能である
    And 会員 "DA-8821" の貸出冊数が 0 になる
    And 貸出記録が削除される

  Scenario: 借りていない書籍は返却できない
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 書籍 "The Infinite Library" が登録されている
    When 会員 "DA-8821" が書籍 "The Infinite Library" を返却しようとする
    Then エラーメッセージ "この書籍は貸し出されていません" が返される

  Scenario: 存在しない会員IDで書籍を借りようとする
    Given 書籍 "The Infinite Library" が登録されている
    When 存在しない会員 "DA-9999" が書籍 "The Infinite Library" を借りようとする
    Then エラーメッセージ "会員が見つかりません" が返される

  Scenario: 存在しない書籍を借りようとする
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    When 会員 "DA-8821" が存在しない書籍を借りようとする
    Then エラーメッセージ "書籍が見つかりません" が返される

  Scenario: 書籍を借りて返却すると再び貸出可能になる
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 書籍 "The Infinite Library" が登録されている
    When 会員 "DA-8821" が書籍 "The Infinite Library" を借りる
    Then 書籍 "The Infinite Library" は貸出中である
    When 会員 "DA-8821" が書籍 "The Infinite Library" を返却する
    Then 書籍 "The Infinite Library" は貸出可能である
    And 会員 "DA-8821" の貸出冊数が 0 になる

  Scenario: 別の会員が借りている書籍は返却できない
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 会員 "田中次郎" (ID: "DA-1156") が登録されている
    And 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
    When 会員 "DA-1156" が書籍 "The Infinite Library" を返却しようとする
    Then エラーメッセージ "この書籍は別の会員が借りています" が返される
