Feature: 会員管理
  図書館の会員を登録・一覧表示・検索できる

  Scenario: 新規会員を登録する
    Given 会員リストが空である
    When 会員 "山田太郎" をメールアドレス "taro@example.com" で登録する
    Then 会員リストに "山田太郎" が含まれている
    And 会員 "山田太郎" の貸出冊数は 0 である

  Scenario: 会員一覧を表示する
    Given 以下の会員が登録されている:
      | id       | name         | email              | loanCount |
      | DA-8821  | Taro Yamada  | taro@example.com   | 2         |
      | DA-1156  | Marcus Thorne| marcus@example.com | 0         |
      | DA-5509  | Julian Chen  | julian@example.com | 1         |
    When 会員一覧を取得する
    Then 会員リストに 3 件の会員が含まれている
    And 会員リストの先頭は "Taro Yamada" である

  Scenario: 会員を名前で検索する
    Given 以下の会員が登録されている:
      | id       | name         | email              | loanCount |
      | DA-8821  | Taro Yamada  | taro@example.com   | 2         |
      | DA-1156  | Marcus Thorne| marcus@example.com | 0         |
      | DA-5509  | Julian Chen  | julian@example.com | 1         |
    When 会員を "Marcus" で検索する
    Then 検索結果に 1 件の会員が含まれている
    And 会員検索結果に "Marcus Thorne" が含まれている

  Scenario: メールアドレスが重複している場合は登録できない
    Given 会員 "山田太郎" がメールアドレス "taro@example.com" で既に登録されている
    When 会員 "山田次郎" をメールアドレス "taro@example.com" で登録しようとする
    Then エラーメッセージ "このメールアドレスは既に登録されています" が返される
    And 会員リストに "山田次郎" が含まれていない

  Scenario: 名前が空の場合は登録できない
    Given 会員リストが空である
    When 名前が空で登録しようとする
    Then バリデーションエラー "名前を入力してください" が返される

  Scenario: メールアドレスが空の場合は登録できない
    Given 会員リストが空である
    When 会員 "山田太郎" をメールアドレス "" で登録しようとする
    Then バリデーションエラー "メールアドレスを入力してください" が返される

  Scenario: メールアドレスの形式が不正な場合は登録できない
    Given 会員リストが空である
    When 会員 "山田太郎" をメールアドレス "invalid-email" で登録しようとする
    Then バリデーションエラー "有効なメールアドレスを入力してください" が返される

  Scenario: 検索結果が0件の場合は空リストが返される
    Given 以下の会員が登録されている:
      | id       | name         | email              | loanCount |
      | DA-8821  | Taro Yamada  | taro@example.com   | 0         |
    When 会員を "存在しない名前" で検索する
    Then 検索結果に 0 件の会員が含まれている

  Scenario: IDで会員を検索する
    Given 以下の会員が登録されている:
      | id       | name         | email              | loanCount |
      | DA-8821  | Taro Yamada  | taro@example.com   | 0         |
      | DA-1156  | Marcus Thorne| marcus@example.com | 0         |
    When 会員を "DA-8821" で検索する
    Then 検索結果に 1 件の会員が含まれている
    And 会員検索結果に "Taro Yamada" が含まれている
