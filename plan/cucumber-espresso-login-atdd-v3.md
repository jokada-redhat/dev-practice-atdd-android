---
name: Cucumber Espresso ログインATDD (v3)
overview: >
  v2 計画をチームレビューで精緻化した実行計画。
  2イテレーション・3トラック並列で、一切悩まずに実装できるレベルまでタスクを分解。
  cucumber-android はスパイクで Go/No-Go 判断し、No-Go 時は JUnit4+Espresso にフォールバック。
  OkHttp3IdlingResource はアーカイブ済み・AndroidX 非対応のため自作 IdlingResource に変更。
decisions:
  - cucumber-android: スパイクで Go/No-Go 判断。No-Go なら JUnit4+Espresso
  - displayName: 追加する（ATDD プロセスに沿って Feature 先行）
  - scope: 2イテレーション分割
  - error-scenario: エラー系UIシナリオ1本を追加
  - idling-resource: OkHttp3IdlingResource は使用不可 → 自作
  - lifecycle-viewmodel: 2.9.1（compileSdk 36 対応確認済み）
---

# Cucumber + Espresso + モック API によるログイン ATDD (v3)

## 変更履歴

- **v3** (2026-03-23): チームレビューによる精緻化。2イテレーション分割、3トラック並列、
  タスク粒度の細分化、OkHttp3IdlingResource→自作、負荷バランス調整
- **v2** (2026-03-23): 非同期設計・IdlingResource・BASE_URL 注入方式の具体化
- **v1**: 初版

## 意思決定ログ

| # | 判断事項 | 結論 | 理由 |
|---|---------|------|------|
| 1 | cucumber-android 導入 | スパイクで Go/No-Go | ATDD 理念に沿いたいが互換性リスクあり |
| 2 | displayName 追加 | 追加する | UI テストの検証価値。ATDD で Feature 先行 |
| 3 | スコープ | 2イテレーション分割 | リスク軽減。各イテレーションで動くものを確保 |
| 4 | エラー系シナリオ | 1本追加 | textError View の動作検証に必要 |
| 5 | OkHttp3IdlingResource | 使用不可→自作 | アーカイブ済み・AndroidX 非対応 |

## 技術的確定事項

| 項目 | 確定値 | 根拠 |
|------|--------|------|
| lifecycle-viewmodel-ktx | 2.9.1 | compileSdk 36 対応 |
| kotlinx-coroutines-android | 1.10.2 | 最新安定版 |
| activity-ktx | 1.10.1 | viewModels() デリゲート用 |
| IdlingResource | 自作（OkHttp Dispatcher.idleCallback 利用） | OkHttp 5.3.2 で公開 API |
| TestRunner | AndroidJUnitRunner 継承（Go 時に CucumberAndroidJUnitRunner に変更可） | |
| animationsDisabled | true（build.gradle.kts の testOptions） | フレイキーテスト対策 |

## トラック設計

| トラック | 担当 | 責務 |
|---------|------|------|
| Track A | Dev-A | Gradle 設定 + cucumber-android スパイク |
| Track B | Dev-B | ドメイン拡張 + ViewModel + Activity 実装 |
| Track C | Dev-C | Application 基盤 + テストインフラ + レイアウト |

---

## イテレーション 1: build/test 緑 + 手動ログイン→トップ遷移確認

### ゴール

- `./gradlew build` 成功
- `./gradlew test` 全緑（displayName ステップ含む）
- `./gradlew lint` パス（warningsAsErrors = true）
- エミュレータで手動確認: LoginActivity → ログイン → TopActivity に displayName 表示

### クリティカルパス

```
B-1 → B-2 → B-8(+A-1) → B-9(+C-1,C-6) → B-10(+C-7) → B-11
         └→ B-3 → B-4（並列パス、B-8より先に完了想定）
```

### 依存グラフ

```
[A] A-1 ──────────────────────────────────────────────────────┐
    A-2 (独立: スパイク調査)                                    │
    A-3 (depends A-2)                                          │
                                                               │
[B] B-1 → B-2 → B-3 → B-4                                    │
                  └────→ B-8 ──→ B-9 ──→ B-10 ──→ B-11       │
                                  │                            │
[C] C-1 ──→ C-2                   │                            │
       ──→ C-3                    │                            │
       ──→ C-4 (depends A-1)     │                            │
       C-5 (独立) ──→ C-6 ──────┘                             │
                ──→ C-7                                        │
                                        (B-11 needs B-9,B-10,C-1)
```

---

### Track A: Gradle 設定 + スパイク

#### A-1: build.gradle.kts にプロダクション依存 + テスト設定を追加

- **トラック**: Dev-A
- **依存**: なし
- **変更ファイル**: `app/build.gradle.kts`
- **完了条件**:
  - implementation 追加: `lifecycle-viewmodel-ktx:2.9.1`, `kotlinx-coroutines-android:1.10.2`, `activity-ktx:1.10.1`
  - androidTestImplementation 追加: `mockwebserver3:5.3.2`
  - `android { testOptions { animationsDisabled = true } }` 追加
  - `./gradlew build` 成功、`./gradlew test` 全緑
- **作業内容**:
```kotlin
// android { } ブロック内に追加
testOptions {
    animationsDisabled = true
}

// dependencies { } ブロックに追加
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
implementation("androidx.activity:activity-ktx:1.10.1")
androidTestImplementation("com.squareup.okhttp3:mockwebserver3:5.3.2")
```
- **推定**: S

#### A-2: cucumber-android 互換性スパイク（調査）

- **トラック**: Dev-A
- **依存**: なし（A-1 と並行可能）
- **新規ファイル**: `plan/spike-result.md`
- **完了条件**:
  - `io.cucumber:cucumber-android` の最新安定バージョンを特定
  - compileSdk 36 との互換性を Go/No-Go で判定
  - Go: ランナー FQCN、Feature 配置パス、共存方法を記録
  - No-Go: 理由と JUnit4+Espresso フォールバック方針を記録
- **Go/No-Go 基準**:
  1. compileSdk 36 でビルドが通る
  2. Feature ファイルが検出され Step 定義が実行される
  3. 既存テストと共存できる
- **推定**: M

#### A-3: スパイク結果に基づく Cucumber/JUnit4 テスト依存追加

- **トラック**: Dev-A
- **依存**: [A-2]
- **変更ファイル**: `app/build.gradle.kts`
- **完了条件**:
  - **Go**: `cucumber-android` を androidTestImplementation に追加
  - **No-Go**: 追加なし（JUnit4+Espresso で進行）
  - いずれの場合も `testInstrumentationRunner = "com.example.atdd.test.TestRunner"` に変更
  - `./gradlew build` 成功、`./gradlew test` 全緑
- **注記**: `testInstrumentationRunner` は文字列設定のため、TestRunner クラス（C-2）が未作成でも build/test は通る。実際に参照されるのは `connectedAndroidTest` 実行時（イテレーション2）
- **推定**: S

---

### Track B: ドメイン拡張 + ViewModel + Activity 実装

#### B-1: Feature ファイルに displayName 検証ステップを追加（ATDD: Feature 先行）

- **トラック**: Dev-B
- **依存**: なし
- **変更ファイル**: `app/src/test/resources/features/login.feature`, `app/src/test/resources/features/login_api.feature`
- **完了条件**:
  - `login.feature` 正常系に `And 表示名 "テストユーザー" が返される` 追加
  - `login_api.feature` 正常系に `And レスポンスに "displayName" フィールドが含まれる` 追加
  - テストは**失敗する**（ATDD の Red 状態）
- **推定**: S

#### B-2: LoginResult.Success に displayName を追加

- **トラック**: Dev-B
- **依存**: [B-1]
- **変更ファイル**: `app/src/main/kotlin/com/example/atdd/auth/LoginResult.kt`
- **完了条件**:
  - `data class Success(val token: String, val displayName: String) : LoginResult()`
  - コンパイルエラー発生（呼び出し元未更新）— 想定通り
