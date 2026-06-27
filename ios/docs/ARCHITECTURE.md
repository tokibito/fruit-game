# アーキテクチャ

Fruit Game の iOS アプリは、GitHub Pages に公開された Web 版ゲームを
**WKWebView で表示するだけのガワアプリ（ラッパーアプリ）** です。
ネイティブでゲームを再実装するのではなく、既存の Web 実装を 1 枚の WKWebView に載せています。
Android 版（`android/`）と同じ思想・同じ読み込み先 URL を使う、対になる実装です。

## なぜガワアプリなのか

- **実装の二重管理を避ける** — ゲーム本体（HTML/CSS/JavaScript）は `public_html/` の 1 か所だけで管理する。
- **アプリ再ビルドが不要** — ゲームの修正は `public_html/` を更新して GitHub Pages にデプロイすれば、アプリ側にも自動で反映される。
- **オフライン対応をサイト側に任せる** — Service Worker / IndexedDB によるキャッシュはサイト側の実装をそのまま利用するため、アプリ側で再実装しない。

トレードオフとして、起動時にネットワークが必要（初回キャッシュ前）・端末性能に依存・WKWebView の挙動差といった
WebView ラッパー特有の制約があります。詳細は [TROUBLESHOOTING.md](TROUBLESHOOTING.md) を参照してください。

## 全体構成

```
┌─────────────────────────────────────────────┐
│ iOS アプリ (io.github.tokibito.fruitgame)     │
│  ┌───────────────────────────────────────┐  │
│  │ WebViewController (全画面・バー非表示)  │  │
│  │  ┌─────────────────────────────────┐  │  │
│  │  │ WKWebView                       │  │  │
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
ios/
├── project.yml                   # XcodeGen のプロジェクト定義（.xcodeproj はこれから生成）
├── FruitGame/
│   ├── AppDelegate.swift         # エントリポイント（Scene 構成を返すだけ）
│   ├── SceneDelegate.swift       # ウィンドウに WebViewController を載せる
│   ├── WebViewController.swift   # WKWebView を構築する唯一の View Controller
│   ├── Info.plist                # Scene 構成・全画面・対応向き
│   └── Assets.xcassets/
│       └── AppIcon.appiconset/   # アプリアイコン（apple.png を sky 背景に合成）
└── docs/                         # 本ドキュメント群
```

> `FruitGame.xcodeproj`（`.pbxproj`）はコミットしません。手書きの `.pbxproj` は壊れやすく差分も追いにくいため、
> [XcodeGen](https://github.com/yonaskolb/XcodeGen) の `project.yml` を唯一の正とし、ビルド前に `xcodegen generate` で生成します。

## WebViewController の責務

`WebViewController.swift` は WKWebView を 1 枚生成し、以下を設定します。
Android 版 `MainActivity.kt` と対になる対応関係です。

| 設定 | 目的 | Android 版の対応 |
| --- | --- | --- |
| `allowsInlineMediaPlayback = true` | インライン再生（全画面プレイヤーに奪われない） | — |
| `mediaTypesRequiringUserActionForPlayback = []` | BGM / 効果音の自動再生 | `mediaPlaybackRequiresUserGesture = false` |
| （標準対応） | Service Worker / IndexedDB | `ServiceWorkerControllerCompat` / `databaseEnabled` |
| `allowsBackForwardNavigationGestures = true` | スワイプで履歴を戻る | 戻るボタン（`OnBackPressedCallback`） |
| `scrollView.bounces = false` | スクロールのバウンスを抑止し固定画面に | `useWideViewPort` / `loadWithOverviewMode` |
| `isIdleTimerDisabled = true` | 遊んでいる間は画面を消さない（幼児向け） | `keepScreenOn = true` |
| `prefersStatusBarHidden` / `prefersHomeIndicatorAutoHidden` | 全画面表示（バーを隠す） | 没入モード（`hideSystemBars`） |

### Service Worker / IndexedDB について

WKWebView は https 経由で読み込んだサイトの Service Worker / IndexedDB を **標準でサポート**します
（iOS 14 以降）。そのため Android のように明示的なコントローラ設定は不要で、サイト側のオフライン実装を
そのまま利用できます。Deployment Target を iOS 14.0 にしているのはこのためです。

### 状態保持と向き

- 画面回転は `Info.plist` の対応向きで許可（iPhone は縦・横、iPad は全向き）。WKWebView がリサイズに追従します。
- アプリはバックグラウンドからの復帰時も WKWebView の状態を保持します（プロセスが生存している限り再読み込み不要）。

## 主要バージョン

| 項目 | 値 |
| --- | --- |
| Bundle Identifier | `io.github.tokibito.fruitgame` |
| Deployment Target | iOS 14.0 |
| 対応デバイス | iPhone / iPad（`TARGETED_DEVICE_FAMILY = 1,2`）|
| Swift | 5.0 |
| プロジェクト生成 | XcodeGen（`project.yml`）|
| ビルド | Xcode 16+ / `xcodebuild`（CI は `macos-15` ランナー）|

## 読み込み先 URL

`WebViewController.swift` の定数 `gameURL`（`https://tokibito.github.io/fruit-game/`）で指定します。
別環境を読み込ませたい場合はこの値を変更して再ビルドしてください。
