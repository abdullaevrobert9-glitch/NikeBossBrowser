package com.nikeboss.browser

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var store: CredentialsStore

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Делаем на всю конечную строку
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_main)
        store = CredentialsStore(this)

        webView = findViewById(R.id.webView)
        swipeRefresh = findViewById(R.id.swipeRefresh)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
        }

        swipeRefresh.setOnRefreshListener { webView.reload() }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipeRefresh.isRefreshing = false
                
                if (url?.contains("file:///android_asset/home.html") == true) {
                } else {
                }
                
                // Автозаполнение логина
                if (url != null) {
                    tryAutofillLogin(url)
                }
            }
        }

        webView.loadUrl("file:///android_asset/home.html")

            if (input.isNotEmpty()) {
                val url = when {
                    input.startsWith("http") -> input
                    input.contains(".") && !input.contains(" ") -> "https://$input"
                    else -> "https://www.google.com/search?q=${java.net.URLEncoder.encode(input, "UTF-8")}"
                }
                webView.loadUrl(url)
            }
            true
        }
    }

    private fun tryAutofillLogin(url: String) {
        val host = android.net.Uri.parse(url).host?.removePrefix("www.") ?: return
        val saved = store.getLogin(host) ?: return

        val js = """
            (function() {
                var v = ${escapeJs(saved)};
                var inputs = document.querySelectorAll('input[type="email"], input[type="text"], input[name*="login"], input[name*="email"], input[name*="user"]');
                for (var i = 0; i <inputs.length; i++) {
                    var el = inputs[i];
                    if (el.offsetParent === null) continue;
                    if (el.value && el.value.length > 0) continue;
                    el.focus();
                    el.value = v;
                    el.dispatchEvent(new Event('input', { bubbles: true }));
                    el.dispatchEvent(new Event('change', { bubbles: true }));
                    return true;
                }
                return false;
            })();
        """.trimIndent()
        webView.evaluateJavascript(js, null)
    }

    private fun escapeJs(s: String): String =
        "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\""

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
