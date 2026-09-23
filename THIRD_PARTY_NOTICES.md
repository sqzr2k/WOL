# Third-Party Software

WOL uses open-source build tools and libraries resolved from Google's Maven repository, Maven Central, and the Gradle Plugin Portal. Exact versions are declared in `gradle/libs.versions.toml` and resolved by Gradle.

| Component family | Use | License | Upstream |
| --- | --- | --- | --- |
| Android Gradle Plugin | Build tooling | Apache-2.0 | https://android.googlesource.com/platform/tools/base/ |
| AndroidX, Jetpack Compose, Material 3, Room, DataStore, Lifecycle, and Navigation | App runtime and UI | Apache-2.0 | https://android.googlesource.com/platform/frameworks/support/ |
| Kotlin and Kotlin Compose plugin | Language and build tooling | Apache-2.0 | https://github.com/JetBrains/kotlin |
| Kotlin Symbol Processing (KSP) | Build-time code generation | Apache-2.0 | https://github.com/google/ksp |
| Kotlin Coroutines | App runtime | Apache-2.0 | https://github.com/Kotlin/kotlinx.coroutines |
| Gradle Wrapper and Gradle | Build tooling | Apache-2.0 | https://github.com/gradle/gradle |
| JUnit 4 | Unit tests only | EPL-1.0 | https://github.com/junit-team/junit4 |

These components remain subject to their respective upstream licenses and notices. No third-party binary artwork, fonts, screenshots, or proprietary assets are included in this repository.
