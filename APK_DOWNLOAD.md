# Calculator Android APK

This repository includes a GitHub Actions workflow that builds a downloadable Android APK.

## Download the APK

1. Open the repository's **Actions** tab.
2. Select **Build Android APK**.
3. Open a successful workflow run.
4. Download the **Calculator-debug-apk** artifact.
5. Extract the downloaded ZIP and install `Calculator-debug.apk` on an Android device.

For a permanent download under **Releases**, create and push a version tag:

```bash
git tag v1.0.0
git push origin v1.0.0
