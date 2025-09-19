jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      # Checkout code
      - uses: actions/checkout@v4

      # Set up JDK
      - uses: actions/setup-java@v3
        with:
          distribution: temurin
          java-version: 17

      # Make gradlew executable
      - name: Make gradlew executable
        run: chmod +x ./gradlew

      # Clean project
      - name: Clean Gradle project
        run: ./gradlew clean

      # Build debug APK
      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace --info --debug --no-daemon

      # Upload APKs
      - uses: actions/upload-artifact@v4
        with:
          name: debug-apks
          path: '**/build/outputs/apk/debug/*.apk'
