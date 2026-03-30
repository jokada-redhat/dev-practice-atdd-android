@library
Feature: 貸し出しフローのE2E操作
    トップ画面から会員選択、書籍選択、貸し出しまでの一連の操作ができる

    Scenario: トップ画面から会員選択を経て書籍を借りる
        Given トップ画面が表示されている
        When 貸し出しカードをタップする
        And 会員 "Taro Yamada" のカードをタップする
        And 書籍 "The Infinite Library" の貸し出しボタンをタップする
        Then 貸し出し成功メッセージが表示される
