# AndroidX AppCompat
-keep class androidx.appcompat.** { *; }
-dontwarn androidx.appcompat.**

# Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Espresso (only if required in production, generally not needed)
-keep class androidx.test.espresso.** { *; }
-dontwarn androidx.test.espresso.**

# JUnit (only if required in production, generally not needed)
-keep class org.junit.** { *; }
-dontwarn org.junit.**
