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

# Gson
#-keepattributes Signature-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# 使用Gson时需要配置Gson的解析对象及变量都不混淆。不然Gson会找不到变量。
-keep class com.acel.streamlivetool.bean.** { *; }
#-keep class com.acel.streamlivetool.platform.bilibili.bean.** { *; }
#-keep class com.acel.streamlivetool.platform.douyu.bean.** { *; }
#-keep class com.acel.streamlivetool.platform.huya.bean.** { *; }
##-keep class com.acel.streamlivetool.platform.douyu.** { *; }
#-keep class com.acel.streamlivetool.platform.huomao.bean.** { *; }
-keep class com.acel.streamlivetool.platform.**.bean.** { *; }


# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions


### greenDAO 3
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties

# If you do not use SQLCipher:
-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**
-keep class com.acel.streamlivetool.db.** { *; }

#ijkplayer  dkplayer
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.dueeeke.videoplayer.** { *; }

#rhino
-dontwarn org.mozilla.**
-keep class org.mozilla.** { *; }