- **作業内容**: L4 を変更
- **推定**: S

#### B-3: AuthApiClient の displayName パース追加

- **トラック**: Dev-B
- **依存**: [B-2]
- **変更ファイル**: `app/src/main/kotlin/com/example/atdd/auth/AuthApiClient.kt`
- **完了条件**:
  - L28 で `LoginResult.Success(token, displayName)` を返す
  - `responseJson.getString("displayName")` でパース
- **作業内容**:
```kotlin
// L28 を変更
200 -> LoginResult.Success(
    responseJson.getString("token"),
    responseJson.getString("displayName")
)
```
- **推定**: S

#### B-4: 既存 JVM テストを displayName 対応に更新

- **トラック**: Dev-B
- **依存**: [B-3]
- **変更ファイル**: `app/src/test/kotlin/com/example/atdd/steps/LoginSteps.kt`, `app/src/test/kotlin/com/example/atdd/steps/LoginApiSteps.kt`
- **完了条件**:
  - LoginSteps.kt L28: `LoginResult.Success("test-token-${request.email}", "テストユーザー")`
  - LoginSteps.kt に `@And("表示名 {string} が返される")` ステップ定義を追加
  - LoginApiSteps.kt L53: `"""{"token":"mock-jwt-token-12345","displayName":"テストユーザー"}"""`
  - `./gradlew test` 全緑
- **推定**: S

#### B-8: LoginViewModel + LoginViewModelFactory + LoginUiState の作成

- **トラック**: Dev-B
- **依存**: [B-2, A-1]（displayName の型 + ViewModel/Coroutine 依存。B-4 完了は推奨 — JVM テスト全緑確認のため。コンパイル上は B-2+A-1 で着手可能）
- **新規ファイル**: `app/src/main/kotlin/com/example/atdd/auth/LoginViewModel.kt`, `app/src/main/kotlin/com/example/atdd/auth/LoginUiState.kt`
- **完了条件**:
  - `LoginUiState` sealed class: `Idle`, `Loading`, `Success(displayName)`, `Error(message)`
  - `LoginViewModel(loginUseCase)`: `viewModelScope.launch(Dispatchers.IO)` で非同期ログイン
  - `uiState: LiveData<LoginUiState>` を公開
  - `LoginViewModelFactory(loginUseCase): ViewModelProvider.Factory`
  - コンパイルが通る
- **作業内容**:
```kotlin
// LoginUiState.kt
package com.example.atdd.auth

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val displayName: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

// LoginViewModel.kt
package com.example.atdd.auth

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {
    private val _uiState = MutableLiveData<LoginUiState>(LoginUiState.Idle)
    val uiState: LiveData<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        _uiState.value = LoginUiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val result = loginUseCase.execute(LoginRequest(email, password))
            withContext(Dispatchers.Main) {
                _uiState.value = when (result) {
                    is LoginResult.Success -> LoginUiState.Success(result.displayName)
                    is LoginResult.Failure -> LoginUiState.Error(result.errorMessage)
                    is LoginResult.ValidationError -> LoginUiState.Error(result.message)
                }
            }
        }
    }
}

class LoginViewModelFactory(
    private val loginUseCase: LoginUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return LoginViewModel(loginUseCase) as T
    }
}
```
- **推定**: M

#### B-9: LoginActivity の実装

- **トラック**: Dev-B
- **依存**: [B-8, C-6, C-1]（ViewModel + レイアウト + AtddApplication）
- **新規ファイル**: `app/src/main/kotlin/com/example/atdd/LoginActivity.kt`
- **完了条件**:
  - AtddApplication から okHttpClient + baseUrl 取得
  - AuthApiClient → LoginUseCase → LoginViewModelFactory → LoginViewModel を構築
  - ログインボタンで viewModel.login() 呼び出し
  - uiState observe: Loading→ボタン無効化、Success→TopActivity 遷移+finish、Error→textError 表示、Idle→初期状態
  - Lint 警告なし
