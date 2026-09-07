#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${KEYGO_ENV_FILE:-${ROOT_DIR}/.env}"
ENV_TEMPLATE="${ROOT_DIR}/envs/.env.example"
ENV_HOME="${ADUMUN_ENV_HOME:-${HOME}/.config/adumun/env}"
BACKING_FILE="${ENV_HOME}/keygo-server/.env"

if [[ -f "${ENV_FILE}" ]]; then
  echo "[bootstrap] Existing environment preserved: ${ENV_FILE}"
  exit 0
fi

if [[ -f "${BACKING_FILE}" ]]; then
  cp "${BACKING_FILE}" "${ENV_FILE}"
  chmod 600 "${ENV_FILE}" || true
  echo "[bootstrap] Restored environment from ADUMUN_ENV_HOME: ${BACKING_FILE}"
  exit 0
fi

if [[ ! -f "${ENV_TEMPLATE}" ]]; then
  echo "[bootstrap] ERROR: environment template not found: ${ENV_TEMPLATE}" >&2
  exit 1
fi

cp "${ENV_TEMPLATE}" "${ENV_FILE}"
chmod 600 "${ENV_FILE}" || true
cat <<MSG
[bootstrap] Initialized ${ENV_FILE} from envs/.env.example.
[bootstrap] Review unresolved values before starting KeyGo.
[bootstrap] Optional recovery source: ${BACKING_FILE}
MSG
