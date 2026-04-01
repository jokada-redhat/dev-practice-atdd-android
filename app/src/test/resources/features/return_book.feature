Feature: 貸出一覧からの書籍返却
  貸し出し中の書籍一覧を表示し、検索・絞り込みして返却できる

  Background:
    Given 返却用に会員 "山田太郎" (ID: "DA-8821") が登録されている
    And 返却用に会員 "田中次郎" (ID: "DA-1156") が登録されている
    And 会員 "DA-8821" が書籍 "The Infinite Library" (ISBN: "978-1234567890") を借りている
    And 会員 "DA-8821" が書籍 "Foundation" (ISBN: "978-0553293357") を借りている
    And 会員 "DA-1156" が書籍 "Neuromancer" (ISBN: "978-0441569595") を借りている

  @smoke
  Scenario: 全ての貸出中書籍が一覧表示される
    When 返却画面を開く
    Then 貸出一覧に 3 件表示される

  Scenario: 書籍名で部分一致検索できる
    When 返却画面を開く
    And 検索ボックスに "Infinite" と入力する
    Then 貸出一覧に 1 件表示される
    And 貸出一覧に書籍 "The Infinite Library" が表示される

  Scenario: ISBNで検索できる
    When 返却画面を開く
    And 検索ボックスに "978-0553" と入力する
    Then 貸出一覧に 1 件表示される
    And 貸出一覧に書籍 "Foundation" が表示される

  Scenario: 会員名で検索できる
    When 返却画面を開く
    And 検索ボックスに "田中" と入力する
    Then 貸出一覧に 1 件表示される
    And 貸出一覧に書籍 "Neuromancer" が表示される

  Scenario: 会員IDで検索できる
    When 返却画面を開く
    And 検索ボックスに "DA-8821" と入力する
    Then 貸出一覧に 2 件表示される

  Scenario: 検索結果から書籍を返却できる
    When 返却画面を開く
    And 検索ボックスに "Infinite" と入力する
    And 絞り込み結果から書籍 "The Infinite Library" を返却する
    Then 返却後の書籍 "The Infinite Library" は貸出可能である
    And 返却後の会員 "DA-8821" の貸出冊数が 1 である

  Scenario: 検索をクリアすると全件表示に戻る
    When 返却画面を開く
    And 検索ボックスに "Infinite" と入力する
    Then 貸出一覧に 1 件表示される
    When 検索ボックスをクリアする
    Then 貸出一覧に 3 件表示される