- **作業内容**:
```kotlin
package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.atdd.auth.*

class LoginActivity : AppCompatActivity() {
    private val viewModel: LoginViewModel by viewModels {
        val app = application as AtddApplication
        val client = AuthApiClient(app.okHttpClient, app.baseUrl)
        LoginViewModelFactory(LoginUseCase(client))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val editEmail = findViewById<EditText>(R.id.editEmail)
        val editPassword = findViewById<EditText>(R.id.editPassword)
        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val textError = findViewById<TextView>(R.id.textError)

        buttonLogin.setOnClickListener {
            viewModel.login(editEmail.text.toString(), editPassword.text.toString())
        }

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is LoginUiState.Loading -> {
                    buttonLogin.isEnabled = false
                    textError.visibility = View.GONE
                }
                is LoginUiState.Success -> {
                    startActivity(Intent(this, TopActivity::class.java)
                        .putExtra("displayName", state.displayName))
                    finish()
                }
                is LoginUiState.Error -> {
                    buttonLogin.isEnabled = true
                    textError.text = state.message
                    textError.visibility = View.VISIBLE
                }
                is LoginUiState.Idle -> {
                    buttonLogin.isEnabled = true
                    textError.visibility = View.GONE
                }
            }
        }
    }
}
```
- **推定**: M

#### B-10: TopActivity の実装

- **トラック**: Dev-B
- **依存**: [C-7]（activity_top.xml）
- **新規ファイル**: `app/src/main/kotlin/com/example/atdd/TopActivity.kt`
- **完了条件**:
  - Intent から displayName 取得 → textDisplayName に表示
  - ログアウトボタンで LoginActivity へ遷移（CLEAR_TASK + NEW_TASK）
  - Lint 警告なし
- **作業内容**:
```kotlin
package com.example.atdd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TopActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top)
        findViewById<TextView>(R.id.textDisplayName).text =
            intent.getStringExtra("displayName").orEmpty()
        findViewById<Button>(R.id.buttonLogout).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}
```
- **推定**: S

#### B-11: AndroidManifest.xml 更新 + MainActivity 削除

- **トラック**: Dev-B
- **依存**: [B-9, B-10, C-1]
- **変更ファイル**: `app/src/main/AndroidManifest.xml`
- **削除ファイル**: `app/src/main/kotlin/com/example/atdd/MainActivity.kt`, `app/src/main/res/layout/activity_main.xml`
- **完了条件**:
  - `<application android:name=".AtddApplication">`
  - LoginActivity がランチャー（MAIN + LAUNCHER）
  - TopActivity が登録（exported=false）
  - MainActivity 関連ファイル削除
  - `./gradlew build` 成功、`./gradlew test` 全緑、`./gradlew lint` パス
- **作業内容**:
```xml
<application
    android:name=".AtddApplication"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:supportsRtl="true"
    android:theme="@style/Theme.AppCompat.Light.DarkActionBar">
    <activity android:name=".LoginActivity" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
    <activity android:name=".TopActivity" android:exported="false" />
</application>
```
- **推定**: S

---

### Track C: Application 基盤 + テストインフラ + レイアウト

#### C-1: AtddApplication クラスの作成

- **トラック**: Dev-C
- **依存**: なし
- **新規ファイル**: `app/src/main/kotlin/com/example/atdd/AtddApplication.kt`
- **完了条件**:
  - `AtddApplication : Application()` 継承
  - `var baseUrl: String = "https://production.example.com"`
  - `val okHttpClient: OkHttpClient = OkHttpClient()`
  - コンパイルが通る
- **作業内容**:
```kotlin
package com.example.atdd

import android.app.Application
import okhttp3.OkHttpClient

class AtddApplication : Application() {
    var baseUrl: String = "https://production.example.com"
    val okHttpClient: OkHttpClient = OkHttpClient()
}
```
- **推定**: S

