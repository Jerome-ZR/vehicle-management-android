# Add project specific ProGuard rules here.

# Ignore warnings for optional dependencies not available on Android
-ignorewarnings

# Keep entity classes for Room database
-keep class com.vehiclemanager.data.entity.** { *; }
-keep class com.vehiclemanager.util.BackupData { *; }

# Apache POI - keep all
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }
-keep class org.openxmlformats.** { *; }
-keep class org.apache.commons.** { *; }
-keep class org.apache.logging.** { *; }

# Don't warn about missing optional dependencies
-dontwarn aQute.bnd.**
-dontwarn javax.xml.stream.**
-dontwarn javax.xml.**
-dontwarn org.apache.xmlbeans.**
-dontwarn org.apache.poi.**
-dontwarn org.openxmlformats.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.logging.**
-dontwarn org.osgi.**
-dontwarn net.sf.saxon.**
-dontwarn com.google.gson.**
-dontwarn org.w3c.**
-dontwarn javax.**
-dontwarn java.xml.**
-dontwarn java.awt.**
-dontwarn com.graphbuilder.**
-dontwarn schemasMicrosoftCom.**
-dontwarn org.jcp.**
-dontwarn com.sun.**

# Keep Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Don't obfuscate Room entities
-keepclassmembers class com.vehiclemanager.data.entity.** {
    <fields>;
    <init>(...);
}
