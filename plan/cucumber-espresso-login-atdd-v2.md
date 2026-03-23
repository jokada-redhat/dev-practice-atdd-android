---
name: Cucumber Espresso ログインATDD (v2)
overview: >
  日本語の Gherkin シナリオを androidTest に追加し、MockWebServer で認証 API をモックしつつ
  Espresso で UI を操作する instrumented ATDD を構成する。
  アカウント識別子はメールアドレスに統一し、JSON も email + password の契約を維持する。
  v1 からの主な変更: 非同期設計(ViewModel+Coroutine)の明記、IdlingResource の必須化、
  BASE_URL 注入方式の確定、cucumber-android スパイクタスクの追加。
todos:
  - id: spike-cucumber-android
    content: >
      cucumber-android の利用可能バージョン・ランナーFQCN・compileSdk 36 との互換性を
      実機ビルドで確認するスパイクタスク。失敗時の代替案(素の Espresso + JUnit4)も検討
    status: pending
  - id: domain-email-api
    content: >
      LoginResult.Success に displayName を追加。AuthApiClient の 200 レスポンスパースを拡張。
      既存 JVM テスト(LoginSteps / LoginApiSteps / feature)は displayName 追加分のみ最小更新
    status: pending
    depends_on: []
  - id: ui-login-top
    content: >
      LoginActivity(メール・パスワード入力 + ログインボタン)、TopActivity(displayName 表示 + ログアウト)を実装。
      LoginViewModel で Coroutine(Dispatchers.IO)による非同期ログインを行い、LiveData/StateFlow で結果を通知。
      成功時に TopActivity へ Intent で displayName を渡して遷移。
      未ログイン時はランチャーを LoginActivity にし、ログアウトで LoginActivity に戻る。
      Lint 対応: contentDescription・strings.xml 外出しなど warningsAsErrors = true をパスする設計
    status: pending
    depends_on: [domain-email-api]
  - id: idling-resource
    content: >
      OkHttp の非同期呼び出しを Espresso に同期させる IdlingResource を実装。
      OkHttp3IdlingResource(square 公式)または CountingIdlingResource を採用し、
      テストの @Before で register、@After で unregister する。
      プロダクション側は OkHttpClient をシングルトン化して IdlingResource を attach 可能にする
    status: pending
    depends_on: [ui-login-top]
  - id: base-url-injection
    content: >
      Application クラス(AtddApplication)に var baseUrl を定義し、デフォルトは本番 URL。
      テスト用 TestRunner(AndroidJUnitRunner 継承)で TestApplication を差し替え、
      MockWebServer の URL を baseUrl に注入する。
      AuthApiClient は Application.baseUrl を参照する構成にする
    status: pending
    depends_on: [ui-login-top]
  - id: gradle-cucumber-android
    content: >
      app/build.gradle.kts に cucumber-android(spike で確定したバージョン)、
      androidTestImplementation の mockwebserver3、lifecycle-viewmodel-ktx / coroutines を追加。
      testInstrumentationRunner をカスタム TestRunner(cucumber ランナー継承)に変更。
      既存 JUnit4 テストとの共存を確認
    status: pending
    depends_on: [spike-cucumber-android]
  - id: androidtest-feature-glue
    content: >
      androidTest/assets/features/ に Gherkin(メールアドレス表現)を配置。
      Step 定義で MockWebServer 起動 + BASE_URL 注入 + Espresso 操作を実装。
      IdlingResource の register/unregister をステップライフサイクルに組み込む
    status: pending
    depends_on: [gradle-cucumber-android, idling-resource, base-url-injection]
  - id: verify-connected-test
    content: >
      connectedDebugAndroidTest で緑になるまで調整。
      フレイキー対策として IdlingResource の動作確認、
      エミュレータ上のループバック(127.0.0.1)接続確認を含む
    status: pending
    depends_on: [androidtest-feature-glue]
---

# Cucumber + Espresso + モック API によるログイン ATDD (v2)

## 変更履歴

- **v2** (2026-03-23): 非同期設計・IdlingResource・BASE_URL 注入方式の具体化、スパイクタスク追加
- **v1**: 初版