#### C-2: TestRunner クラスの作成

- **トラック**: Dev-C
- **依存**: [C-1]
- **新規ファイル**: `app/src/androidTest/kotlin/com/example/atdd/test/TestRunner.kt`
- **完了条件**:
  - AndroidJUnitRunner 継承
  - newApplication() で AtddApplication を返す
  - コンパイルが通る
- **作業内容**:
```kotlin
package com.example.atdd.test

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.example.atdd.AtddApplication

class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, AtddApplication::class.java.name, context)
    }
}
```
- **推定**: S

#### C-3: 自作 OkHttpIdlingResource の実装

- **トラック**: Dev-C
- **依存**: [C-1]
- **新規ファイル**: `app/src/androidTest/kotlin/com/example/atdd/test/OkHttpIdlingResource.kt`
- **完了条件**:
  - IdlingResource 実装
  - OkHttp Dispatcher の idleCallback + runningCallsCount() で idle 判定
  - `companion object { fun create(name, client) }` ヘルパー
  - コンパイルが通る
- **作業内容**:
```kotlin
package com.example.atdd.test

import androidx.test.espresso.IdlingResource
import okhttp3.OkHttpClient

class OkHttpIdlingResource(
    private val name: String,
    private val dispatcher: okhttp3.Dispatcher
) : IdlingResource {
    @Volatile
    private var callback: IdlingResource.ResourceCallback? = null

    init {
        dispatcher.idleCallback = Runnable { callback?.onTransitionToIdle() }
    }

    override fun getName(): String = name
    override fun isIdleNow(): Boolean {
        val idle = dispatcher.runningCallsCount() == 0
        if (idle) callback?.onTransitionToIdle()
        return idle
    }
    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    companion object {
        fun create(name: String, client: OkHttpClient): OkHttpIdlingResource =
            OkHttpIdlingResource(name, client.dispatcher)
    }
}
```
- **注記**: `idleCallback` と `isIdleNow()` の両方から `onTransitionToIdle()` を呼ぶため二重通知が発生し得る。Espresso は冪等なので通常問題ないが、D-6 で flaky が出た場合は `isIdleNow()` 側を削除し `idleCallback` 一経路に寄せること
- **推定**: S

#### C-4: TestHelper（BaseURL 注入ヘルパー）の実装

- **トラック**: Dev-C
- **依存**: [C-1, A-1]（mockwebserver3 が androidTestImplementation に必要）
- **新規ファイル**: `app/src/androidTest/kotlin/com/example/atdd/test/TestHelper.kt`
- **完了条件**:
  - `getApp()` で AtddApplication 取得
  - `injectMockServerUrl(app, server)` で baseUrl 注入
  - コンパイルが通る
- **作業内容**:
```kotlin
package com.example.atdd.test

import androidx.test.platform.app.InstrumentationRegistry
import com.example.atdd.AtddApplication
import mockwebserver3.MockWebServer

object TestHelper {
    fun getApp(): AtddApplication =
        InstrumentationRegistry.getInstrumentation()
            .targetContext.applicationContext as AtddApplication

    fun injectMockServerUrl(app: AtddApplication, server: MockWebServer) {
        app.baseUrl = server.url("/").toString()
    }
}
```
- **推定**: S

#### C-5: strings.xml に全画面の文字列リソースを追加

- **トラック**: Dev-C
- **依存**: なし（早期着手可能）
- **変更ファイル**: `app/src/main/res/values/strings.xml`
- **完了条件**:
  - ログイン画面・トップ画面の全テキストが定義されている
  - warningsAsErrors の hardcoded text 警告を回避可能
- **作業内容**:
```xml
<resources>
    <string name="app_name">ATDD Android</string>
    <string name="login_title">ログイン</string>
    <string name="email_hint">メールアドレス</string>
    <string name="password_hint">パスワード</string>
    <string name="login_button">ログイン</string>
    <string name="logout_button">ログアウト</string>
    <string name="top_title">トップ</string>
    <string name="email_content_description">メールアドレス入力</string>
    <string name="password_content_description">パスワード入力</string>
    <string name="error_content_description">エラーメッセージ</string>
    <string name="display_name_content_description">表示名</string>
</resources>
```
- **推定**: S

