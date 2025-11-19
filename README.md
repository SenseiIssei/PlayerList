# PlayerList (Minecraft Fabric Mod)

Quick instructions to build locally and with GitHub Actions.

Build locally

- If you have the Gradle wrapper (`gradlew` / `gradlew.bat`):
  - Windows PowerShell: `./gradlew.bat build`
  - Unix / macOS: `./gradlew build`
- If you don't have the wrapper but have Gradle installed: `gradle build`

Gradle wrapper notes

- This repo now includes the wrapper scripts (`gradlew`, `gradlew.bat`) and `gradle/wrapper/gradle-wrapper.properties` pointing to Gradle 8.6.
- The wrapper JAR (`gradle/wrapper/gradle-wrapper.jar`) is not included. To generate it locally, run (if you have Gradle installed):

  ```powershell
  gradle wrapper
  ```

  After that `./gradlew.bat build` will work on Windows.

 - Alternatively, install Gradle on your machine and run `gradle build`.

Output

- The mod JAR will be under `build/libs/` after a successful build.

CI / Releases

- CI runs on pushes and PRs (workflow: `.github/workflows/ci.yml`).
- Creating a Git tag like `v1.2.3` and pushing it will trigger the release workflow and upload `build/libs/*.jar` as a release asset (workflow: `.github/workflows/release.yml`).

Notes

- The project uses Java 21; ensure your local JDK matches the toolchain or use the JDK setup in the CI workflows.
- If you'd like, I can also add the Gradle wrapper files to the repo so `./gradlew` works out of the box.

Auto-release and GitHub Actions permissions

- This repository now includes an `auto-release` workflow (`.github/workflows/auto-release.yml`) that will:
  - Run on every push to `main`.
  - Build the project using Gradle.
  - Create a timestamped tag (format: `vYYYYMMDDHHMM-<shortsha>`), push that tag, create a GitHub Release for the tag, and upload the produced JAR as a release asset.

- Required repository settings for full automation:
  - In the repository `Settings` → `Actions` → `General`, under `Workflow permissions`, set `Read and write permissions` so workflows can push tags and create releases. Also allow GitHub Actions to create and approve pull requests if your workflow needs that.

- Note: The workflows use the built-in `GITHUB_TOKEN` for authentication. You do not need to create a personal access token unless you want the workflow to push to protected branches or perform actions that require elevated permissions.
