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

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep public class * extends android.app.Fragment
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep class android.support.v4.** {*;}
-keep interface android.support.v4.app.** {*;}
# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.**, android.support.v7.** {*;}
-keep interface android.support.v7.app.** {*;}
-keep class android.support.v13.** {*;}
-keep interface android.support.v13.app.** {*;}
-keep class android.support.v17.** {*;}
-keep interface android.support.v17.app.** {*;}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keep class com.google.android.gms.** {*;}
-dontwarn com.google.android.gms.**

-keep public class com.flyingwing.android.** {public protected *;}
-keep interface com.flyingwing.android.** {*;}
-keep public class com.flyingwing.android.**$* {public protected *;}
-keep public interface com.flyingwing.android.**$* {*;}