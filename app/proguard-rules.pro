# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
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


-libraryjars libs/twitter4j-core-4.0.2.jar
-libraryjars libs/twitter4j-stream-4.0.2.jar

-dontwarn twitter4j.**

#-dontwarn twitter4j.management.**

#-dontwarn twitter4j.TwitterAPIMonitor

#-dontwarn twitter4j.internal.**

#-dontwarn twitter4j.Annotation

-keep class twitter4j.** { *; }


-keepattributes SourceFile,LineNumberTable

-keep class android.support.v7.widget.SearchView { *; }