#### C-6: activity_login.xml レイアウト作成

- **トラック**: Dev-C
- **依存**: [C-5]
- **新規ファイル**: `app/src/main/res/layout/activity_login.xml`
- **完了条件**:
  - View ID: `editEmail`(inputType=textEmailAddress), `editPassword`(inputType=textPassword), `buttonLogin`, `textError`
  - 全テキストが `@string/` 参照（hint, contentDescription 含む）
  - `textError` は初期状態で `visibility="gone"`
  - Lint 警告なし
- **推定**: S

#### C-7: activity_top.xml レイアウト作成

- **トラック**: Dev-C
- **依存**: [C-5]
- **新規ファイル**: `app/src/main/res/layout/activity_top.xml`
- **完了条件**:
  - View ID: `textDisplayName`, `buttonLogout`
  - 全テキストが `@string/` 参照、contentDescription 設定済み
  - Lint 警告なし
- **推定**: S

---

### イテレーション 1 完了確認

```bash
./gradlew build    # コンパイル成功
./gradlew test     # JVM テスト全緑（displayName ステップ含む）
./gradlew lint     # Lint パス（warningsAsErrors = true）
# エミュレータで手動確認:
#   LoginActivity → email/password 入力 → ログインボタン → TopActivity に displayName 表示
```

---

## イテレーション 2: connectedDebugAndroidTest 緑

### ゴール

- `./gradlew connectedDebugAndroidTest` 緑（2シナリオ: 正常系 + エラー系）
- `./gradlew test` 引き続き緑
- `./gradlew lint` パス

### クリティカルパス

```
D-2 → D-3(+D-4) → D-5 → D-6
```

### 依存グラフ

```
[A] D-1 (depends A-2/A-3 spike result)
                          │
[B] D-2 → D-3 → D-5 ──→ D-6
                │          │
[C] D-4 ───────┘          │
                    (all converge)
```

---

#### D-1: TestRunner を Cucumber ランナー対応に更新（or 維持）

- **トラック**: Dev-A
- **依存**: [A-2, A-3]
- **変更ファイル**: `app/src/androidTest/kotlin/com/example/atdd/test/TestRunner.kt`
- **完了条件**:
  - **Go**: TestRunner が CucumberAndroidJUnitRunner を継承に変更
  - **No-Go**: 変更なし（AndroidJUnitRunner 継承のまま）
- **推定**: S

#### D-2: androidTest 用 Feature ファイル作成（正常系 + エラー系）

- **トラック**: Dev-B
- **依存**: [A-3]
- **新規ファイル**: `app/src/androidTest/assets/features/login_ui.feature`
- **完了条件**: 2シナリオが記述されている
- **Go 版（Cucumber）**:
```gherkin
Feature: ログイン画面からトップ画面への遷移
  メールアドレスとパスワードでログインし、トップに表示名とログアウトが表示される

  Scenario: 登録済みメールアドレスでログインするとトップに表示名とログアウトが表示される
    Given 未ログイン状態になっている
    And メールアドレス "test@example.com" がパスワード "pass123" で登録されている
    When メールアドレス "test@example.com" とパスワード "pass123" でログインする
    Then 表示名 "山田太郎" がトップページに表示されている
    And ログアウトボタンが表示されている

  Scenario: 誤ったパスワードでログインするとエラーメッセージが表示される
    Given 未ログイン状態になっている
    And メールアドレス "test@example.com" がパスワード "pass123" で登録されている
    When メールアドレス "test@example.com" とパスワード "wrongpass" でログインする
    Then エラーメッセージ "メールアドレスまたはパスワードが正しくありません" が表示されている
```
- **No-Go 版**: Feature ファイルは仕様ドキュメントとして同じ場所に配置（テスト実行はしない）。No-Go 判明時点で Gherkin ドラフトの先行着手可（A-3 の Gradle 変更を待つ必要なし）
- **推定**: S

