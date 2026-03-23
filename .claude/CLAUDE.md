# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## プロジェクト概要

Android アプリの ATDD (Acceptance Test Driven Development) 実践リポジトリ。Cucumber による受け入れテスト駆動で Android アプリとバックエンド API を開発する。

## 技術スタック

- **言語**: Kotlin
- **IDE**: Android Studio
- **ビルド**: Gradle (Kotlin DSL)
- **テストフレームワーク**: Cucumber (Gherkin feature files)
- **UIテスト**: Espresso / Compose Testing
- **静的解析**: Android Lint
- **バックエンドAPI**: REST API (テスト用モック/スタブを含む)

## プロジェクト構成

```
app/
├── src/
│   ├── main/
│   │   └── java/...          # プロダクションコード (Kotlin)
│   ├── test/
│   │   ├── resources/
│   │   │   └── features/     # Cucumber feature ファイル (.feature)
│   │   └── java/...          # ユニットテスト・ステップ定義
│   └── androidTest/
│       ├── resources/
│       │   └── features/     # UI向け Cucumber feature ファイル
│       └── java/...          # Instrumented テスト・UIステップ定義
├── build.gradle.kts
└── ...
```

## ATDDワークフロー

1. **受け入れ条件の定義**: ビジネス要件から Cucumber feature ファイルを作成 (.feature)
2. **ステップ定義の実装**: Given-When-Then に対応する Kotlin ステップ定義を作成
3. **機能の実装**: テストが通るようにプロダクションコードを実装
4. **リファクタリング**: テストを維持しながらコードを改善
5. **Lint チェック**: Android Lint で静的解析を実行し品質を確保

## 開発コマンド

```bash
# ビルド
./gradlew build

# ユニットテスト実行
./gradlew test

# Instrumented テスト実行 (エミュレータ/実機が必要)
./gradlew connectedAndroidTest

# Android Lint 実行
./gradlew lint

# 特定の feature ファイルに対するテスト実行
./gradlew test --tests "*CucumberTest*"
```

## 開発原則

### テストファースト

- Cucumber feature ファイルを先に作成
- ステップ定義を Kotlin で実装
- プロダクションコードを実装してテストをパス

### テストシナリオの記述

- **Given-When-Then構造**: 前提条件、アクション、期待結果を明確に分離
- **ビジネス言語**: 技術用語ではなく、ビジネス要件を反映した表現を使用
- **独立性**: 各シナリオは他のシナリオに依存しない
- **再現性**: 同じ条件で実行すれば同じ結果が得られる

### バックエンド API テスト

- API のインターフェース (リクエスト/レスポンス) を先に定義
- Cucumber でAPI の振る舞いを記述
- モック/スタブを活用してフロントエンドとバックエンドを独立にテスト

### コード品質

- Android Lint の警告をゼロに保つ
- 実装コードは適切なレイヤーに配置 (UI / Domain / Data)
- ステップ定義は再利用可能に設計
- 単一責任の原則に従う

## コミットメッセージ規約

ATDDのフェーズを明確にするため、プレフィックスを使用：

- `test:` - 受け入れテスト (feature ファイル / ステップ定義) の追加・修正
- `impl:` - テストを通すための実装
- `refactor:` - リファクタリング
- `docs:` - ドキュメントの更新
- `build:` - ビルド設定・Gradle 設定の変更
- `api:` - バックエンド API 定義・実装の変更

テストと実装を同時にコミットせず、フェーズを分けてコミットすることでATDDのプロセスを明確にします。
