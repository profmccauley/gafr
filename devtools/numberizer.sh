#!/bin/bash

set -e

# dir name from stackoverflow 59895
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

(
  cd "$DIR/.."
  make --silent numberizer/target/numberizer-1.0-SNAPSHOT-shaded.jar
)

exec java -jar "$DIR/../numberizer/target/numberizer-1.0-SNAPSHOT-shaded.jar" "$@"
