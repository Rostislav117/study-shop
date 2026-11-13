#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."
archive="$PWD/target/shop-java-study-1.0.0.jar"
if [[ ! -f "$archive" ]]; then
  printf '%s\n' 'Сначала соберите приложение командой mvn clean package.' >&2
  exit 1
fi

jdk_bin=""
if [[ -n "${JAVA_HOME:-}" ]]; then
  jdk_bin="$JAVA_HOME/bin/"
fi

libraries="$(mktemp -d)"
trap 'rm -rf "$libraries"' EXIT
(cd "$libraries" && "${jdk_bin}jar" xf "$archive" BOOT-INF/lib)
jars=("$libraries"/BOOT-INF/lib/*.jar)
classpath="$(IFS=:; printf '%s' "${jars[*]}")"
"${jdk_bin}javadoc" -quiet -Werror -encoding UTF-8 -docencoding UTF-8 -charset UTF-8 \
  -Xdoclint:all,-missing -classpath "$classpath" -d target/javadoc \
  -sourcepath src/main/java -subpackages ru.rostislav.shop
