# Fruit Game

## 技術スタック

- HTML
- CSS
- JavaScript (ビルドなし)
- Service Worker（オフライン対応）
- IndexedDB（キャッシュ管理）

## プロジェクト構成

シンプルなフルーツゲームを実装します。ブラウザで直接動作し、ビルドプロセスを必要としません。外部ライブラリは使用せず、純粋なHTML/CSS/JavaScriptのみで構成されています。

### ファイル構成

公開されるファイルは `public_html/` フォルダにまとめられています。GitHub Pages へは `public_html/` の内容のみがデプロイされます。

#### 公開ファイル（`public_html/`）

- `public_html/index.html` - メインゲームファイル（HTML/CSS/JavaScript全て含む）
- `public_html/images/` - フルーツ画像（apple.png, banana.png, grape.png, kiwi.png, orange.png, melon.png, pineapple.png）
- `public_html/sounds/` - 効果音・BGM（effect_15_dogyun.mp3, effect_37_kirarin.mp3, bgm.mp3）
- `public_html/sw.js` - Service Worker（オフライン対応用）
- `public_html/manifest.json` - PWAマニフェスト
- `public_html/version.json` - バージョン管理

#### Android ガワアプリ（`android/`）

公開サイトを WebView で表示するだけの Android ラッパーアプリです。ゲーム本体は再実装せず `public_html/` を GitHub Pages 経由で読み込むため、ゲームの修正はサイト更新だけでアプリにも反映されます（アプリ再ビルド不要）。

- `android/MainActivity.kt`（`app/src/main/java/io/github/tokibito/fruitgame/`）- WebView を構築する唯一の Activity
- `android/app/build.gradle.kts` - SDK・依存関係・バージョン設定
- `android/README.md` - Android アプリのドキュメント入口
- `android/docs/` - アーキテクチャ・ビルド・リリース・トラブルシュートの各ドキュメント

#### 開発用ファイル（リポジトリ直下、非公開）

- `README.md` - プロジェクト概要
- `spec.md` - ゲーム仕様書
- `CLAUDE.md` - 開発指針（本ファイル）
- `.github/workflows/static.yml` - GitHub Pages デプロイ設定
- `.github/workflows/android.yml` - Android デバッグ APK の自動ビルド設定

### 主な機能

- タッチ/マウス操作対応
- マルチタッチ対応
- ドラッグ&ドロップ機能
- ボーナスフルーツシステム（50個ごと）
- オフライン動作
- PWA対応
