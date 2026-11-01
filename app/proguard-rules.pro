# Aroy Player - Hardened ProGuard / R8 Obfuscation & Security Rules

# Strip sensitive debug and diagnostic logs from release builds to prevent data leakage in logcat
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Obfuscate source file names and line tables to hinder reverse engineering
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod

# Protect Room database entities & DAO generated classes
-keep class androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Protect Media3 and ExoPlayer core codecs and dynamic reflection loaders
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class androidx.media.** { *; }
-dontwarn androidx.media.**

# Protect Kotlin Coroutines & Flow reflection mechanics
-keepclassmembers class kotlinx.coroutines.** { *; }

# Prevent tampering with models
-keepclassmembers class com.example.data.model.** {
    <fields>;
    <init>(...);
}