## 現状の整理

- [app/build.gradle.kts](../app/build.gradle.kts): Cucumber は JVM の `test` のみ。Espresso は `androidTestImplementation` のみで `src/androidTest` は未作成
- 既存 Feature: [login.feature](../app/src/test/resources/features/login.feature)(UseCase/InMemory)、[login_api.feature](../app/src/test/resources/features/login_api.feature)(MockWebServer で HTTP 検証)
- ドメインは `LoginRequest(email, password)`、JSON も `email` フィールド
- UI 未実装: `MainActivity` は空シェル、ViewModel / 画面遷移なし
- DI フレームワーク未導入(Hilt/Dagger/Koin なし)

## タスク依存グラフ

```
spike-cucumber-android ──> gradle-cucumber-android ──┐
                                                     │
domain-email-api ──> ui-login-top ──> idling-resource ──> androidtest-feature-glue ──> verify-connected-test
                                  ──> base-url-injection ─┘
```

## 0. スパイク: cucumber-android 互換性確認 (spike-cucumber-android)

### 目的

本格実装の前に以下を確認し、Go/No-Go を判断する:

1. `io.cucumber:cucumber-android` の最新安定バージョンと `compileSdk 36` の互換性
2. ランナーの完全修飾クラス名(FQCN)
3. Feature ファイルの配置パスとグルーコードの検出
4. 既存の `AndroidJUnitRunner` ベースの Espresso テストとの共存可否

### 代替案(cucumber-android が使えない場合)

素の JUnit4 + Espresso でテストを書き、Feature ファイルは仕様ドキュメントとしてのみ管理する。
Step 定義の代わりに `@Test` メソッドに Given/When/Then をコメントで記述。

## 1. ドメイン・API 契約の拡張 (domain-email-api)

- `LoginResult.Success` に `displayName: String` を追加
- `AuthApiClient`: 200 レスポンスから `displayName` もパース

```kotlin
// LoginResult.kt
sealed class LoginResult {
    data class Success(val token: String, val displayName: String) : LoginResult()
    data class Failure(val errorMessage: String) : LoginResult()
    data class ValidationError(val message: String) : LoginResult()
}
```

- 既存 JVM テスト更新:
  - `LoginApiSteps`: MockWebServer の 200 レスポンスに `"displayName":"テストユーザー"` 追加
  - `LoginSteps`: InMemory の成功結果に displayName 追加
  - Feature ファイルは変更不要(token の存在チェックのみのため)

## 2. UI 実装 (ui-login-top)

### 2.1 アーキテクチャ

```
LoginActivity ──> LoginViewModel ──> LoginUseCase ──> AuthApiClient
     │                  │
     │            LiveData<UiState>
     │                  │
     ▼                  ▼
 (入力/クリック)    (画面状態更新)
     │
     ▼ (成功時)
TopActivity ← Intent(displayName)
```

### 2.2 非同期設計(v1 で欠落していた点)

`AuthApiClient.login()` は同期的な `OkHttpClient.execute()` を使用しており、
メインスレッドから呼ぶと `NetworkOnMainThreadException` でクラッシュする。

**解決策**: `LoginViewModel` で `viewModelScope.launch(Dispatchers.IO)` を使用:

```kotlin
class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _uiState = MutableLiveData<LoginUiState>()
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
```

### 2.3 画面一覧

| 画面 | Activity | 主要 View ID |
|------|----------|-------------|
| ログイン | `LoginActivity` | `editEmail`, `editPassword`, `buttonLogin`, `textError` |
| トップ | `TopActivity` | `textDisplayName`, `buttonLogout` |

### 2.4 Manifest 構成

```xml
<activity android:name=".LoginActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
<activity android:name=".TopActivity" />
```

### 2.5 Lint 対応

- `warningsAsErrors = true` のため、以下を事前対応:
  - 全テキストを `strings.xml` に外出し(hardcoded text 警告回避)
  - ImageView 等には `contentDescription` を付与
  - `EditText` には `inputType` を明示(`textEmailAddress`, `textPassword`)

