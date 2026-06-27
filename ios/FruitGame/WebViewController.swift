import UIKit
import WebKit

/// Fruit Game をそのまま表示するガワアプリ（WKWebView ラッパー）。
///
/// GitHub Pages に公開されている既存サイトを読み込むため、
/// Service Worker / IndexedDB によるオフラインキャッシュなどの
/// 「ブラウザ動作の仕組み」はサイト側の実装をそのまま利用する。
///
/// Android 版 `MainActivity.kt` と対になる実装。
class WebViewController: UIViewController {

    /// 読み込み先 URL。別環境を読み込ませたい場合はこの値を変更して再ビルドする。
    private static let gameURL = URL(string: "https://tokibito.github.io/fruit-game/")!

    private var webView: WKWebView!

    // MARK: - 全画面表示

    /// ステータスバーを隠して全画面表示にする（Android の没入モード相当）。
    override var prefersStatusBarHidden: Bool { true }

    /// ホームインジケータを自動的に隠す（幼児の誤操作を減らす）。
    override var prefersHomeIndicatorAutoHidden: Bool { true }

    // MARK: - ライフサイクル

    override func loadView() {
        let configuration = WKWebViewConfiguration()

        // BGM / 効果音をユーザー操作なしで再生できるようにする
        // （Android の mediaPlaybackRequiresUserGesture = false 相当）。
        configuration.allowsInlineMediaPlayback = true
        configuration.mediaTypesRequiringUserActionForPlayback = []

        // WKWebView は https 経由のサイトであれば Service Worker / IndexedDB を
        // 標準で利用できるため、ここでの追加設定は不要（サイト側の実装をそのまま使う）。

        webView = WKWebView(frame: .zero, configuration: configuration)

        // 端末のスワイプ操作で WebView の履歴をたどる（Android の戻る操作相当）。
        webView.allowsBackForwardNavigationGestures = true

        // ピンチによる拡大縮小やスクロールバウンスを抑え、ゲームらしい固定画面にする。
        webView.scrollView.bounces = false
        webView.scrollView.contentInsetAdjustmentBehavior = .never

        view = webView
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        webView.load(URLRequest(url: Self.gameURL))
    }

    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        // 遊んでいる間は画面を消さない（幼児向けのため）。
        UIApplication.shared.isIdleTimerDisabled = true
    }

    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        UIApplication.shared.isIdleTimerDisabled = false
    }
}
