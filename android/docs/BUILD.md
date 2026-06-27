# ビルド手順

Android アプリ（WebView ガワアプリ）のビルド方法をまとめます。
アプリはゲーム本体を含まず GitHub Pages を読み込むだけなので、ビルドに `public_html/` の内容は不要です。

## 前提環境

| ツール | バージョン |
| --- | --- |
| JDK | 17 |
| Android SDK | API 34（compileSdk / targetSdk）|
| Gradle | Wrapper（8.7）が同梱されているため個別インストール不要 |

Android Studio を使う場合は上記が同梱・自動取得されます。
コマンドラインでビルドする場合は JDK 17 と Android SDK を用意し、`ANDROID_HOME`（または `local.properties` の `sdk.dir`）を設定してください。

## Android Studio でビルド

1. Android Studio で `android/` フォルダを開く。
2. Gradle 同期の完了を待つ。
3. ▶ Run で実機 / エミュレータに実行、または
   `Build > Build Bundle(s) / APK(s) > Build APK(s)` で APK を生成する。

## コマンドラインでビルド

```bash
cd android
./gradlew assembleDebug      # デバッグ APK
./gradlew assembleRelease    # リリース APK（署名設定が必要。RELEASE.md を参照）
```

生成物:

- デバッグ APK: `android/app/build/outputs/apk/debug/app-debug.apk`
- リリース APK: `android/app/build/outputs/apk/release/app-release.apk`

その他のよく使うタスク:

```bash
./gradlew clean              # ビルド成果物を削除
./gradlew tasks              # 利用可能なタスク一覧
./gradlew assembleDebug --stacktrace   # 失敗時のスタックトレース表示
```

> Windows ではバッチ版 `gradlew.bat` を使用します。

## GitHub Actions での自動ビルド

`android/**` または `.github/workflows/android.yml` を変更して push すると、
ワークフロー **Build Android APK**（`.github/workflows/android.yml`）が起動します。

- 対象ブランチ: `main` および `claude/**`、Pull Request、手動実行（workflow_dispatch）
- JDK 17 をセットアップし `./gradlew assembleDebug` を実行
- 生成したデバッグ APK を Artifact `fruit-game-debug-apk` としてアップロード

ビルド結果は Actions の実行ページ → Artifacts から `fruit-game-debug-apk` をダウンロードして取得します。

> CI が生成するのは**デバッグ APK** です。正式配布にはリリース署名が必要です（[RELEASE.md](RELEASE.md) を参照）。

## デバッグ APK のインストール

```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

端末側で「提供元不明のアプリ（不明なアプリのインストール）」を許可しておく必要があります。
