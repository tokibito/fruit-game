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

- `index.html` - メインゲームファイル（HTML/CSS/JavaScript全て含む）
- `images/` - フルーツ画像（apple.png, banana.png, grape.png, kiwi.png, orange.png, melon.png, pineapple.png）
- `sw.js` - Service Worker（オフライン対応用）
- `manifest.json` - PWAマニフェスト
- `version.json` - バージョン管理
- `spec.md` - ゲーム仕様書
- `CLAUDE.md` - 開発指針（本ファイル）

### 主な機能

- タッチ/マウス操作対応
- マルチタッチ対応
- ドラッグ&ドロップ機能
- ボーナスフルーツシステム（50個ごと）
- オフライン動作
- PWA対応
