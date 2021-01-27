
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;

}

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }