#!/bin/bash

set -e

ALLGOOD=1

check_file () {
  local O=$(git whatchanged --all --find-object=$1 2> /dev/null | wc -l)

  if [[ "$O" > 0 ]]; then
    return 0
  fi
  return 1
}

do_copy () {
  local SRC="$1"
  local FN="$2"
  local DN=$(dirname "$FN")
  if [[ ! -d "$DN" ]]; then
    if [[ -e "$DN" ]]; then
      echo "** Error!  $DN exists but isn't a directory!"
      exit 1
    fi
    mkdir -p "$DN"
    echo "  (Created directory $DN)"
  fi
  if [[ -e "$FN" ]]; then
    echo "** Error!  Wasn't expecting $TARGET/$F to exist!"
    exit 2
  fi
  cp -i "$SRC" "$FN"
}

TARGET=$(pwd)

# dir name from stackoverflow 59895
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

GAFR="$DIR/.."

pushd "$GAFR/skeleton" > /dev/null
COPY=()
UPDATE=()
NO_UPDATE=()
for F in $(find "." -type f); do
  if [[ -e "$TARGET/$F" ]]; then
    if cmp --quiet "$TARGET/$F" "$F" ; then
      :
    else
      if check_file $(git hash-object "$TARGET/$F") ; then
        UPDATE+=($F)
      else
        #echo "** File $F appears to be modified and will not be updated."
        NO_UPDATE+=($F)
        ALLGOOD=0
      fi
    fi
  else
    COPY+=($F)
  fi
done

if [ ${#UPDATE[@]} -ne 0 ]; then
  echo "The following files appear to be old versions:"

  for F in "${UPDATE[@]}"; do
    echo "  $F"
  done

  echo "If you think you have modified these files, you should make sure you"
  echo "have backed them up, or should say no to updating them."
  echo -n "Do you want to update them (y/N)? "
  read CONFIRM
  if [[ "$CONFIRM" != "y" ]]; then
    ALLGOOD=0
  else
    for F in "${UPDATE[@]}"; do
      #mv "$TARGET/$F" "$TARGET/$F.bak"
      rm "$TARGET/$F"
      do_copy "$F" "$TARGET/$F"
    done
  fi
fi

if [ ${#COPY[@]} -ne 0 ]; then
  echo "Copying missing files..."

  for F in "${COPY[@]}"; do
    #cp -i "$F" "$TARGET/$F"
    do_copy "$F" "$TARGET/$F"
    echo "  $F"
  done
fi

popd > /dev/null

if [[ ! -e gafr ]]; then
  echo "It doesn't look like the project directory contains a copy of GaFr."
  echo "This may cause your project to not work."
  #echo "Do you want to try to git clone it into the project directory (y/N)?"
  echo -n "Do you want to copy it in (y/N)? "
  read CONFIRM
  if [[ "$CONFIRM" == "y" ]]; then
    cp -i -a "$GAFR" gafr
    #git clone "$GAFR" gafr
  fi
fi

if [[ -e gafr ]]; then
  :
#  pushd gafr
#  if [[ ! -e GaFr.jar.js ]]; then
#    echo "It looks like GaFr isn't built yet."
#    echo -n "Try building (y/N)? "
#    read CONFIRM
#    if [[ "$CONFIRM" == "y" ]]; then
#      make
#      make fonts
#    fi
#  fi
#  popd
else
  ALLGOOD=0
fi

if [ ${#NO_UPDATE[@]} -ne 0 ]; then
  echo "The following files appear to have been modified:"

  for F in "${NO_UPDATE[@]}"; do
    echo "  $F"
  done

  echo "You may have to update these manually."
fi


if [[ "$ALLGOOD" == 1 ]]; then
  echo "Success!  You can build your project with 'make'."
else
  echo "Completed, but something may be odd."
  echo "You can try building your project with 'make'."
fi