## 3. IdlingResource (idling-resource)

v1 では「必要に応じて」としていたが、**非同期ネットワーク呼び出しがある以上必須**。

### 方針

`OkHttpClient` をアプリ全体でシングルトン化し、`IdlingResource` を attach する:

```kotlin
// AtddApplication.kt
class AtddApplication : Application() {
    var baseUrl: String = "https://production.example.com"
    val okHttpClient: OkHttpClient = OkHttpClient()
}
```

テスト側:

```kotlin
// テストの @Before
val idlingResource = OkHttp3IdlingResource.create("OkHttp", app.okHttpClient)
IdlingRegistry.getInstance().register(idlingResource)

// テストの @After
IdlingRegistry.getInstance().unregister(idlingResource)
```

依存追加:

```kotlin
androidTestImplementation("com.jakewharton.espresso:okhttp3-idling-resource:1.0.0")
```

> **注意**: `okhttp3-idling-resource` が OkHttp 5.x (`mockwebserver3`) と互換性があるか
> スパイクで確認する。非互換の場合は `CountingIdlingResource` で手動カウントする。

## 4. BASE_URL 注入 (base-url-injection)

### v1 からの変更: 具体策を1つに確定

**採用案: Application クラスによる注入**

DI フレームワークなしで最もシンプルに実現できる方式。

#### プロダクション側

```kotlin
class AtddApplication : Application() {
    var baseUrl: String = "https://production.example.com"
    val okHttpClient: OkHttpClient = OkHttpClient()
}
```

`LoginViewModel` / `AuthApiClient` は `(application as AtddApplication).baseUrl` を参照。

#### テスト側

```kotlin
class TestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, AtddApplication::class.java.name, context)
    }
}
```

Step 定義の `@Before` で:

```kotlin
val app = InstrumentationRegistry.getInstrumentation()
    .targetContext.applicationContext as AtddApplication
server = MockWebServer()
server.start()
app.baseUrl = server.url("/").toString()
```

## 5. Gradle 設定 (gradle-cucumber-android)

### 追加依存(予定、バージョンはスパイクで確定)

```kotlin
// ViewModel + Coroutine(プロダクション)
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

// Cucumber Android(instrumented テスト)
androidTestImplementation("io.cucumber:cucumber-android:X.Y.Z")  // spike で確定

// MockWebServer(instrumented テスト)
androidTestImplementation("com.squareup.okhttp3:mockwebserver3:5.3.2")

// IdlingResource
androidTestImplementation("com.jakewharton.espresso:okhttp3-idling-resource:1.0.0")
```

### testInstrumentationRunner

```kotlin
defaultConfig {
    testInstrumentationRunner = "com.example.atdd.test.TestRunner"
}
```

### 既存テストとの共存

- JVM テスト(`testImplementation`)は影響なし
- `androidTestImplementation` の既存 Espresso/JUnit は、Cucumber ランナーが JUnit4 も
  実行できるか確認(スパイクで検証)。不可なら `testInstrumentationRunnerArguments` で制御

## 6. Feature ファイルとステップ定義 (androidtest-feature-glue)

### Feature ファイル

`src/androidTest/assets/features/login_ui.feature`:

```gherkin
Feature: ログインからトップ表示まで
  メールアドレスとパスワードでログインし、トップに表示名とログアウトが表示される

  Scenario: 登録済みメールアドレスでログインするとトップに表示名とログアウトが表示される
    Given 未ログイン状態になっている
      And メールアドレス "test@example.com" がパスワード "pass123" で登録されている
    When メールアドレス "test@example.com" とパスワード "pass123" でログインする
    Then 表示名 "山田太郎" がトップページに表示されている
      And ログアウトボタンが表示されている
```

### Step 定義の責務

