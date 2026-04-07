Feature: 会員管理
  図書館の会員を登録・一覧表示・検索できる

  @smoke
  Scenario: 新規会員を登録する
    Given 会員リストが空である
    When 会員 "山田太郎" を登録する
    Then 会員リストに "山田太郎" が含まれている
    And 会員 "山田太郎" の貸出冊数は 0 である

  Scenario: 会員一覧を表示する
    Given 以下の会員が登録されている:
      | id       | name         |
      | DA-8821  | Taro Yamada  |
      | DA-1156  | Marcus Thorne|
      | DA-5509  | Julian Chen  |
    When 会員一覧を取得する
    Then 会員リストに 3 件の会員が含まれている
    And 会員リストの先頭は "Marcus Thorne" である

  Scenario: 会員を名前で検索する
    Given 以下の会員が登録されている:
      | id       | name         |
      | DA-8821  | Taro Yamada  |
      | DA-1156  | Marcus Thorne|
      | DA-5509  | Julian Chen  |
    When 会員を "Marcus" で検索する
    Then 検索結果に 1 件の会員が含まれている
    And 会員検索結果に "Marcus Thorne" が含まれている

  Scenario: 名前が空の場合は登録できない
    Given 会員リストが空である
    When 名前が空で登録しようとする
    Then バリデーションエラー "名前を入力してください" が返される

  Scenario: 検索結果が0件の場合は空リストが返される
    Given 以下の会員が登録されている:
      | id       | name         |
      | DA-8821  | Taro Yamada  |
    When 会員を "存在しない名前" で検索する
    Then 検索結果に 0 件の会員が含まれている

  Scenario: IDで会員を検索する
    Given 以下の会員が登録されている:
      | id       | name         |
      | DA-8821  | Taro Yamada  |
      | DA-1156  | Marcus Thorne|
    When 会員を "DA-8821" で検索する
    Then 検索結果に 1 件の会員が含まれている
    And 会員検索結果に "Taro Yamada" が含まれている
