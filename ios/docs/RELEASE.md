# リリース・署名・配布

iOS アプリの実機インストールと配布には、**Apple Developer Program（年額の有料登録）** と
コード署名が必須です。Android の「提供元不明のアプリ」のような野良配布（APK 直接配布）はできません。

## バージョンの更新

`project.yml` の `settings.base` を更新し、`xcodegen generate` で反映します。

```yaml
settings:
  base:
    MARKETING_VERSION: "1.0"      # 表示用バージョン（CFBundleShortVersionString）
    CURRENT_PROJECT_VERSION: "1"  # ビルド番号（CFBundleVersion）
```

- `MARKETING_VERSION` は利用者に見える表記（例 `1.0`, `1.1`, `2.0`）。
- `CURRENT_PROJECT_VERSION` は App Store Connect での更新判定に使うビルド番号で、**アップロードのたびに増やす**。

> ゲーム本体（`public_html/`）の更新だけならアプリの再リリースは不要です。
> 再ビルド・再申請が必要なのは、iOS 側の挙動・依存・SDK バージョンなどを変えたときだけです。

## 署名の設定

1. [Apple Developer Program](https://developer.apple.com/programs/) に登録する。
2. App ID（`io.github.tokibito.fruitgame`）を登録する。
3. Xcode の **Signing & Capabilities** で Team を選び、自動署名（Automatic）を有効にする。
   - コマンドライン / CI で署名する場合は、配布証明書とプロビジョニングプロファイルを用意し、
     `project.yml` の `DEVELOPMENT_TEAM` に Team ID を設定する。

> 証明書（`.p12`）・プロビジョニングプロファイル（`.mobileprovision`）・秘密鍵は
> **リポジトリにコミットしない**でください（`.gitignore` 済み。CI では GitHub Secrets 経由で渡す）。

## アーカイブと配布用ビルド

```bash
cd ios
xcodegen generate
xcodebuild \
  -project FruitGame.xcodeproj \
  -scheme FruitGame \
  -configuration Release \
  -sdk iphoneos \
  -archivePath build/FruitGame.xcarchive \
  archive

xcodebuild \
  -exportArchive \
  -archivePath build/FruitGame.xcarchive \
  -exportOptionsPlist ExportOptions.plist \
  -exportPath build/export
```

`ExportOptions.plist` には配布方法（`app-store` / `ad-hoc` / `development`）と Team ID を記載します
（このファイルも `.gitignore` 済み）。

## TestFlight / App Store での配布

1. [App Store Connect](https://appstoreconnect.apple.com/) でアプリレコードを作成する。
2. 生成した `.ipa` を `xcrun altool` または **Transporter** アプリでアップロードする。
3. **TestFlight** で内部 / 外部テスターに配布、または審査に提出して App Store 公開する。
4. ストア掲載情報・スクリーンショット・プライバシーポリシー・年齢区分を設定する。

このアプリは公開サイトを WKWebView で読み込み、外部に個人データを送信しません。
ただし App Store のプライバシー申告（App Privacy）では、利用するネットワーク通信や
ゲーム本体（サイト側）の挙動を確認のうえ正確に申告してください。

## CI で署名ビルドを行う場合

`.github/workflows/ios.yml` は現状、署名なしのシミュレータビルドで**ビルドの成否のみ**を検証します。
CI で `.ipa` を生成・配布する場合は、配布証明書とプロビジョニングプロファイルを Base64 で
GitHub Secrets に格納し、ジョブ内でキーチェーンに復元してから `archive` / `exportArchive` を実行する手順を
追加してください（証明書本体やパスワードはリポジトリに含めないこと）。