```kotlin
// com.example.atdd.steps.ui.LoginUiSteps (androidTest)
class LoginUiSteps {
    private lateinit var server: MockWebServer
    private lateinit var scenario: ActivityScenario<LoginActivity>

    @Before  // Cucumber の @Before
    fun setUp() {
        // 1. MockWebServer 起動
        server = MockWebServer()
        server.start()

        // 2. BASE_URL 注入
        val app = getApp()
        app.baseUrl = server.url("/").toString()

        // 3. IdlingResource 登録
        val idling = OkHttp3IdlingResource.create("OkHttp", app.okHttpClient)
        IdlingRegistry.getInstance().register(idling)
    }

    @Given("未ログイン状態になっている")
    fun userIsNotLoggedIn() {
        // SharedPreferences クリア(将来のセッション管理用)
        scenario = ActivityScenario.launch(LoginActivity::class.java)
    }

    @Given("メールアドレス {string} がパスワード {string} で登録されている")
    fun userIsRegistered(email: String, password: String) {
        // MockWebServer のディスパッチャに認証成功パターンを登録
        server.dispatcher = createDispatcher(email, password, "山田太郎")
    }

    @When("メールアドレス {string} とパスワード {string} でログインする")
    fun login(email: String, password: String) {
        onView(withId(R.id.editEmail)).perform(typeText(email), closeSoftKeyboard())
        onView(withId(R.id.editPassword)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.buttonLogin)).perform(click())
    }

    @Then("表示名 {string} がトップページに表示されている")
    fun displayNameIsShown(name: String) {
        onView(withId(R.id.textDisplayName)).check(matches(withText(name)))
    }

    @Then("ログアウトボタンが表示されている")
    fun logoutButtonIsShown() {
        onView(withId(R.id.buttonLogout)).check(matches(isDisplayed()))
    }

    @After
    fun tearDown() {
        server.close()
        IdlingRegistry.getInstance().unregister(/* ... */)
    }
}
```

## 7. 実行と検証 (verify-connected-test)

```bash
# エミュレータ起動済みの状態で
./gradlew :app:connectedDebugAndroidTest
```

### 確認ポイント

- [ ] Feature シナリオが全て緑
- [ ] IdlingResource により待ち合わせが安定
- [ ] MockWebServer のループバック接続が正常(エミュレータ: `127.0.0.1`)
- [ ] 既存 JVM テスト(`./gradlew test`)が引き続き緑
- [ ] Lint チェック(`./gradlew lint`)がパス

## リスクと対策

| リスク | 影響度 | 対策 |
|--------|--------|------|
| cucumber-android が compileSdk 36 非対応 | 高 | スパイクで事前検証。代替: 素の JUnit4 + Espresso |
| OkHttp3IdlingResource が OkHttp 5.x 非互換 | 中 | CountingIdlingResource にフォールバック |
| Cucumber ランナーが既存 JUnit4 テストを実行不可 | 中 | testInstrumentationRunnerArguments で分離 or テストスイート分割 |
| Lint の warningsAsErrors で新規 UI コードが失敗 | 低 | strings.xml 外出し・inputType 明示を徹底 |
| エミュレータ vs 実機のループバックアドレス差異 | 低 | 同一プロセスなら 127.0.0.1 で統一。README に注記 |

## 主要タッチファイル

| 領域 | ファイル |
|------|----------|
| Gradle | `app/build.gradle.kts` |
| Application | `AtddApplication.kt`(新規) |
| ViewModel | `LoginViewModel.kt`, `LoginUiState.kt`(新規) |
| Activity | `LoginActivity.kt`, `TopActivity.kt`(新規) |
| Layout | `activity_login.xml`, `activity_top.xml`(新規) |
| Manifest | `AndroidManifest.xml` |
| ドメイン | `LoginResult.kt`(displayName 追加) |
| API | `AuthApiClient.kt`(displayName パース追加) |
| Feature(androidTest) | `src/androidTest/assets/features/login_ui.feature`(新規) |
| Glue(androidTest) | `src/androidTest/kotlin/.../steps/ui/LoginUiSteps.kt`(新規) |
| TestRunner | `src/androidTest/kotlin/.../test/TestRunner.kt`(新規) |
| IdlingResource | テストコード内で OkHttp3IdlingResource を利用 |
| 既存テスト | `LoginSteps.kt`, `LoginApiSteps.kt`(displayName 対応の最小更新) |
