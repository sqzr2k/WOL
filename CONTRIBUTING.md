# Contributing

Issues and focused pull requests are welcome.

- Keep changes small, clear, and maintainable.
- Avoid proprietary dependencies unless there is a compelling and documented reason.
- Do not add telemetry, tracking, advertising, secrets, private data, or build artifacts.
- Preserve local-only storage and the absence of mandatory accounts or cloud services.
- Update tests and documentation when behavior changes.

Before submitting a pull request, run:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
```

## Versioning

`versionCode` and `versionName` in `app/build.gradle.kts` are the authoritative version source. The About WOL screen reads the version automatically from the build. Before publishing an Android version, increase `versionCode` monotonically and choose `versionName` deliberately for that release. Release versions are prepared only after explicit approval.
