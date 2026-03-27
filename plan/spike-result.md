# cucumber-android 互換性スパイク結果

## 判定: Go（条件付き）

## 調査日: 2026-03-27

## 調査結果

| 項目 | 結果 |
|------|------|
| 最新安定バージョン | 7.18.1 (2024年8月リリース) |
| compileSdk 36 互換性 | あり（mainブランチは AGP 9.1.0 + compileSdk 36 で CI 通過） |
| AndroidX 互換性 | 完全互換（CucumberAndroidJUnitRunner は AndroidJUnitRunner 継承） |
| JUnit4+Espresso 共存 | 可能（Runner がサブクラスのため既存テスト影響なし） |
| Runner FQCN | `io.cucumber.android.runner.CucumberAndroidJUnitRunner` |
| Feature 配置パス | `app/src/androidTest/assets/features/` |

## Go の条件

1. JUnit XML レポート出力が不要であること（`javax.xml.stream` 非対応）
2. cucumber-jvm バージョン差異を許容（JVM 側 7.34.3 vs Android 側 7.18.1）
3. リリース頻度が低い（年1回程度）ことを許容

## Gradle 設定

```kotlin
android {
    defaultConfig {
        testInstrumentationRunner = "io.cucumber.android.runner.CucumberAndroidJUnitRunner"
    }
}

dependencies {
    androidTestImplementation("io.cucumber:cucumber-android:7.18.1")
}
```

## 既知の問題

| 問題 | 影響度 | 対策 |
|------|--------|------|
| JUnit XML レポートプラグインが Android 非対応 | 低 | テスト実行には影響なし |
| リリース頻度が低い | 中 | main ブランチは活発。SNAPSHOT ビルドも可能 |
| JVM 側とバージョン不一致 | 低 | Gherkin 構文互換性は問題なし |
