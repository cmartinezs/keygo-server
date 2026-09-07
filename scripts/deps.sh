#!/usr/bin/env bash
set -euo pipefail

fail=0

check_cmd() {
  local cmd="$1"
  local label="$2"
  if command -v "${cmd}" >/dev/null 2>&1; then
    printf '[deps] OK   %-14s %s\n' "${label}" "$(command -v "${cmd}")"
  else
    printf '[deps] FAIL %-14s missing command: %s\n' "${label}" "${cmd}" >&2
    fail=1
  fi
}

check_cmd git Git
check_cmd java Java
check_cmd docker Docker
check_cmd make Make

if [[ -x ./mvnw ]]; then
  echo '[deps] OK   Maven wrapper  ./mvnw'
else
  echo '[deps] FAIL Maven wrapper is missing or not executable' >&2
  fail=1
fi

if command -v docker >/dev/null 2>&1; then
  if docker compose version >/dev/null 2>&1; then
    echo "[deps] OK   Docker Compose $(docker compose version --short 2>/dev/null || true)"
  else
    echo '[deps] FAIL Docker Compose plugin is unavailable' >&2
    fail=1
  fi
fi

if command -v java >/dev/null 2>&1; then
  java_version="$(java -version 2>&1 | head -n 1)"
  echo "[deps] INFO Java ${java_version}"
  if ! java -version 2>&1 | head -n 1 | grep -Eq '"21([.\"]|$)'; then
    echo '[deps] FAIL Java 21 is required by the current KeyGo build' >&2
    fail=1
  fi
fi

if (( fail != 0 )); then
  exit 1
fi

echo '[deps] PASS local toolchain baseline'
