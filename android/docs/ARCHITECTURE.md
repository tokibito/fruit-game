# アーキテクチャ

Fruit Game の Android アプリは、GitHub Pages に公開された Web 版ゲームを
**WebView で表示するだけのガワアプリ（ラッパーアプリ）** です。
ネイティブでゲームを再実装するのではなく、既存の Web 実装を 1 枚の WebView に載せています。

## なぜガワアプリなのか

- **実装の二重管理を避ける** — ゲーム本体（HTML/CSS/JavaScript）は `public_html/` の 1 か所だけで管理する。
- **アプリ再ビルドが不要** — ゲームの修正は `public_html/` を更新して GitHub Pages にデプロイすれば、アプリ側にも自動で反映される。
- **オフライン対応をサイト側に任せる** — Service Worker / IndexedDB によるキャッシュはサイト側の実装をそのまま利用するため、アプリ側で再実装しない。

トレードオフとして、起動時にネットワークが必要（初回キャッシュ前）・端末性能に依存・WebView の挙動差といった
WebView ラッパー特有の制約があります。詳細は [TROUBLESHOOTING.md](TROUBLESHOOTING.md) を参照してください。

## 全体構成

```
┌─────────────────────────────────────────────┐
│ Android アプリ (io.github.tokibito.fruitgame) │
│  ┌───────────────────────────────────────┐  │
│  │ MainActivity (全画面・没入モード)      │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │ WebView                         │  │  │
│  │  │  └─ https://tokibito.github.io/ │  │  │
│  │  │       fruit-game/               │  │  │
│  │  │     (HTML/CSS/JS + SW + IDB)    │  │  │
│  │  └─────────────────────────────────┘  │  │
│  └───────────────────────────────────────┘  │
└─────────────────────────────────────────────┘
            │ 初回・更新時のみ
            ▼
   GitHub Pages (public_html/ の内容)
```

## ソース構成

```
android/
├── app/
│   ├── build.gradle.kts          # アプリモジュールのビルド設定（SDK / 依存）
│   ├── proguard-rules.pro        # 難読化ルール（ガワのため基本は空）
│   └── src/main/
│       ├── AndroidManifest.xml   # 権限・Activity・テーマ
│       ├── java/io/github/tokibito/fruitgame/
│       │   └── MainActivity.kt   # WebView を構築する唯一の Activity
│       └── res/
│           ├── mipmap-*/         # アプリアイコン（apple.png 由来）
│           └── values/
│               ├── colors.xml    # sky (#87CEEB)
│               ├── strings.xml   # app_name = "Fruit Game"
│               └── themes.xml    # 全画面・タイトルバーなしテーマ
├── build.gradle.kts              # トップレベル（プラグインのバージョン宣言）
├── settings.gradle.kts           # モジュール構成・リポジトリ
├── gradle.properties             # Gradle / AndroidX 設定
└── gradle/wrapper/               # Gradle Wrapper（8.7）
```

## MainActivity の責務

`MainActivity.kt` は WebView を 1 枚生成し、以下を設定します。

| 設定 | 目的 |
| --- | --- |
| `javaScriptEnabled = true` | ゲーム本体は JavaScript |
| `domStorageEnabled = true` | localStorage / sessionStorage |
| `databaseEnabled = true` | IndexedDB のバッキング |
| `mediaPlaybackRequiresUserGesture = false` | BGM / 効果音の自動再生 |
| `useWideViewPort` / `loadWithOverviewMode` | 画面サイズへのフィット |
| `ServiceWorkerControllerCompat` | Service Worker を有効化（オフライン対応） |
| `keepScreenOn = true` | 遊んでいる間は画面を消さない（幼児向け） |
| 没入モード（`hideSystemBars`） | ステータス／ナビゲーションバーを隠して全画面表示 |

### ナビゲーションと状態保持

- `WebViewClient` を設定し、リンク遷移を WebView 内に閉じ込める（外部ブラウザを起動しない）。
- 端末の戻る操作は `OnBackPressedCallback` で受け、WebView に履歴があれば `goBack()`、なければアプリを終了する。
- 画面回転などの構成変更は `AndroidManifest.xml` の `configChanges` で Activity 再生成を抑止し、加えて `onSaveInstanceState` / `restoreState` で WebView の状態を保存・復元する。

## 主要バージョン

| 項目 | 値 |
| --- | --- |
| applicationId / namespace | `io.github.tokibito.fruitgame` |
| minSdk | 24 (Android 7.0) |
| targetSdk / compileSdk | 34 |
| Android Gradle Plugin | 8.5.2 |
| Kotlin | 1.9.24 |
| Gradle | 8.7 |
| JDK | 17 |

依存ライブラリ（`app/build.gradle.kts`）:

- `androidx.core:core-ktx:1.13.1`
- `androidx.appcompat:appcompat:1.7.0`
- `androidx.webkit:webkit:1.11.0`（Service Worker 制御に使用）

## 読み込み先 URL

`MainActivity.kt` の定数 `GAME_URL`（`https://tokibito.github.io/fruit-game/`）で指定します。
別環境を読み込ませたい場合はこの値を変更して再ビルドしてください。
