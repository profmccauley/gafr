#!/bin/bash

set -e

# dir name from stackoverflow 59895
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd "$DIR/.."

TVW=$(grep ^TREEVIEW_WIDTH Doxyfile | tail -1 | cut -d= -f2)
TVW=$(echo $TVW)
if [[ "$TVW" == "" ]]; then
  echo "TREEVIEW_WIDTH not set?"
  exit 1
fi

sed -i "s/^\([ \t]*\)--side-nav-fixed-width:.*/\1--side-nav-fixed-width: ${TVW}px;/" doc/html/doxygen-awesome-sidebar-only.css
