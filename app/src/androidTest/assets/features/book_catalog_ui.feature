@library
Feature: 書籍カタログ画面の表示とフィルタ
    書籍カタログ画面で書籍一覧の表示とフィルタリングができる

    @smoke
    Scenario: 書籍カタログ画面に書籍カードが表示される
        Given 書籍カタログ画面が会員 "Taro Yamada" で表示されている
        Then 書籍 "The Infinite Library" のカードが表示されている
        And 書籍 "Neuromancer" のカードが表示されている
        And 書籍 "Foundation" のカードが表示されている

    Scenario: Availableフィルタで貸出可能な書籍のみ表示する
        Given 書籍カタログ画面が会員 "Taro Yamada" で表示されている
        When "Available" フィルタボタンをタップする
        Then 書籍 "The Infinite Library" のカードが表示されている
        And 書籍 "Neuromancer" のカードが表示されていない

    Scenario: Borrowedフィルタで貸出中の書籍のみ表示する
        Given 書籍カタログ画面が会員 "Taro Yamada" で表示されている
        When "Borrowed" フィルタボタンをタップする
        Then 書籍 "Neuromancer" のカードが表示されている
        And 書籍 "The Infinite Library" のカードが表示されていない
