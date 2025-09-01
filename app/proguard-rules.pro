# -------------------------
# General attributes
# -------------------------
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod,SourceFile,LineNumberTable

# Keep classes annotated with @Keep
-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# -------------------------
# Retrofit & OkHttp & Gson
# -------------------------
# Keep Retrofit interfaces and annotations
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# Gson model classes — IMPORTANT: change package below to your model package
# keep all model classes used by Gson (fields & classes)
-keep class com.adjaba.models.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.JsonDeserializer
-keep class * implements com.google.gson.JsonSerializer

# Keep retrofit converter factories if used via reflection
-keep class retrofit2.converter.gson.** { *; }

# -------------------------
# ExoPlayer
# -------------------------
-dontwarn com.google.android.exoplayer2.**
-keep class com.google.android.exoplayer2.** { *; }
-keep interface com.google.android.exoplayer2.** { *; }

# -------------------------
# Glide (or other image libs)
# -------------------------
# Keep Glide modules and generated API
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**

# If you use generated Glide API (annotation processor)
-keep class com.bumptech.glide.load.resource.bitmap.TransformationUtils { *; }

# -------------------------
# Room (entities, DAOs, DB)
# -------------------------
# Keep Room entities and DAOs — adjust package to yours
-keep class com.adjaba.room.** { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Keep Room's schema classes and annotations
-keepattributes *Annotation*
-dontwarn androidx.room.**

# -------------------------
# Hilt / Dagger / javax.inject
# -------------------------
-dontwarn dagger.**
-dontwarn javax.inject.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }

# Keep Hilt generated components and entry points
-keep class dagger.hilt.android.** { *; }
-keep class * extends android.app.Application { *; }

# Keep all classes annotated with @Module, @InstallIn, @EntryPoint, @AndroidEntryPoint

# -------------------------
# Firebase & Google Play services
# -------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep Firebase auth / model classes that may be referenced by reflection
-keepclassmembers class * {
    @com.google.firebase.* *;
}

# -------------------------
# Android components (Activities, Services, Receivers, Providers)
# -------------------------
# Keep public components referenced in manifest (safe default)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep classes referenced by name in XML (e.g., Custom Views)
-keepclassmembers class * {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# -------------------------
# Reflection / Serialization / Callbacks
# -------------------------
# Keep classes with methods referenced by name (e.g., via reflection)
-keepclassmembers class * {
    public void *(...);
}

# Keep lambdas serialization compatibility (if used)
-keepclassmembers class * {
    public <init>(...);
}

# -------------------------
# Keep application entry points / utility classes
# -------------------------
# Replace with your Application class fully qualified name if you have one

# Keep any BroadcastReceiver/Service/etc. that are started by name
-keepclassmembers class * {
    public void onReceive(android.content.Context, android.content.Intent);
}

# -------------------------
# Logging / Exceptions - keep for readability if you want stack traces
# -------------------------
# Uncomment to keep line numbers for better stack traces (makes mapping required)
#-keepattributes SourceFile,LineNumberTable

# -------------------------
# Fallbacks and safety nets
# -------------------------
# Keep any classes annotated with Json, Serializable, Parcelable, etc.
-keep @interface com.google.gson.annotations.SerializedName

# Keep Parcelable creators
-keepclassmembers class * implements android.os.Parcelable {
  public static final ** CREATOR;
}

# -------------------------
# End of file
# -------------------------
