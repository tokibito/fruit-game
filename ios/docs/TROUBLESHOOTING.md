# トラブルシューティング

WKWebView ガワアプリ特有の問題と対処をまとめます。

## ゲームが表示されない / 白い画面のまま

- **初回はネットワークが必要**: 初回起動時はサイト（GitHub Pages）の読み込みとキャッシュ作成のため通信が必要です。オフラインのまま初回起動するとキャッシュが無く表示できません。一度オンラインで起動すれば、以降は Service Worker / IndexedDB のキャッシュで動作します。
- **URL を確認**: `WebViewController.swift` の `gameURL` が正しいか確認します。
- **ATS（App Transport Security）**: 読み込み先は https なので通常は問題ありません。http の独自サーバを読み込ませる場合のみ `Info.plist` に例外設定が必要です。

## 音（BGM / 効果音）が鳴らない

- `WebViewController` で `mediaTypesRequiringUserActionForPlayback = []`・`allowsInlineMediaPlayback = true` を設定済みです。それでも鳴らない場合は端末のサイレントスイッチ（消音）・音量を確認してください。
- iOS ではサイレントスイッチが ON だと Web の音声が鳴らないことがあります。STARTボタンなどの操作後に再生される想定です。

## スワイプで戻れない / 画面がリセットされる

- 戻る操作は `allowsBackForwardNavigationGestures = true` による画面端スワイプで WebView の履歴をたどります。
- ホーム画面に戻ってからの復帰でリロードされる場合は、メモリ不足でプロセスが破棄された可能性があります（ゲーム進行はサイト側の localStorage / IndexedDB に依存します）。

## 全画面にならない / ステータスバーが出る

- `prefersStatusBarHidden` / `prefersHomeIndicatorAutoHidden` と `Info.plist` の `UIStatusBarHidden` で全画面化しています。機種により挙動が異なる場合があります。

## ビルドが失敗する

- **XcodeGen 未生成**: `xcodegen generate` を実行して `FruitGame.xcodeproj` を生成したか確認します（`.xcodeproj` はコミットされていません）。
- **`future Xcode project file format (77)` エラー**: 最新の XcodeGen は Xcode 16 形式のプロジェクトを生成するため、Xcode 15 では開けません。**Xcode 16 以降**を使ってください（CI は `macos-15` ランナーを使用）。
- **Xcode / SDK**: iOS 14 以降の SDK を含む Xcode（16 以降）が必要です。
- **署名エラー**: シミュレータ向けは `CODE_SIGNING_ALLOWED=NO` で署名不要です。実機向けは Apple Developer の署名設定が必要です（[RELEASE.md](RELEASE.md) を参照）。
- 詳細なログは `xcodebuild ... -verbose` で確認できます。

## 古いゲーム画面が表示される

- ゲーム本体はサイト側のキャッシュ（Service Worker / IndexedDB）で管理されます。更新が反映されない場合は、サイト側の更新が GitHub Pages にデプロイ済みか、`public_html/version.json` が更新されているかを確認してください。
- 端末側で確実に再取得したい場合は、アプリを削除して再インストールし、オンラインで再起動します。

## 実機にインストールできない

- iOS は署名済みアプリしか実機にインストールできません。Apple Developer アカウントでの署名、または TestFlight 経由の配布が必要です（[RELEASE.md](RELEASE.md) を参照）。
- 無料の Apple ID でも Xcode から自分の実機へ 7 日間有効な署名でインストールできますが、配布はできません。
