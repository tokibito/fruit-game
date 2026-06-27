# ビルド手順

iOS アプリ（WKWebView ガワアプリ）のビルド方法をまとめます。
アプリはゲーム本体を含まず GitHub Pages を読み込むだけなので、ビルドに `public_html/` の内容は不要です。

## 前提環境

| ツール | バージョン |
| --- | --- |
| macOS | Xcode が動作するバージョン |
| Xcode | 16 以降（iOS 14+ SDK を含む）|
| XcodeGen | 最新（`brew install xcodegen`）|

> iOS のビルドには **macOS と Xcode が必須**です（Android のように Linux ではビルドできません）。
>
> Xcode は **16 以降**が必要です。最新の XcodeGen は Xcode 16 形式（`objectVersion 77`）の
> プロジェクトを生成するため、Xcode 15 では開けません（`future Xcode project file format (77)` エラー）。

## Xcode プロジェクトの生成

`.xcodeproj` はコミットしていません。`project.yml` から生成します。

```bash
cd ios
brew install xcodegen      # 初回のみ
xcodegen generate          # FruitGame.xcodeproj を生成
```

`project.yml` を編集したら、再度 `xcodegen generate` を実行して反映します。

## Xcode でビルド

1. `xcodegen generate` 後に `open FruitGame.xcodeproj` で開く。
2. 上部のスキームで実機またはシミュレータを選ぶ。
3. ▶ Run で実行する。
   - 実機で動かすには Apple Developer アカウントでの署名が必要です（[RELEASE.md](RELEASE.md) を参照）。
   - シミュレータなら署名不要で動作確認できます。

## コマンドラインでビルド

署名なしでシミュレータ向けにビルドする（CI と同じ検証）:

```bash
cd ios
xcodegen generate
xcodebuild \
  -project FruitGame.xcodeproj \
  -scheme FruitGame \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  CODE_SIGNING_ALLOWED=NO \
  build
```

シミュレータで起動して確認する:

```bash
xcrun simctl boot "iPhone 15"
xcodebuild -project FruitGame.xcodeproj -scheme FruitGame \
  -sdk iphonesimulator -destination 'platform=iOS Simulator,name=iPhone 15' build
# 生成された .app を simctl install / launch する
```

## GitHub Actions での自動ビルド

`ios/**` または `.github/workflows/ios.yml` を変更して push すると、
ワークフロー **Build iOS App**（`.github/workflows/ios.yml`）が起動します。

- 対象ブランチ: `main` および `claude/**`、Pull Request、手動実行（workflow_dispatch）
- `macos-15` ランナー（Xcode 16 系が既定）で XcodeGen をインストール → `xcodegen generate`
- 署名なし（`CODE_SIGNING_ALLOWED=NO`）でシミュレータ向けにビルドし、ビルドの成否を検証

> XcodeGen が生成するプロジェクトは Xcode 16 形式のため、それを既定で読める Xcode 16 系が入った
> `macos-15` ランナーを使用しています（Xcode 15.4 が既定の `macos-14` では読めずに失敗します）。

> CI は **ビルドが通ることの検証** までです。配布可能な `.ipa` の生成には署名が必要で、
> Apple Developer アカウントと証明書・プロビジョニングプロファイルが要ります（[RELEASE.md](RELEASE.md) を参照）。
> これらは GitHub Secrets 経由で渡す必要があり、本リポジトリには含めていません。
