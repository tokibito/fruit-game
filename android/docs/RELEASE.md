# リリース・署名・配布

CI が自動生成するのは**デバッグ APK** です。
Google Play への公開や、デバッグ署名以外での正式配布を行うには、リリースビルドの署名設定が必要です。

## デバッグ APK の入手（スマホ向け）

`main` に push されるたびに CI がデバッグ APK をビルドし、GitHub Release（タグ `debug-latest`）に自動で公開します。
ダウンロード URL は毎回同じなので、ブックマークや QR コードにして使えます:

```
https://github.com/tokibito/fruit-game/releases/download/debug-latest/fruit-game-debug.apk
```

- Android スマホのブラウザでこの URL（または [Releases ページ](https://github.com/tokibito/fruit-game/releases)）を開く
- `fruit-game-debug.apk` をタップしてダウンロード → インストール
- 初回は「提供元不明のアプリ」のインストール許可が必要な場合があります
- ログイン不要・期限切れで消えません（GitHub Actions の Artifact とは別物です。Artifact はログインが必要で、zip 圧縮され、保存期限があります）

> これは**デバッグ署名**の APK です。動作確認・社内配布用途で、Google Play 配布には下記のリリース署名が必要です。

## バージョンの更新

`app/build.gradle.kts` の `defaultConfig` を更新します。

```kotlin
defaultConfig {
    // ...
    versionCode = 1      // 整数。リリースのたびに必ず増やす
    versionName = "1.0"  // 利用者に見えるバージョン表記
}
```

- `versionCode` は Google Play で更新判定に使われる整数で、**毎リリース必ずインクリメント**する。
- `versionName` は表示用の文字列（例 `1.0`, `1.1`, `2.0`）。

> ゲーム本体（`public_html/`）の更新だけならアプリの再リリースは不要です。
> アプリの再ビルドが必要なのは、Android 側の挙動・依存・SDK バージョンなどを変えたときだけです。

## リリース署名の設定

### 1. keystore を作成（初回のみ）

```bash
keytool -genkey -v \
  -keystore fruit-game-release.jks \
  -alias fruit-game \
  -keyalg RSA -keysize 2048 -validity 10000
```

> 作成した keystore とパスワードは厳重に保管してください。紛失すると同一アプリとしての更新ができなくなります。
> keystore ファイルや認証情報は**リポジトリにコミットしない**でください（`local.properties` や CI シークレット経由で渡す）。

### 2. `app/build.gradle.kts` に署名設定を追加

認証情報はコードに直書きせず、`local.properties` などから読み込みます。

```kotlin
import java.util.Properties

val keystoreProps = Properties().apply {
    val f = rootProject.file("keystore.properties")
    if (f.exists()) load(f.inputStream())
}

android {
    // ...
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProps.getProperty("storeFile"))
            storePassword = keystoreProps.getProperty("storePassword")
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // isMinifyEnabled は現状 false（ガワアプリのため難読化不要）
        }
    }
}
```

`keystore.properties`（gitignore 対象にすること）:

```properties
storeFile=/absolute/path/to/fruit-game-release.jks
storePassword=********
keyAlias=fruit-game
keyPassword=********
```

### 3. リリースビルド

```bash
cd android
./gradlew assembleRelease    # 署名済み APK
./gradlew bundleRelease      # Play 向け AAB（推奨）
```

- APK: `app/build/outputs/apk/release/app-release.apk`
- AAB: `app/build/outputs/bundle/release/app-release.aab`

## Google Play での配布

1. [Google Play Console](https://play.google.com/console/) でアプリを登録する。
2. **AAB（Android App Bundle）** をアップロードする（Play は AAB を推奨）。
3. ストア掲載情報・スクリーンショット・プライバシーポリシーを設定する。

このアプリは公開サイトを WebView で読み込み、外部に個人データを送信しません。
ただし Play のデータセーフティ申告では、利用するネットワーク通信やゲーム本体（サイト側）の挙動を確認のうえ正確に申告してください。

## CI でリリースビルドを行う場合

`.github/workflows/android.yml` は現状デバッグ APK のみをビルドします。
CI でリリース署名する場合は、keystore を Base64 でエンコードして GitHub Secrets に格納し、
ジョブ内で復元してから `assembleRelease` / `bundleRelease` を実行する手順を追加してください
（keystore 本体やパスワードはリポジトリに含めないこと）。
