-dontwarn
-ignorewarnings

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

-keepnames class com.bumptech.glide.integration.okhttp.OkHttpGlideModule

# Cloud Endpoints generated classes

-keepclassmembers class * {
  @com.google.api.client.util.Key <fields>;
}
-keepattributes Signature,RuntimeVisibleAnnotations,AnnotationDefault
-keep class * extends com.google.api.client.json.GenericJson{
	*;
}
