Feature: 貸出一覧からの返却
  貸し出し中の書籍一覧から直接返却できる

  Background:
    Given 会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 書籍 "The Infinite Library" が貸出可能である

  Scenario: 貸出一覧から書籍を返却する
    Given 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
    When 貸出一覧で書籍 "The Infinite Library" の返却ボタンを押す
    Then 書籍 "The Infinite Library" のステータスが "AVAILABLE" になる
    And 会員 "DA-8821" の貸出冊数が 0 になる
    And 貸出記録の返却日が記録される
    And 貸出一覧から書籍 "The Infinite Library" が消える

  Scenario: 返却後に貸出中の冊数表示が更新される
    Given 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
    And 書籍 "Foundation" が貸出可能である
    And 会員 "DA-8821" が書籍 "Foundation" を既に借りている
    When 貸出一覧で書籍 "The Infinite Library" の返却ボタンを押す
    Then 貸出一覧の件数表示が "1冊 貸し出し中" になる

  Scenario: 全て返却すると空状態が表示される
    Given 会員 "DA-8821" が書籍 "The Infinite Library" を既に借りている
    When 貸出一覧で書籍 "The Infinite Library" の返却ボタンを押す
    Then 貸出一覧に "現在貸し出し中の書籍はありません" と表示される
