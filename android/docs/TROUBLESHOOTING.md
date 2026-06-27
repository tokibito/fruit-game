# トラブルシューティング

WebView ガワアプリ特有の問題と対処をまとめます。

## ゲームが表示されない / 白い画面のまま

- **初回はネットワークが必要**: 初回起動時はサイト（GitHub Pages）の読み込みとキャッシュ作成のため通信が必要です。オフラインのまま初回起動するとキャッシュが無く表示できません。一度オンラインで起動すれば、以降は Service Worker / IndexedDB のキャッシュで動作します。
- **URL を確認**: `MainActivity.kt` の `GAME_URL` が正しいか確認します。
- **権限**: `AndroidManifest.xml` に `INTERNET` 権限があるか確認します。

## 音（BGM / 効果音）が鳴らない

- `MainActivity` で `mediaPlaybackRequiresUserGesture = false` を設定済みです。それでも鳴らない場合は端末のマナーモード／メディア音量を確認してください。
- 一部端末では最初のユーザー操作（タップ）まで音声が再生されないことがあります（STARTボタン操作後に再生される想定）。

## 戻るボタンでゲームがリセットされる / アプリが即終了する

- 戻る操作は `OnBackPressedCallback` で処理し、WebView に履歴があれば `goBack()`、なければアプリ終了とする実装です。
- 画面回転などで状態が失われる場合は、`AndroidManifest.xml` の `configChanges` と `onSaveInstanceState`/`restoreState` が効いているか確認します。

## 全画面（没入モード）にならない

- `hideSystemBars()` は `onResume` で呼ばれます。標準フラグ（API 30 で deprecated だが互換目的）を使用しているため、機種によりバーの挙動が異なる場合があります。

## ビルドが失敗する

- **JDK バージョン**: JDK 17 を使用しているか確認します（`java -version`）。
- **Android SDK**: API 34 のプラットフォームがインストールされているか、`ANDROID_HOME` / `local.properties` の `sdk.dir` が正しいか確認します。
- **Gradle 同期エラー**: ネットワークを確認のうえ `./gradlew --refresh-dependencies clean assembleDebug` を試します。
- 詳細なエラー出力は `./gradlew assembleDebug --stacktrace --info` で確認できます。

## 古いゲーム画面が表示される

- ゲーム本体はサイト側のキャッシュ（Service Worker / IndexedDB）で管理されます。更新が反映されない場合は、サイト側の更新が GitHub Pages にデプロイ済みか、`public_html/version.json` が更新されているかを確認してください。
- 端末側で確実に再取得したい場合は、アプリのストレージ／キャッシュをクリアしてからオンラインで再起動します。

## デバッグ APK がインストールできない

- 端末で「提供元不明のアプリ（不明なアプリのインストール）」を許可します。
- 署名違いで上書きできない場合は、既存アプリをアンインストールしてから `adb install` し直します。
