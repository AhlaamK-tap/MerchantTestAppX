# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
 -keepattributes Signature
    -keepclassmembernames,allowobfuscation interface * {
        @retrofit2.http.* <methods>;
    }
    -dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

    #########################################################################
    # OkHttp
    #########################################################################
    -dontwarn okhttp3.**
    -dontwarn okhttp2.**
    -dontwarn okio.**
    -dontwarn javax.annotation.**
    -dontwarn org.conscrypt.**
    -keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class company.tap.gosellapi.** { *; }
-keep class gotap.com.tapglkitandroid.** { *; }


-dontwarn okhttp2.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
#-dontobfuscate
-optimizations !code/allocation/variable
-keep class company.tap.gosellapi.** { *; }
# KEEP TapGLKit classes
-keep class gotap.com.tapglkitandroid.** { *; }
-keep class gotap.com.tapglkitandroid.gl.Views.TapLoadingView { *; }

-dontwarn gotap.com.tapglkitandroid.**
-keepclassmembers class gotap.com.tapglkitandroid.gl.Views.TapLoadingView {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keep class company.tap.tapcardvalidator_android.** { *; }
-dontwarn company.tap.tapcardvalidator_android.**


# GSON.
-keepnames class com.google.gson.** {*;}
-keepnames enum com.google.gson.** {*;}
-keepnames interface com.google.gson.** {*;}
-keep class com.google.gson.** { *; }
-keepnames class org.** {*;}
-keepnames enum org.** {*;}
-keepnames interface org.** {*;}
-keep class org.** { *; }
-keepclassmembers enum * { *; }

# Retrofit
-keepattributes Signature
-keepattributes RuntimeVisibleAnnotations
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes Signature

# Gson TypeToken
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken