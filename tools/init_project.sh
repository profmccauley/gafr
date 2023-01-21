#!/bin/bash

set -e

TARGET=$(pwd)

# dir name from stackoverflow 59895
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

GAFR="$DIR/.."

cp -r -i "$GAFR/skeleton/." "$TARGET"

if [[ ! -e "$TARGET/gafr" ]]; then
  echo "It doesn't look like the project directory contains a copy of GaFr."
  echo "This may cause your project to not work."
  #echo "Do you want to try to git clone it into the project directory (y/N)?"
  echo -n "Do you want to copy it in (y/N)? "
  read CONFIRM
  if [[ "$CONFIRM" != "y" ]]; then
    exit 0
  fi
  #git clone "$GAFR" gafr
  cp -i -a "$GAFR" gafr
fi

pushd gafr
if [[ ! -e GaFr.jar.js ]]; then
  echo "It looks like GaFr isn't built yet."
  echo -n "Try building (y/N)? "
  read CONFIRM
  if [[ "$CONFIRM" == "y" ]]; then
    make
    make fonts
  fi
fi
popd

echo "Success!  You can build your project with 'make'."
