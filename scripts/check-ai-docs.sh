#!/bin/bash
# =========================================================
# check-ai-docs.sh — Verificador de actividad del agente AI
# =========================================================
# Verifica que los documentos de base de conocimiento AI
# tengan entradas recientes dentro de los últimos DAYS_THRESHOLD días:
#
#   • docs/ai/lecciones.md   → sección "## Lecciones"
#   • docs/ai/agents-registro.md → sección "## Registro de cambios"
#
# Códigos de salida:
#   0 → Todos los documentos tienen actividad reciente (OK)
#   1 → Documentos con entradas pero ninguna es reciente (ALERTA)
#   2 → Algún documento sin ninguna entrada (SIN ACTIVIDAD)
#   3 → Archivo requerido no encontrado (ERROR)
#
# Uso:
#   ./scripts/check-ai-docs.sh
#   ./scripts/check-ai-docs.sh --days 60   # Cambiar umbral
#   ./scripts/check-ai-docs.sh --quiet     # Solo código de salida
# =========================================================

set -uo pipefail

# ----------------------------------------------------------
# Colores
# ----------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

# ----------------------------------------------------------
# Configuración por defecto
# ----------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
AI_LECCIONES="$REPO_ROOT/docs/ai/lecciones.md"
AGENTS_REGISTRO="$REPO_ROOT/docs/ai/agents-registro.md"
DAYS_THRESHOLD=30
QUIET=false

# ----------------------------------------------------------
# Parseo de argumentos
# ----------------------------------------------------------
while [[ $# -gt 0 ]]; do
  case "$1" in
    --days)
      DAYS_THRESHOLD="$2"
      shift 2
      ;;
    --quiet|-q)
      QUIET=true
      shift
      ;;
    --help|-h)
      sed -n '2,21p' "$0" | sed 's/^# \?//'
      exit 0
      ;;
    *)
      echo -e "${RED}Argumento desconocido: $1${NC}" >&2
      exit 3
      ;;
  esac
done

# ----------------------------------------------------------
# Helpers
# ----------------------------------------------------------
log() {
  $QUIET || echo -e "$*"
}

print_header() {
  log ""
  log "${BOLD}${CYAN}╔══════════════════════════════════════════════╗${NC}"
  log "${BOLD}${CYAN}║   KeyGo — Verificador de Actividad AI        ║${NC}"
  log "${BOLD}${CYAN}╚══════════════════════════════════════════════╝${NC}"
  log ""
}

# Convierte YYYY-MM-DD a epoch segundos.
# Compatible con GNU date (Linux) y BSD date (macOS).
date_to_epoch() {
  local date_str="$1"
  if date --version >/dev/null 2>&1; then
    date -d "$date_str" +%s 2>/dev/null || echo ""
  else
    date -j -f "%Y-%m-%d" "$date_str" +%s 2>/dev/null || echo ""
  fi
}

# ----------------------------------------------------------
# check_section FILE SECTION_LABEL
#
# Extrae entradas ### [YYYY-MM-DD] de la sección indicada
# dentro de FILE, ignorando bloques <!-- -->.
#
# Escribe en variables globales (limpiadas al inicio):
#   _check_found  → array con todas las fechas encontradas
#   _check_recent → array con fechas dentro del umbral
# ----------------------------------------------------------
_check_found=()
_check_recent=()

