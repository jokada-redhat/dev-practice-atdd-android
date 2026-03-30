@library
Feature: 会員一覧画面の表示と操作
    トップ画面から会員一覧に遷移し、会員カードが表示される

    Scenario: 会員一覧画面に会員カードが表示される
        Given トップ画面が表示されている
        When 貸し出しカードをタップする
        Then 会員一覧画面が表示される
        And 会員 "Taro Yamada" のカードが表示されている
        And 会員 "Marcus Thorne" のカードが表示されている
        And 会員 "Julian Chen" のカードが表示されている

    Scenario: 会員をタップすると書籍カタログ画面に遷移する
        Given 会員一覧画面が表示されている
        When 会員 "Taro Yamada" のカードをタップする
        Then 書籍カタログ画面が表示される
        And 選択中メンバー "Taro Yamada" が表示されている