#### D-3: ステップ定義（or JUnit4 テストクラス）の実装 — 正常系

- **トラック**: Dev-B
- **依存**: [D-2, D-1, D-4]
- **Go 版 新規ファイル**: `app/src/androidTest/kotlin/com/example/atdd/steps/ui/LoginUiSteps.kt`
- **No-Go 版 新規ファイル**: `app/src/androidTest/kotlin/com/example/atdd/test/LoginUiTest.kt`
- **完了条件**:
  - @Before: MockWebServer 起動 → BaseURL 注入 → OkHttpIdlingResource 登録
  - @After: IdlingResource 解除（先）→ MockWebServer 停止（後）— **順序重要**
  - 正常系の全ステップ実装:
    - 「未ログイン状態」→ ActivityScenario.launch(LoginActivity)
    - 「メールアドレス/パスワードで登録されている」→ MockAuthDispatcher 設定
    - 「メールアドレス/パスワードでログインする」→ typeText + closeSoftKeyboard + click
    - 「表示名がトップページに表示されている」→ check(matches(withText()))
    - 「ログアウトボタンが表示されている」→ check(matches(isDisplayed()))
  - コンパイルが通る
- **推定**: M

#### D-4: MockAuthDispatcher の作成

- **トラック**: Dev-C
- **依存**: なし（イテレーション2 開始時に即着手可能）
- **新規ファイル**: `app/src/androidTest/kotlin/com/example/atdd/test/MockAuthDispatcher.kt`
- **完了条件**:
  - `createAuthDispatcher(email, password, displayName): Dispatcher` 関数
  - 200: `{"token":"...", "displayName":"..."}`
  - 401: `{"error":"メールアドレスまたはパスワードが正しくありません"}`
- **作業内容**:
```kotlin
package com.example.atdd.test

import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.RecordedRequest
import org.json.JSONObject

fun createAuthDispatcher(
    registeredEmail: String,
    registeredPassword: String,
    displayName: String
): Dispatcher = object : Dispatcher() {
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.path != "/api/auth/login" || request.method != "POST") {
            return MockResponse.Builder().code(404)
                .body("""{"error":"Not Found"}""").build()
        }
        val body = request.body?.utf8() ?: ""
        val json = JSONObject(body)
        val email = json.optString("email", "")
        val password = json.optString("password", "")
        return if (email == registeredEmail && password == registeredPassword) {
            MockResponse.Builder().code(200)
                .body("""{"token":"mock-token","displayName":"$displayName"}""").build()
        } else {
            MockResponse.Builder().code(401)
                .body("""{"error":"メールアドレスまたはパスワードが正しくありません"}""").build()
        }
    }
}
```
- **推定**: S

#### D-5: エラー系ステップ定義の追加

- **トラック**: Dev-B
- **依存**: [D-3, D-4]
- **変更ファイル**: D-3 で作成したファイル
- **完了条件**:
  - 「エラーメッセージ {string} が表示されている」→ `onView(withId(R.id.textError)).check(matches(withText(msg)))` + `check(matches(isDisplayed()))`
  - コンパイルが通る
- **推定**: S

#### D-6: connectedDebugAndroidTest 実行と調整

- **トラック**: 合流（全員）
- **依存**: [D-5, D-1]
- **変更ファイル**: 必要に応じて調整
- **完了条件**:
  - `./gradlew connectedDebugAndroidTest` 緑
  - `./gradlew test` 緑維持
  - `./gradlew lint` パス
  - IdlingResource 待ち合わせ安定
  - MockWebServer ループバック接続正常
- **注記**:
  - MockWebServer は `server.url("/")` で取得した URL をそのまま使用。`10.0.2.2` は不要（androidTest はアプリと同一プロセス）
  - MockWebServer は `server.start(0)` でランダムポートを使用し、ポート競合を回避
