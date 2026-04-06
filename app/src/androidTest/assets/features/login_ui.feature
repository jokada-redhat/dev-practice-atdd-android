@login
Feature: ログイン画面からトップ画面への遷移
    メールアドレスとパスワードでログインし、トップに表示名とログアウトが表示される

    @smoke
    Scenario: 登録済みメールアドレスでログインするとトップに表示名とログアウトが表示される
        Given 未ログイン状態になっている
        When 以下の認証情報でログインする
            | email                 | password |
            | librarian@example.com | password |
        Then 表示名 "司書 太郎" がトップページに表示されている
        And ログアウトボタンが表示されている

    Scenario: 誤ったパスワードでログインするとエラーメッセージが表示される
        Given 未ログイン状態になっている
        When 以下の認証情報でログインする
            | email                 | password  |
            | librarian@example.com | wrongpass |
        Then エラーメッセージ "メールアドレスまたはパスワードが正しくありません" が表示されている
