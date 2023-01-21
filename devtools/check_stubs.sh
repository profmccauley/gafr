set -e
echo "Checking for native differences..."
grep _CHEERPJ_COMPRESS native_stubs/GaFr/GFN_native.js | sort > .nat_stub
grep _CHEERPJ_COMPRESS native/GaFr/GFN_native.js | sort | grep -v -e '^//' > .nat_real
if ! cmp --silent .nat_real .nat_stub; then
  echo "Identifying differences in natives..."
  colordiff .nat_real .nat_stub
  echo "** Fix natives and recompile! **"
  rm .nat_real .nat_stub
  exit 1
fi
rm .nat_real .nat_stub
