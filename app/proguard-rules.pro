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
-libraryjars 'C:\Program Files\Android\Android Studio\jre\jre\lib\rt.jar'
-libraryjars 'C:\Users\acel\AppData\Local\Android\Sdk\platforms\android-29\android.jar'
# Gson
#-keepattributes Signature-keepattributes *Annotation*
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# 使用Gson时需要配置Gson的解析对象及变量都不混淆。不然Gson会找不到变量。
-keep class com.acel.streamlivetool.bean.** { *; }
-keep class com.acel.streamlivetool.platform.**.bean.** { *; }
-keep class com.acel.streamlivetool.platform.**.impl.**.bean { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#rhino
-dontwarn org.mozilla.**
-keep class org.mozilla.** { *; }

#glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

-keep class com.google.android.material.snackbar.SnackbarContentLayout { *; }