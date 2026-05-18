#!/usr/bin/env bash
# =========================================================
# validate-local-seed-required-columns.sh
# =========================================================
# Valida que cada INSERT de data-local.sql incluya columnas
# obligatorias efectivas (NOT NULL sin DEFAULT) del esquema H2.
#
# Uso:
#   ./scripts/validate-local-seed-required-columns.sh
#   ./scripts/validate-local-seed-required-columns.sh --db-url "jdbc:h2:file:./db/keygo-local;MODE=PostgreSQL;NON_KEYWORDS=VALUE;INIT=CREATE DOMAIN IF NOT EXISTS CITEXT AS VARCHAR"
# =========================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
SEED_FILE="$REPO_ROOT/keygo-run/src/main/resources/data-local.sql"

DB_URL='jdbc:h2:file:./db/keygo-local;MODE=PostgreSQL;NON_KEYWORDS=VALUE;INIT=CREATE DOMAIN IF NOT EXISTS CITEXT AS VARCHAR'
DB_USER='sa'
DB_PASSWORD=''

while [[ $# -gt 0 ]]; do
  case "$1" in
    --db-url)
      DB_URL="$2"
      shift 2
      ;;
    --help|-h)
      sed -n '2,12p' "$0" | sed 's/^# \{0,1\}//'
      exit 0
      ;;
    *)
      echo "Argumento desconocido: $1" >&2
      exit 2
      ;;
  esac
done

if [[ ! -f "$SEED_FILE" ]]; then
  echo "No se encontró el seed: $SEED_FILE" >&2
  exit 2
fi

H2_JAR="$(find "$HOME/.m2/repository" -name 'h2-*.jar' | sort | tail -1)"
if [[ -z "$H2_JAR" ]]; then
  echo "No se encontró JAR de H2 en ~/.m2/repository" >&2
  exit 2
fi

TMP_REQUIRED="$(mktemp)"
trap 'rm -f "$TMP_REQUIRED"' EXIT

SQL_QUERY="SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = 'PUBLIC' AND IS_NULLABLE = 'NO' AND COLUMN_DEFAULT IS NULL AND COALESCE(IS_IDENTITY, 'NO') <> 'YES' AND COALESCE(IS_GENERATED, 'NEVER') = 'NEVER' ORDER BY TABLE_NAME, ORDINAL_POSITION"
ESCAPED_SQL_QUERY="${SQL_QUERY//\'/\'\'}"

cd "$REPO_ROOT"

java -cp "$H2_JAR" org.h2.tools.Shell \
  -url "$DB_URL" \
  -user "$DB_USER" \
  -password "$DB_PASSWORD" \
  -sql "CALL CSVWRITE('$TMP_REQUIRED', '$ESCAPED_SQL_QUERY')" >/dev/null

python3 - "$TMP_REQUIRED" "$SEED_FILE" <<'PY'
import csv
import re
import sys
from pathlib import Path

required_csv = Path(sys.argv[1])
seed_file = Path(sys.argv[2])

required = {}
with required_csv.open(newline='') as f:
    for row in csv.DictReader(f):
        table = row['TABLE_NAME'].lower()
        col = row['COLUMN_NAME'].lower()
        required.setdefault(table, set()).add(col)

seed = seed_file.read_text()
insert_cols = {}
for m in re.finditer(r'INSERT\s+INTO\s+([a-zA-Z0-9_]+)\s*\((.*?)\)\s*(?:SELECT|VALUES)', seed, re.I | re.S):
    table = m.group(1).lower()
    cols = {
        c.strip().strip('"').lower()
        for c in m.group(2).replace('\n', ' ').split(',')
        if c.strip()
    }
    insert_cols.setdefault(table, []).append(cols)

issues = []
for table, req in sorted(required.items()):
    if table not in insert_cols:
        continue
    missing = set()
    for cols in insert_cols[table]:
        missing |= (req - cols)
    if missing:
        issues.append((table, sorted(missing), len(insert_cols[table]), len(req)))

print(f"required_tables_total={len(required)}")
print(f"required_tables_seeded={sum(1 for t in required if t in insert_cols)}")
print(f"issues={len(issues)}")

if issues:
    for table, missing, inserts, req_count in issues:
        print(f"ISSUE table={table} inserts={inserts} required={req_count} missing={missing}")
    sys.exit(1)
PY

echo "Validacion completada sin columnas obligatorias faltantes en INSERTs de data-local.sql"
