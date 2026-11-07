# Не трогать Tink (библиотека безопасности)
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Не трогать аннотации ErrorProne
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }

# Не трогать WebView и JS-мосты
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class androidx.security.crypto.** { *; }

# Не трогать BuildConfig (там наш GROQ_API_KEY)
-keep class com.nikeboss.browser.BuildConfig { *; }
