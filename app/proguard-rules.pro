# Add project specific ProGuard rules here.

# Keep Compose runtime
-dontwarn androidx.compose.**

# Keep WorkManager worker classes
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