- **推定**: M

---

## タスクサマリー

### イテレーション 1（18 タスク）

| トラック | タスク | S/M 内訳 | 主な成果物 |
|---------|--------|---------|-----------|
| Dev-A | A-1, A-2, A-3 | 1M + 2S | Gradle 設定 + スパイク結果 |
| Dev-B | B-1〜B-4, B-8〜B-11 | 2M + 6S | ドメイン拡張 + UI 全画面 |
| Dev-C | C-1〜C-7 | 7S | AtddApplication + テストインフラ + レイアウト |

### イテレーション 2（6 タスク）

| トラック | タスク | S/M 内訳 | 主な成果物 |
|---------|--------|---------|-----------|
| Dev-A | D-1 | 1S | TestRunner 更新 |
| Dev-B | D-2, D-3, D-5 | 1M + 2S | Feature + ステップ定義 |
| Dev-C | D-4 | 1S | MockDispatcher |
| 合流 | D-6 | 1M | 最終統合検証 |

### 全体クリティカルパス

```
Iter1: B-1 → B-2 → B-8(+A-1) → B-9(+C-1,C-6) → B-10(+C-7) → B-11
                └→ B-3 → B-4（並列、B-8より先に完了想定）
Iter2: D-2 → D-3(+D-4) → D-5 → D-6
```

## テスト環境の前提条件

| 項目 | 設定 | 理由 |
|------|------|------|
| エミュレータ API Level | 26 以上 | minSdk 制約 |
| アニメーション | `animationsDisabled = true` (build.gradle.kts) | Espresso 安定化 |
| 画面ロック | OFF | テスト中のロック回避 |
| 画面スリープ | 無効 or 長時間 | 画面消灯による失敗回避 |
| MockWebServer | `server.url("/")` の URL をそのまま使用 | 同一プロセスで 127.0.0.1 疎通 |

## リスクと対策

| リスク | 影響度 | 対策 |
|--------|--------|------|
| cucumber-android が compileSdk 36 非対応 | 高 | スパイクで事前検証。No-Go 時は JUnit4+Espresso |
| Lint の warningsAsErrors で新規 UI コードが失敗 | 中 | strings.xml 外出し・inputType 明示を徹底 |
| IdlingResource で非同期待ち合わせが不安定 | 中 | 自作 IdlingResource で OkHttp Dispatcher を監視 |
| エミュレータ上の flaky test | 低 | animationsDisabled + IdlingResource |

## 主要タッチファイル一覧

| 領域 | ファイル | 操作 |
|------|---------|------|
| Gradle | `app/build.gradle.kts` | 変更 |
| Application | `AtddApplication.kt` | 新規 |
| ViewModel | `LoginViewModel.kt`, `LoginUiState.kt` | 新規 |
| Activity | `LoginActivity.kt`, `TopActivity.kt` | 新規 |
| Layout | `activity_login.xml`, `activity_top.xml` | 新規 |
| Resources | `strings.xml` | 変更 |
| Manifest | `AndroidManifest.xml` | 変更 |
| Domain | `LoginResult.kt` | 変更 |
| API | `AuthApiClient.kt` | 変更 |
| 既存テスト | `LoginSteps.kt`, `LoginApiSteps.kt` | 変更 |
| Feature(JVM) | `login.feature`, `login_api.feature` | 変更 |
| Feature(androidTest) | `login_ui.feature` | 新規 |
| Step定義(androidTest) | `LoginUiSteps.kt` or `LoginUiTest.kt` | 新規 |
| TestRunner | `TestRunner.kt` | 新規 |
| IdlingResource | `OkHttpIdlingResource.kt` | 新規 |
| TestHelper | `TestHelper.kt` | 新規 |
| MockDispatcher | `MockAuthDispatcher.kt` | 新規 |
| 削除 | `MainActivity.kt`, `activity_main.xml` | 削除 |
