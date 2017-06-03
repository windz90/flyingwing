# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android-sdk-windows/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# If enable optimize, and need remove Log method in code
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** i(...);
    public static *** d(...);
    public static *** w(...);
    public static *** e(...);
}

# If enable optimize, and need remove Print.println() method in code
-assumenosideeffects class java.io.PrintStream {
    public void println(%);
    public void println(**);
}

# If enable optimize, and need remove Exception.printStackTrace() method in code
-assumenosideeffects class java.lang.Exception {
    public void printStackTrace();
}

# If use reflection
-keepattributes Signature # Keep Generics
-keepattributes EnclosingMethod

# Keep source code line number(Can display throw exception line number)
-keepattributes SourceFile, LineNumberTable

-keep class com.google.android.gms.** {*;}
-dontwarn com.google.android.gms.**

-keep public class * {public protected *;}
-keep public interface * {*;}
-keep public class *$* {public protected *;}
-keep public interface *$* {*;}