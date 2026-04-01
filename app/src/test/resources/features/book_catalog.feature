Feature: 書籍カタログ
  図書館の書籍を一覧表示・検索・フィルタリングできる

  @smoke
  Scenario: 全書籍を表示する
    Given 以下の書籍が登録されている:
      | title                        | author              | isbn           | publicationYear |
      | The Infinite Library         | Jorge Luis Borges   | 978-0142437889 | 1941            |
      | Neuromancer                  | William Gibson      | 978-0441569595 | 1984            |
      | The Left Hand of Darkness    | Ursula K. Le Guin   | 978-0441478125 | 1969            |
      | Foundation                   | Isaac Asimov        | 978-0553293357 | 1951            |
    And 書籍 "Neuromancer" が貸出中になっている
    When 書籍一覧を取得する
    Then 書籍リストに 4 件の書籍が含まれている

  Scenario: 貸出可能な書籍のみ表示する
    Given 以下の書籍が登録されている:
      | title                        | author              | isbn           | publicationYear |
      | The Infinite Library         | Jorge Luis Borges   | 978-0142437889 | 1941            |
      | Neuromancer                  | William Gibson      | 978-0441569595 | 1984            |
      | The Left Hand of Darkness    | Ursula K. Le Guin   | 978-0441478125 | 1969            |
    And 書籍 "Neuromancer" が貸出中になっている
    When 貸出可能な書籍でフィルタする
    Then 書籍リストに 2 件の書籍が含まれている
    And 書籍リストに "Neuromancer" が含まれていない

  Scenario: 貸出中の書籍のみ表示する
    Given 以下の書籍が登録されている:
      | title                        | author              | isbn           | publicationYear |
      | The Infinite Library         | Jorge Luis Borges   | 978-0142437889 | 1941            |
      | Neuromancer                  | William Gibson      | 978-0441569595 | 1984            |
    And 書籍 "Neuromancer" が貸出中になっている
    When 貸出中の書籍でフィルタする
    Then 書籍リストに 1 件の書籍が含まれている
    And 書籍リストに "Neuromancer" が含まれている

  Scenario: 書籍をタイトルで検索する
    Given 以下の書籍が登録されている:
      | title                        | author              | isbn           | publicationYear |
      | The Infinite Library         | Jorge Luis Borges   | 978-0142437889 | 1941            |
      | Neuromancer                  | William Gibson      | 978-0441569595 | 1984            |
      | The Left Hand of Darkness    | Ursula K. Le Guin   | 978-0441478125 | 1969            |
    When 書籍を "Neuromancer" で検索する
    Then 検索結果に 1 件の書籍が含まれている
    And 書籍検索結果に "Neuromancer" が含まれている

  Scenario: 書籍を著者名で検索する
    Given 以下の書籍が登録されている:
      | title                        | author              | isbn           | publicationYear |
      | The Infinite Library         | Jorge Luis Borges   | 978-0142437889 | 1941            |
      | The Left Hand of Darkness    | Ursula K. Le Guin   | 978-0441478125 | 1969            |
    When 書籍を "Borges" で検索する
    Then 検索結果に 1 件の書籍が含まれている
    And 書籍検索結果に "The Infinite Library" が含まれている

  Scenario: 検索結果が0件の場合は空リストが返される
    Given 以下の書籍が登録されている:
      | title                | author            | isbn           | publicationYear |
      | The Infinite Library | Jorge Luis Borges | 978-0142437889 | 1941            |
    When 書籍を "存在しないタイトル" で検索する
    Then 検索結果に 0 件の書籍が含まれている

  Scenario: ISBNで書籍を検索する
    Given 以下の書籍が登録されている:
      | title                | author            | isbn           | publicationYear |
      | The Infinite Library | Jorge Luis Borges | 978-0142437889 | 1941            |
      | Neuromancer          | William Gibson    | 978-0441569595 | 1984            |
    When 書籍を "978-0142437889" で検索する
    Then 検索結果に 1 件の書籍が含まれている
    And 書籍検索結果に "The Infinite Library" が含まれている

  Scenario: Allフィルタで全書籍が表示される
    Given 以下の書籍が登録されている:
      | title                | author            | isbn           | publicationYear |
      | The Infinite Library | Jorge Luis Borges | 978-0142437889 | 1941            |
      | Neuromancer          | William Gibson    | 978-0441569595 | 1984            |
    And 書籍 "Neuromancer" が貸出中になっている
    When 書籍一覧を取得する
    Then 書籍リストに 2 件の書籍が含まれている
    And 書籍リストに "The Infinite Library" が含まれている
    And 書籍リストに "Neuromancer" が含まれている
