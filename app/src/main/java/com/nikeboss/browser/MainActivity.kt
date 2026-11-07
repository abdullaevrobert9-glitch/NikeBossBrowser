package com.nikeboss.browser

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var etUrl: EditText
    private lateinit var btnBack: Button
    private lateinit var btnForward: Button
    private lateinit var btnReload: Button
    private lateinit var btnSaveLogin: Button

    private lateinit var store: CredentialsStore

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        store = CredentialsStore(this)

        webView = findViewById(R.id.webView)
        etUrl = findViewById(R.id.etUrl)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnReload = findViewById(R.id.btnReload)
        btnSaveLogin = findViewById(R.id.btnSaveLogin)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                etUrl.setText(url)
                updateNavButtons()
                url?.let { tryAutofillLogin(it) }
            }
        }

        webView.loadUrl("https://www.google.com")

        btnBack.setOnClickListener { if (webView.canGoBack()) webView.goBack() }
        btnForward.setOnClickListener { if (webView.canGoForward()) webView.goForward() }
        btnReload.setOnClickListener { webView.reload() }

        btnSaveLogin.setOnClickListener { promptSaveLogin() }

        etUrl.setOnEditorActionListener { _, _, _ ->
            val input = etUrl.text.toString().trim()
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

    private fun currentDomain(): String? {
        val host = webView.url?.let { android.net.Uri.parse(it).host } ?: return null
        return host.removePrefix("www.")
    }

    private fun promptSaveLogin() {
        val domain = currentDomain() ?: run {
            Toast.makeText(this, "Сначала открой сайт", Toast.LENGTH_SHORT).show()
            return
        }
        val input = EditText(this).apply {
            hint = "Email или логин для $domain"
            setText(store.getLogin(domain) ?: "")
        }

        AlertDialog.Builder(this)
            .setTitle("Сохранить логин?")
            .setMessage("Будет сохранён ТОЛЬКО email/логин. Пароль не сохраняется — его подставит Android.")
            .setView(input)
            .setPositiveButton("Сохранить") { _, _ ->
                val login = input.text.toString().trim()
                if (login.isNotEmpty()) {
                    store.saveLogin(domain, login)
                    Toast.makeText(this, "Логин сохранён для $domain", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Удалить") { _, _ ->
                store.clearLogin(domain)
                Toast.makeText(this, "Логин удалён для $domain", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun tryAutofillLogin(url: String) {
        val host = android.net.Uri.parse(url).host?.removePrefix("www.") ?: return
        val saved = store.getLogin(host) ?: return

        // Ищем первое поле email/логина и подставляем сохранённое значение
        val js = """
            (function() {
                var v = ${escapeJs(saved)};
                var inputs = document.querySelectorAll('input[type="email"], input[type="text"], input[name*="login"], input[name*="email"], input[name*="user"]');
                for (var i = 0; i < inputs.length; i++) {
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

    private fun updateNavButtons() {
        btnBack.isEnabled = webView.canGoBack()
        btnForward.isEnabled = webView.canGoForward()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
