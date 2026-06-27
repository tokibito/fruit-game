# Fruit Game — Android ガワアプリ

GitHub Pages に公開されている Fruit Game（ https://tokibito.github.io/fruit-game/ ）を
WebView で表示するだけの **ガワアプリ（ラッパーアプリ）** です。

ゲーム本体（HTML/CSS/JavaScript）はリポジトリの `public_html/` のものをそのまま使い、
Service Worker / IndexedDB によるオフラインキャッシュなどの仕組みも **サイト側の実装をそのまま利用** します。
このため、ゲームの修正は従来どおり `public_html/` を更新して GitHub Pages にデプロイするだけで
アプリ側にも自動反映されます（アプリの再ビルドは不要）。

## ドキュメント一覧

| ドキュメント | 内容 |
| --- | --- |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | アーキテクチャ・ソース構成・MainActivity の責務・主要バージョン |
| [docs/BUILD.md](docs/BUILD.md) | ビルド手順（Android Studio / コマンドライン / GitHub Actions）|
| [docs/RELEASE.md](docs/RELEASE.md) | バージョン更新・リリース署名・Google Play 配布 |
| [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) | よくある問題と対処 |

## 構成の概要

- WebView 1枚の全画面 Activity（`MainActivity.kt`）
- JavaScript / DOM Storage / IndexedDB / Service Worker を有効化
- 端末の戻る操作で WebView の履歴をたどる
- 遊んでいる間は画面を消さない（`keepScreenOn`）
- アプリアイコンは `apple.png` を流用

| 項目 | 値 |
| --- | --- |
| applicationId | `io.github.tokibito.fruitgame` |
| minSdk | 24 (Android 7.0) |
| targetSdk / compileSdk | 34 |
| 読み込み先 URL | `https://tokibito.github.io/fruit-game/` |

読み込み先 URL を変える場合は `MainActivity.kt` の `GAME_URL` を編集してください。
詳細は [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) を参照してください。

## クイックビルド

```bash
cd android
./gradlew assembleDebug      # デバッグ APK
```

生成物: `android/app/build/outputs/apk/debug/app-debug.apk`

`android/**` を変更して push すると `.github/workflows/android.yml` が
デバッグ APK を自動ビルドし、Actions の Artifact `fruit-game-debug-apk` から
ダウンロードできます。手順の詳細は [docs/BUILD.md](docs/BUILD.md) を参照してください。

## 配布について

CI が生成するのは **デバッグ APK** です。Google Play への公開や正式配布には
リリースビルドの署名（keystore）設定が必要です。手順は [docs/RELEASE.md](docs/RELEASE.md) を参照してください。
