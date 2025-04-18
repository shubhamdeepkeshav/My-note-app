plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // 🔥 Removed Firebase plugin
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
