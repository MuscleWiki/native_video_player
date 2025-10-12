#!/bin/bash

echo "Cleaning Flutter project..."

# Navigate to project root
cd "$(dirname "$0")"

# Clean Flutter
flutter clean

# Remove build directories
echo "Removing build directories..."
rm -rf build/
rm -rf .dart_tool/
rm -rf android/build/
rm -rf android/.gradle/
rm -rf example/build/
rm -rf example/.dart_tool/
rm -rf example/android/build/
rm -rf example/android/.gradle/
rm -rf example/ios/Pods/
rm -rf example/ios/.symlinks/

# Clean Gradle cache (more thorough)
echo "Cleaning Gradle cache..."
rm -rf ~/.gradle/caches/

# Get dependencies
echo "Getting dependencies..."
flutter pub get

# Navigate to example and get dependencies
echo "Getting example dependencies..."
cd example
flutter pub get
cd ..

echo ""
echo "✅ Clean complete!"
echo ""
echo "Now you can rebuild your project:"
echo "  cd example"
echo "  flutter run"
