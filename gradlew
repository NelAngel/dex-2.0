#!/usr/bin/env sh
# Lightweight Gradle bootstrapper for environments where gradle-wrapper.jar is unavailable.
set -eu
VERSION="8.11.1"
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/sd-lygane-gradle-${VERSION}"
BIN="$BASE/gradle-${VERSION}/bin/gradle"
if [ ! -x "$BIN" ]; then
  mkdir -p "$BASE"
  ZIP="$BASE/gradle-${VERSION}-bin.zip"
  URL="https://services.gradle.org/distributions/gradle-${VERSION}-bin.zip"
  if command -v curl >/dev/null 2>&1; then curl -fL "$URL" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then wget -O "$ZIP" "$URL"
  else echo "Se necesita curl o wget para descargar Gradle ${VERSION}." >&2; exit 1
  fi
  if command -v unzip >/dev/null 2>&1; then unzip -q -o "$ZIP" -d "$BASE"
  else echo "Se necesita unzip para instalar Gradle." >&2; exit 1
  fi
fi
exec "$BIN" "$@"
