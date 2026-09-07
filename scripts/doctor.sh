#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

fail=0
warn=0

ok()   { printf '[doctor] OK   %s\n' "$*"; }
info() { printf '[doctor] INFO %s\n' "$*"; }
warning() { printf '[doctor] WARN %s\n' "$*" >&2; warn=$((warn + 1)); }
error() { printf '[doctor] FAIL %s\n' "$*" >&2; fail=$((fail + 1)); }

for file in .gitignore .gitattributes .dockerignore .editorconfig Makefile compose.yml envs/.env.example; do
  [[ -f "${file}" ]] && ok "control file ${file}" || error "missing control file ${file}"
done

for script in scripts/bootstrap-env.sh scripts/deps.sh scripts/doctor.sh; do
  [[ -f "${script}" ]] && ok "automation ${script}" || error "missing automation ${script}"
done

if command -v git >/dev/null 2>&1; then
  branch="$(git branch --show-current 2>/dev/null || true)"
  [[ -n "${branch}" ]] && info "git branch=${branch}" || warning 'detached HEAD or branch unavailable'
else
  error 'git command unavailable'
fi

if [[ -f .env ]]; then
  if git check-ignore -q .env; then
    ok '.env is ignored by Git'
  else
    error '.env exists but is not ignored by Git'
  fi
else
  warning '.env is absent; run make bootstrap before make up'
fi

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  if docker compose --env-file "${KEYGO_ENV_FILE:-.env}" config -q >/dev/null 2>&1; then
    ok 'Compose configuration parses'
  elif [[ ! -f "${KEYGO_ENV_FILE:-.env}" ]]; then
    warning 'Compose parse skipped because .env is absent; run make bootstrap'
  else
    error 'Compose configuration does not parse with current environment'
  fi
else
  warning 'Docker Compose unavailable; run make deps for toolchain diagnosis'
fi

if grep -RInE 'jdbc:postgresql://[^ ]*(prod|production)|SPRING_PROFILES_ACTIVE=.*prod' .env 2>/dev/null | grep -v '^$' >/dev/null; then
  warning 'current .env contains a production-looking profile/endpoint; verify before local use'
else
  ok 'no obvious production profile/DB fallback detected in local .env'
fi

if [[ -f .github/workflows/ci.yml ]] && grep -Eq 'branches:.*main' .github/workflows/ci.yml; then
  warning 'CI workflow still references main; ADÜMÜN persistent release branch is master'
fi

if [[ -d doc ]]; then
  ok 'legacy doc/ path exists'
else
  warning 'doc/ directory is absent; verify README links that reference doc/...'
fi

if (( fail > 0 )); then
  echo "[doctor] RESULT FAIL (${fail} failure(s), ${warn} warning(s))" >&2
  exit 1
fi

echo "[doctor] RESULT PASS (${warn} warning(s))"