check_section() {
  local file="$1"
  local section_label="$2"   # texto exacto después de "## "
  _check_found=()
  _check_recent=()

  local in_section=false
  local in_html_comment=false

  while IFS= read -r line; do

    # Gestión de bloques <!-- ... -->
    if [[ "$line" == *"<!--"* ]]; then in_html_comment=true; fi
    if $in_html_comment; then
      [[ "$line" == *"-->"* ]] && in_html_comment=false
      continue
    fi

    # Activar sección
    if [[ "$line" =~ ^##[[:space:]]${section_label} ]]; then
      in_section=true
      continue
    fi

    # Desactivar al encontrar otro ## (pero no ###)
    if $in_section && [[ "$line" =~ ^##[^#] ]]; then
      break
    fi

    # Detectar entradas ### [YYYY-MM-DD]
    if $in_section && [[ "$line" =~ ^\#\#\#[[:space:]]+\[([0-9]{4}-[0-9]{2}-[0-9]{2})\] ]]; then
      local entry_date="${BASH_REMATCH[1]}"
      _check_found+=("$entry_date")
      local entry_epoch
      entry_epoch=$(date_to_epoch "$entry_date")
      if [[ -n "$entry_epoch" && "$entry_epoch" -ge "$THRESHOLD_EPOCH" ]]; then
        _check_recent+=("$entry_date")
      fi
    fi

  done < "$file"
}

# ----------------------------------------------------------
# report_result FILE LABEL SECTION_LABEL
#
# Llama a check_section y muestra el resultado detallado.
# Devuelve: 0=OK, 1=sin recientes, 2=sin entradas
# ----------------------------------------------------------
report_result() {
  local file="$1"
  local label="$2"
  local section_label="$3"

  log "  ${BOLD}${label}${NC}  ${CYAN}$(basename "$file")${NC} → ${CYAN}## ${section_label}${NC}"

  check_section "$file" "$section_label"

  local total=${#_check_found[@]}
  local recent=${#_check_recent[@]}

  if [[ $total -eq 0 ]]; then
    log "  ${YELLOW}⚠  Sin entradas. Agrega una con formato: ### [YYYY-MM-DD] Descripción${NC}"
    log ""
    return 2
  fi

  for d in "${_check_found[@]}"; do
    local entry_epoch
    entry_epoch=$(date_to_epoch "$d")
    if [[ -n "$entry_epoch" && "$entry_epoch" -ge "$THRESHOLD_EPOCH" ]]; then
      log "  ${GREEN}✓${NC} ${BOLD}${d}${NC}  ${GREEN}← dentro de los últimos ${DAYS_THRESHOLD} días${NC}"
    else
      local days_ago=$(( (NOW_EPOCH - entry_epoch) / 86400 ))
      log "  ${YELLOW}○${NC} ${d}  (hace ${days_ago} días)"
    fi
  done

  if [[ $recent -gt 0 ]]; then
    log "  ${GREEN}→ ${recent}/${total} entrada(s) reciente(s)${NC}"
    log ""
    return 0
  else
    log "  ${RED}→ 0/${total} entrada(s) reciente(s) — supera el umbral de ${DAYS_THRESHOLD} días${NC}"
    log ""
    return 1
  fi
}

# ----------------------------------------------------------
# Inicio
# ----------------------------------------------------------
print_header

# Verificar existencia de archivos requeridos
missing=false
for f in "$AI_LECCIONES" "$AGENTS_REGISTRO"; do
  if [[ ! -f "$f" ]]; then
    log "${RED}✗ No se encontró: ${CYAN}$f${NC}"
    missing=true
  fi
done
$missing && { log ""; exit 3; }

TODAY=$(date +%Y-%m-%d)
NOW_EPOCH=$(date_to_epoch "$TODAY")
THRESHOLD_EPOCH=$(( NOW_EPOCH - DAYS_THRESHOLD * 86400 ))
THRESHOLD_DATE=$(date -d "@$THRESHOLD_EPOCH" +%Y-%m-%d 2>/dev/null \
  || date -r "$THRESHOLD_EPOCH" +%Y-%m-%d 2>/dev/null \
  || echo "N/A")

log "📅 Hoy:     ${CYAN}${TODAY}${NC}"
log "⏱  Umbral:  ${CYAN}${DAYS_THRESHOLD} días${NC}  (desde ${CYAN}${THRESHOLD_DATE}${NC})"
log ""
log "${BOLD}Resultados:${NC}"
log "──────────────────────────────────────────────"

# ----------------------------------------------------------
# Verificar cada documento
# ----------------------------------------------------------
exit_lecciones=0
exit_registro=0

report_result "$AI_LECCIONES"    "①" "Lecciones" || exit_lecciones=$?
report_result "$AGENTS_REGISTRO" "②" "Registro de cambios"  || exit_registro=$?

# ----------------------------------------------------------
# Resumen final
# ----------------------------------------------------------
log "──────────────────────────────────────────────"

worst=$(( exit_lecciones > exit_registro ? exit_lecciones : exit_registro ))

case $worst in
  0)
    log "${GREEN}${BOLD}✅ OK — Todos los documentos AI están actualizados.${NC}"
    log "   El agente está activo y retroalimentando el conocimiento del proyecto."
    ;;
  1)
    log "${RED}${BOLD}✗  ALERTA — Sin actividad reciente (últimos ${DAYS_THRESHOLD} días).${NC}"
    log "   Actualiza los documentos marcados con ${RED}✗${NC} al concluir cada tarea relevante:"
    log "     • ${CYAN}docs/ai/lecciones.md${NC}      → sección ${CYAN}## Lecciones${NC}"
    log "     • ${CYAN}docs/ai/agents-registro.md${NC} → sección ${CYAN}## Registro de cambios${NC}"
    ;;
  2)
    log "${YELLOW}${BOLD}⚠  SIN ACTIVIDAD — Algún documento no tiene entradas registradas.${NC}"
    log "   Agrega la primera entrada con el formato:"
    log "   ${CYAN}### [YYYY-MM-DD] Título descriptivo${NC}"
    ;;
esac

log ""
exit $worst
