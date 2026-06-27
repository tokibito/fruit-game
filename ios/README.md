# Fruit Game — iOS ガワアプリ

GitHub Pages に公開されている Fruit Game（ https://tokibito.github.io/fruit-game/ ）を
WKWebView で表示するだけの **ガワアプリ（ラッパーアプリ）** です。
Android 版（`android/`）と対になる構成で、ゲーム本体は再実装しません。

ゲーム本体（HTML/CSS/JavaScript）はリポジトリの `public_html/` のものをそのまま使い、
Service Worker / IndexedDB によるオフラインキャッシュなどの仕組みも **サイト側の実装をそのまま利用** します。
このため、ゲームの修正は従来どおり `public_html/` を更新して GitHub Pages にデプロイするだけで
アプリ側にも自動反映されます（アプリの再ビルドは不要）。

## ドキュメント一覧

| ドキュメント | 内容 |
| --- | --- |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | アーキテクチャ・ソース構成・WebViewController の責務・主要バージョン |
| [docs/BUILD.md](docs/BUILD.md) | ビルド手順（Xcode / コマンドライン / GitHub Actions）|
| [docs/RELEASE.md](docs/RELEASE.md) | バージョン更新・署名・TestFlight / App Store 配布 |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | よくある問題と対処 |

## 構成の概要

- WKWebView 1枚の全画面 View Controller（`WebViewController.swift`）
- BGM / 効果音の自動再生を許可（`allowsInlineMediaPlayback` / `mediaTypesRequiringUserActionForPlayback = []`）
- Service Worker / IndexedDB は WKWebView が標準対応（サイト側の実装をそのまま利用）
- スワイプ操作で WebView の履歴をたどる（`allowsBackForwardNavigationGestures`）
- 既定のサイト以外への遷移をブロック（後述の「セキュリティ」を参照）
- 遊んでいる間は画面を消さない（`isIdleTimerDisabled`）
- ステータスバー / ホームインジケータを隠して全画面表示
- アプリアイコンは `public_html/images/apple.png` を流用（sky 背景に合成）

| 項目 | 値 |
| --- | --- |
| Bundle Identifier | `io.github.tokibito.fruitgame` |
| Deployment Target | iOS 14.0 |
| 対応デバイス | iPhone / iPad |
| 読み込み先 URL | `https://tokibito.github.io/fruit-game/` |

読み込み先 URL を変える場合は `FruitGame/WebViewController.swift` の `gameURL` を編集してください。
詳細は [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) を参照してください。

## セキュリティ

WKWebView が**既定のサイト（`gameURL`）以外へ遷移しないよう制限**しています。
`WKNavigationDelegate` の `decidePolicyFor navigationAction` で遷移先 URL を検査し、スキーム（`https`）・
ホスト（`tokibito.github.io`）・パス接頭辞（`/fruit-game/`）の **3 つすべてが一致した場合のみ許可**、
それ以外への遷移は `.cancel` でブロックします。

- 万一サイト内に外部リンクや予期しないリダイレクトがあっても、WKWebView が既定 URL の外へ出ることはありません（幼児が意図せず外部サイトへ飛ぶのを防ぎます）。
- 対象はページ遷移（ナビゲーション）です。画像・音声・Service Worker などのサブリソース読み込みには影響しません。
- 許可範囲は `gameURL` から導出するため、読み込み先 URL を変更すると追従します。
- 実装の対応関係は Android 版（`shouldOverrideUrlLoading`）と対になっています。詳細は [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) を参照してください。

## クイックビルド

Xcode プロジェクト（`.xcodeproj`）はリポジトリにコミットせず、[XcodeGen](https://github.com/yonaskolb/XcodeGen) の
`project.yml` から生成します（`.pbxproj` を手書きしないため）。

```bash
cd ios
brew install xcodegen          # 初回のみ
xcodegen generate              # FruitGame.xcodeproj を生成
open FruitGame.xcodeproj       # Xcode で開いて ▶ Run
```

`ios/**` を変更して push すると `.github/workflows/ios.yml` が
シミュレータ向けの署名なしビルドでビルドの成否を検証します。

## 配布について

iOS アプリの実機インストール・配布には **Apple Developer Program（有料）** の登録と
署名が必要です（Android のような「提供元不明のアプリ」での野良配布はできません）。
TestFlight / App Store での配布手順は [docs/RELEASE.md](docs/RELEASE.md) を参照してください。
