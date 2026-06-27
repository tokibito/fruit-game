package io.github.tokibito.fruitgame

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.ServiceWorkerControllerCompat
import androidx.webkit.WebViewFeature

/**
 * Fruit Game をそのまま表示するガワアプリ（WebView ラッパー）。
 *
 * GitHub Pages に公開されている既存サイトを読み込むため、
 * Service Worker / IndexedDB によるオフラインキャッシュなどの
 * 「ブラウザ動作の仕組み」はサイト側の実装をそのまま利用する。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true          // ゲーム本体は JavaScript
            domStorageEnabled = true          // localStorage / sessionStorage
            databaseEnabled = true            // IndexedDB のバッキングを許可
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false // BGM / 効果音の再生
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        // Service Worker を有効化（オフライン対応の仕組みを維持）
        if (WebViewFeature.isFeatureSupported(WebViewFeature.SERVICE_WORKER_BASIC_USAGE)) {
            ServiceWorkerControllerCompat.getInstance().serviceWorkerWebSettings.apply {
                cacheMode = WebSettings.LOAD_DEFAULT
                allowContentAccess = true
                allowFileAccess = true
            }
        }

        // ナビゲーションは WebView 内に閉じ込め、ブラウザは起動しない。
        // 既定のサイト（GitHub Pages）以外への遷移はブロックする。
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                // 許可された URL なら WebView 内でそのまま読み込ませる（false = ブロックしない）。
                // それ以外（外部リンクなど）は遷移をキャンセルする（true = 上書きして何もしない）。
                return !isAllowedUrl(request.url)
            }
        }
        webView.webChromeClient = WebChromeClient()

        // 遊んでいる間は画面を消さない（幼児向けのため）
        webView.keepScreenOn = true

        if (savedInstanceState == null) {
            webView.loadUrl(GAME_URL)
        } else {
            webView.restoreState(savedInstanceState)
        }

        // 端末の戻る操作で WebView の履歴をたどる
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        webView.onResume()
    }

    override fun onPause() {
        webView.onPause()
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    /**
     * ステータスバー / ナビゲーションバーを隠してスティッキー没入モードにする。
     * minSdk 24 から動作する標準フラグを使用（API 30 で deprecated だが互換目的で利用）。
     */
    @Suppress("DEPRECATION")
    private fun hideSystemBars() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
    }

    /**
     * 既定のサイト内への遷移かどうかを判定する。
     *
     * スキーム（https）・ホスト（tokibito.github.io）・パスの接頭辞（/fruit-game/）が
     * すべて一致する場合のみ許可し、それ以外への遷移はブロックする。
     */
    private fun isAllowedUrl(uri: Uri): Boolean {
        val base = Uri.parse(GAME_URL)
        return uri.scheme.equals(base.scheme, ignoreCase = true) &&
            uri.host.equals(base.host, ignoreCase = true) &&
            (uri.path ?: "").startsWith(base.path ?: "")
    }

    companion object {
        private const val GAME_URL = "https://tokibito.github.io/fruit-game/"
    }
}
