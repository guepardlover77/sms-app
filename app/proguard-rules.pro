# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Compose
-keep class androidx.compose.** { *; }

# Keep Coil
-keep class coil.** { *; }

# Keep Google Fonts
-keep class androidx.compose.ui.text.googlefonts.** { *; }
