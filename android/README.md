# Fruit Game — Android ガワアプリ

GitHub Pages に公開されている Fruit Game（ https://tokibito.github.io/fruit-game/ ）を
WebView で表示するだけの **ガワアプリ（ラッパーアプリ）** です。

ゲーム本体（HTML/CSS/JavaScript）はリポジトリの `public_html/` のものをそのまま使い、
Service Worker / IndexedDB によるオフラインキャッシュなどの仕組みも **サイト側の実装をそのまま利用** します。
このため、ゲームの修正は従来どおり `public_html/` を更新して GitHub Pages にデプロイするだけで
アプリ側にも自動反映されます（アプリの再ビルドは不要）。

## 構成

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

## ビルド方法

### Android Studio
1. Android Studio で `android/` フォルダを開く
2. Gradle 同期が終わったら ▶ Run、または `Build > Build Bundle(s) / APK(s) > Build APK(s)`

### コマンドライン
JDK 17 と Android SDK が必要です（`ANDROID_HOME` を設定）。

```bash
cd android
./gradlew assembleDebug      # デバッグ APK
./gradlew assembleRelease    # リリース APK（署名設定は別途必要）
```

生成物: `android/app/build/outputs/apk/debug/app-debug.apk`

### GitHub Actions
`android/**` を変更して push すると `.github/workflows/android.yml` が
デバッグ APK を自動ビルドし、Actions の Artifact `fruit-game-debug-apk` から
ダウンロードできます。手動実行（workflow_dispatch）も可能です。

## 配布について

CI が生成するのは **デバッグ APK** です。Google Play への公開や正式配布には
リリースビルドの署名（keystore）設定が必要です。必要になったら
`app/build.gradle.kts` に `signingConfigs` を追加してください。
