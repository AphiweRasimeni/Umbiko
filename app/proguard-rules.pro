# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.umbiko.app.**$$serializer { *; }
-keepclassmembers class com.umbiko.app.** { *** Companion; }
-keepclasseswithmembers class com.umbiko.app.** { kotlinx.serialization.KSerializer serializer(...); }

# Retrofit / OkHttp
-keep class com.umbiko.app.data.remote.** { *; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-dontwarn okio.**
