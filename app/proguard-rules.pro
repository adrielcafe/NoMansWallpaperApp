# NMW
-keepclasseswithmembers class cafe.adriel.nomanswallpaper.model.** { *; }
-keepclasseswithmembers class cafe.adriel.nomanswallpaper.background.WallpaperWorker { *; }

# Billing
-keep class com.android.vending.billing.**

# Kotlin
-dontwarn kotlin.**
-dontwarn org.jetbrains.annotations.**
-keepattributes EnclosingMethod
-keepattributes Signature
-keepattributes Annotation
-keep public class kotlin.reflect.jvm.internal.impl.builtins.* { public *; }
-keep public class kotlin.reflect.jvm.internal.impl.serialization.deserialization.builtins.* { public *; }
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Firebase
-keep class io.grpc.** {*;}

# Crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

# OkHttp
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.*
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# EventBus
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# ArcAnimator
-keep class io.codetail.animation.arcanimator.** { *; }

# StickySwitch
-keep class io.ghyeok.stickyswitch.widget.StickySwitch { *; }
