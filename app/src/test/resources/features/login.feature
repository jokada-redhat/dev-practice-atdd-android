Feature: ログイン機能
  ユーザーがメールアドレスとパスワードでログインできる

  Scenario: 正しい認証情報でログインできる
    Given ログインAPIが利用可能である
    And ユーザー "test@example.com" がパスワード "password123" で登録されている
    When メールアドレス "test@example.com" とパスワード "password123" でログインする
    Then ログインが成功する
    And アクセストークンが返される
    And 表示名 "テストユーザー" が返される

  Scenario: 誤ったパスワードではログインできない
    Given ログインAPIが利用可能である
    And ユーザー "test@example.com" がパスワード "password123" で登録されている
    When メールアドレス "test@example.com" とパスワード "wrongpassword" でログインする
    Then ログインが失敗する
    And エラーメッセージ "メールアドレスまたはパスワードが正しくありません" が返される

  Scenario: 未登録のメールアドレスではログインできない
    Given ログインAPIが利用可能である
    When メールアドレス "unknown@example.com" とパスワード "password123" でログインする
    Then ログインが失敗する
    And エラーメッセージ "メールアドレスまたはパスワードが正しくありません" が返される

  Scenario: メールアドレスが空ではログインできない
    When メールアドレス "" とパスワード "password123" でログインする
    Then バリデーションエラー "メールアドレスを入力してください" が発生する

  Scenario: パスワードが空ではログインできない
    When メールアドレス "test@example.com" とパスワード "" でログインする
    Then バリデーションエラー "パスワードを入力してください" が発生する
