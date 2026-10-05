# Bixby Wake Fix (LSPosed module)

Build: open in Android Studio (or `gradle assembleRelease` with the Android SDK), sign the APK, install.
Enable in LSPosed -> Modules, scope = `com.samsung.android.bixby.wakeup`, then force-stop that app (or reboot).
Check LSPosed logs for `[BixbyWakeFix]` lines.

Before building, confirm the class/method names against your firmware by decompiling the
Bixby wakeup APK (jadx). Samsung renames these between One UI versions. If `isRecognitionAllowed`
takes arguments or is obfuscated, hookAllMethods still matches by name, but a renamed method needs editing in MainHook.java.
