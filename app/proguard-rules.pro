-dontwarn
-ignorewarnings

# Gson

-keep class com.livae.ff.common.* { *; }
-keep class com.livae.ff.common.model.* { *; }

# Google play services

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}
-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# Okhttp integration with glide

-keep class com.bumptech.glide.integration.okhttp.OkHttpGlideModule {
	*;
}

# Cloud Endpoints generated classes

-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keep class * extends com.google.api.client.json.GenericJson{
	*;
}

# for animations in views

-keepclassmembers public class * extends android.view.View {
   void set*(***);
   *** get*();
}

# For design library

-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }
-dontwarn android.support.design.**

# support-v4

-dontwarn android.support.v4.**
-keep class android.support.v4.app.** { *; }
-keep interface android.support.v4.app.** { *; }

# support-v7

-dontwarn android.support.v7.**
-keep class android.support.v7.widget.** { *; }
-keep interface android.support.v7.widget.** { *; }
-keep class android.support.v7.internal.** { *; }
-keep interface android.support.v7.internal.** { *; }
