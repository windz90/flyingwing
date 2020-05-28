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

# Keep source code line number(Can display throw exception line number)
-keepattributes SourceFile, LineNumberTable
# If you keep the line number information, this can hide the original source file name.
-renamesourcefileattribute SourceFile