Feature: 書籍管理
  書籍の一覧表示と新規登録ができる

  Scenario: 登録済みの書籍が一覧表示される
    Given 書籍管理に以下の書籍が登録されている:
      | title               | author        | isbn               | year |
      | The Infinite Library | Jorge Borges  | 978-1234567890     | 2020 |
      | Foundation           | Isaac Asimov  | 978-0553293357     | 1951 |
      | Neuromancer          | William Gibson| 978-0441569595     | 1984 |
    When 書籍一覧を表示する
    Then 書籍一覧に 3 件表示される

  Scenario: 書籍名で部分一致検索できる
    Given 書籍管理に以下の書籍が登録されている:
      | title               | author        | isbn               | year |
      | The Infinite Library | Jorge Borges  | 978-1234567890     | 2020 |
      | Foundation           | Isaac Asimov  | 978-0553293357     | 1951 |
    When 書籍一覧を表示する
    And 書籍一覧で "Infinite" と検索する
    Then 書籍一覧に 1 件表示される
    And 書籍一覧に書籍 "The Infinite Library" が含まれる

  Scenario: ISBNで検索できる
    Given 書籍管理に以下の書籍が登録されている:
      | title               | author        | isbn               | year |
      | The Infinite Library | Jorge Borges  | 978-1234567890     | 2020 |
      | Foundation           | Isaac Asimov  | 978-0553293357     | 1951 |
    When 書籍一覧を表示する
    And 書籍一覧で "978-0553" と検索する
    Then 書籍一覧に 1 件表示される
    And 書籍一覧に書籍 "Foundation" が含まれる

  Scenario: 著者名で検索できる
    Given 書籍管理に以下の書籍が登録されている:
      | title               | author        | isbn               | year |
      | The Infinite Library | Jorge Borges  | 978-1234567890     | 2020 |
      | Foundation           | Isaac Asimov  | 978-0553293357     | 1951 |
    When 書籍一覧を表示する
    And 書籍一覧で "Asimov" と検索する
    Then 書籍一覧に 1 件表示される

  @smoke
  Scenario: 新しい書籍を登録できる
    When 書籍を登録する:
      | title      | author       | isbn           | year |
      | Dune       | Frank Herbert| 978-0441172719 | 1965 |
    Then 書籍 "Dune" が書籍一覧に存在する
    And 書籍 "Dune" の著者が "Frank Herbert" である
    And 書籍 "Dune" のISBNが "978-0441172719" である

  Scenario: タイトル未入力では登録できない
    When タイトル未入力で書籍を登録しようとする
    Then 書籍登録エラー "タイトルを入力してください" が表示される

  Scenario: ISBN重複では登録できない
    Given 書籍管理に以下の書籍が登録されている:
      | title               | author        | isbn               | year |
      | The Infinite Library | Jorge Borges  | 978-1234567890     | 2020 |
    When 重複ISBNで書籍を登録しようとする:
      | title      | author       | isbn           | year |
      | Another    | Author       | 978-1234567890 | 2021 |
    Then 書籍登録エラー "このISBNは既に登録されています" が表示される
