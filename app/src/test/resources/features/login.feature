Feature: ログイン機能
  ユーザーがメールアドレスとパスワードでログインできる

  @smoke
  Scenario: 正しい認証情報でログインできる
    Given ログインAPIが利用可能である
    And 以下の認証情報でユーザーが登録されている:
      | email            | password    |
      | test@example.com | password123 |
    When 以下の認証情報でログインする
      | email            | password    |
      | test@example.com | password123 |
    Then ログインが成功する
    And アクセストークンが返される
    And 表示名 "テストユーザー" が返される

  Scenario: 誤ったパスワードではログインできない
    Given ログインAPIが利用可能である
    And 以下の認証情報でユーザーが登録されている:
      | email            | password    |
      | test@example.com | password123 |
    When 以下の認証情報でログインする
      | email            | password      |
      | test@example.com | wrongpassword |
    Then ログインが失敗する
    And エラーメッセージ "メールアドレスまたはパスワードが正しくありません" が返される

  Scenario: 未登録のメールアドレスではログインできない
    Given ログインAPIが利用可能である
    When 以下の認証情報でログインする
      | email               | password    |
      | unknown@example.com | password123 |
    Then ログインが失敗する
    And エラーメッセージ "メールアドレスまたはパスワードが正しくありません" が返される

  Scenario: メールアドレスが空ではログインできない
    When 以下の認証情報でログインする
      | email | password    |
      |       | password123 |
    Then バリデーションエラー "メールアドレスを入力してください" が発生する

  Scenario: パスワードが空ではログインできない
    When 以下の認証情報でログインする
      | email            | password |
      | test@example.com |          |
    Then バリデーションエラー "パスワードを入力してください" が発生する
