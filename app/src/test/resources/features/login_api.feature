Feature: ログインAPI
  バックエンドのログインAPIの振る舞いを検証する

  Scenario: 正しい認証情報でトークンが返る
    Given ログインAPIサーバーが起動している
    When POST "/api/auth/login" に以下のJSONを送信する:
      | email    | test@example.com |
      | password | password123      |
    Then レスポンスステータスが 200 である
    And レスポンスに "token" フィールドが含まれる
    And レスポンスに "displayName" フィールドが含まれる

  Scenario: 誤った認証情報で401が返る
    Given ログインAPIサーバーが起動している
    When POST "/api/auth/login" に以下のJSONを送信する:
      | email    | test@example.com |
      | password | wrongpassword    |
    Then レスポンスステータスが 401 である
    And レスポンスに "error" フィールドが含まれる

  Scenario: 不正なリクエスト形式で400が返る
    Given ログインAPIサーバーが起動している
    When POST "/api/auth/login" に以下のJSONを送信する:
      | email | invalid-email |
    Then レスポンスステータスが 400 である
