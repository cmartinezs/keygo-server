#!/bin/bash
# =============================================================================
# setup-keygo-tenant.sh
# Bootstrap script: creates the main 'keygo' tenant + keygo-ui client app,
# and a 'dev-demo' tenant for UI development testing.
#
# Prerequisites:
#   - App must be running with the 'supabase' profile (signing key auto-generated)
#   - KEYGO_ADMIN_KEY env var (default: changeMe)
#   - BASE_URL env var (default: http://localhost:8080/keygo-server)
#
# Usage:
#   ./scripts/setup-keygo-tenant.sh
#   KEYGO_ADMIN_KEY=myKey BASE_URL=http://localhost:8080/keygo-server ./scripts/setup-keygo-tenant.sh
# =============================================================================
set -euo pipefail

# ─── Config ──────────────────────────────────────────────────────────────────

BASE_URL="${BASE_URL:-http://localhost:8080/keygo-server}"
ADMIN_KEY="${KEYGO_ADMIN_KEY:-changeMe}"
API="${BASE_URL}/api/v1"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ─── Helpers ─────────────────────────────────────────────────────────────────

info()    { echo -e "${CYAN}ℹ  $*${NC}"; }
success() { echo -e "${GREEN}✅ $*${NC}"; }
warn()    { echo -e "${YELLOW}⚠  $*${NC}"; }
error()   { echo -e "${RED}❌ $*${NC}" >&2; }

require_cmd() {
  if ! command -v "$1" &>/dev/null; then
    error "Required command not found: $1"
    exit 1
  fi
}

require_cmd curl
require_cmd jq

# ─── Wait for app ────────────────────────────────────────────────────────────

wait_for_app() {
  local max=30
  local i=0
  info "Waiting for app at ${BASE_URL}/actuator/health ..."
  until curl -sf "${BASE_URL}/actuator/health" | jq -e '.status == "UP"' > /dev/null 2>&1; do
    i=$((i + 1))
    if [ "$i" -ge "$max" ]; then
      error "App is not healthy after ${max} attempts. Is it running?"
      exit 1
    fi
    sleep 2
  done
  success "App is up and healthy"
}

# ─── API helpers ─────────────────────────────────────────────────────────────

api_post() {
  local path="$1"
  local body="$2"
  curl -sf -X POST \
    -H "Content-Type: application/json" \
    -H "X-KEYGO-ADMIN: ${ADMIN_KEY}" \
    -d "$body" \
    "${API}${path}"
}

api_get() {
  local path="$1"
  curl -sf -X GET \
    -H "X-KEYGO-ADMIN: ${ADMIN_KEY}" \
    "${API}${path}"
}

extract() {
  # Usage: extract <json> <jq_path>
  echo "$1" | jq -r "$2"
}

# ─── Create tenant ───────────────────────────────────────────────────────────

create_tenant() {
  local name="$1"
  local owner_email="$2"
  info "Creating tenant: ${name} ..."
  local resp
  resp=$(api_post "/tenants" "{\"name\":\"${name}\",\"ownerEmail\":\"${owner_email}\"}")
  local slug
  slug=$(extract "$resp" '.data.slug')
  success "Tenant created: slug=${slug}"
  echo "$slug"
}

# ─── Create client app ───────────────────────────────────────────────────────

create_app() {
  local tenant_slug="$1"
  local app_name="$2"
  local app_type="$3"        # PUBLIC or CONFIDENTIAL
  local redirect_uri="$4"
  local grants="$5"          # JSON array string e.g. '["AUTHORIZATION_CODE"]'
  local scopes="$6"          # JSON array string e.g. '["openid","profile","email"]'

  info "Creating app '${app_name}' in tenant '${tenant_slug}' ..."
  local body
  body=$(jq -n \
    --arg name "$app_name" \
    --arg type "$app_type" \
    --arg uri "$redirect_uri" \
    --argjson grants "$grants" \
    --argjson scopes "$scopes" \
    '{name:$name, type:$type, redirectUris:[$uri], grants:$grants, scopes:$scopes}')

  local resp
  resp=$(api_post "/tenants/${tenant_slug}/apps" "$body")
  local client_id
  client_id=$(extract "$resp" '.data.clientId')
  local app_id
  app_id=$(extract "$resp" '.data.id')
  local client_secret
  client_secret=$(extract "$resp" '.data.clientSecret // "N/A (PUBLIC client)"')
  success "App created: clientId=${client_id} id=${app_id}"
  echo "${app_id}|${client_id}|${client_secret}"
}

# ─── Create app role ─────────────────────────────────────────────────────────

create_role() {
  local tenant_slug="$1"
  local app_id="$2"
  local code="$3"
  local display_name="$4"
  info "Creating role '${code}' in app ${app_id} ..."
  local resp
  resp=$(api_post "/tenants/${tenant_slug}/apps/${app_id}/roles" \
    "{\"code\":\"${code}\",\"displayName\":\"${display_name}\"}")
  local role_id
  role_id=$(extract "$resp" '.data.id')
  success "Role created: code=${code} id=${role_id}"
  echo "$role_id"
}

# ─── Create user ─────────────────────────────────────────────────────────────

create_user() {
  local tenant_slug="$1"
  local username="$2"
  local email="$3"
  local password="$4"
  local first_name="$5"
  local last_name="$6"
  info "Creating user '${username}' in tenant '${tenant_slug}' ..."
  local body
  body=$(jq -n \
    --arg u "$username" --arg e "$email" --arg p "$password" \
    --arg f "$first_name" --arg l "$last_name" \
    '{username:$u, email:$e, password:$p, firstName:$f, lastName:$l}')
  local resp
  resp=$(api_post "/tenants/${tenant_slug}/users" "$body")
  local user_id
  user_id=$(extract "$resp" '.data.id')
  success "User created: username=${username} id=${user_id}"
  echo "$user_id"
}

# ─── Create membership ───────────────────────────────────────────────────────

create_membership() {
  local tenant_slug="$1"
  local user_id="$2"
  local app_id="$3"
  local role_codes_json="$4"   # JSON array of role code strings e.g. '["admin"]'
  info "Creating membership for user ${user_id} in app ${app_id} ..."
  local body
  body=$(jq -n \
    --arg uid "$user_id" --arg aid "$app_id" \
    --argjson codes "$role_codes_json" \
    '{userId:$uid, clientAppId:$aid, roleCodes:$codes}')
  local resp
  resp=$(api_post "/tenants/${tenant_slug}/memberships" "$body")
  local mem_id
  mem_id=$(extract "$resp" '.data.id')
  success "Membership created: id=${mem_id}"
  echo "$mem_id"
}

# =============================================================================
# MAIN
# =============================================================================

echo ""
echo -e "${CYAN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║         KeyGo Tenant Bootstrap Setup Script              ║${NC}"
echo -e "${CYAN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "  BASE_URL   : ${BASE_URL}"
echo "  ADMIN_KEY  : ${ADMIN_KEY:0:4}****"
echo ""

wait_for_app

# ─────────────────────────────────────────────────────────────────────────────
# 1. Tenant: keygo  (main platform tenant)
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo -e "${CYAN}── [1/2] Setting up main tenant: keygo ────────────────────${NC}"

KEYGO_SLUG=$(create_tenant "keygo" "admin@keygo.local")

# 1a. Client app: keygo-ui (PUBLIC, authorization_code + PKCE)
KEYGO_UI_RESULT=$(create_app "$KEYGO_SLUG" "keygo-ui" "PUBLIC" \
  "http://localhost:5173/callback" \
  '["AUTHORIZATION_CODE"]' \
  '["openid","profile","email"]')

KEYGO_UI_APP_ID=$(echo "$KEYGO_UI_RESULT" | cut -d'|' -f1)
KEYGO_UI_CLIENT_ID=$(echo "$KEYGO_UI_RESULT" | cut -d'|' -f2)

# 1b. Role: admin  (in keygo-ui)
create_role "$KEYGO_SLUG" "$KEYGO_UI_APP_ID" "admin" "Administrator" > /dev/null

# 1c. Admin user
ADMIN_USER_ID=$(create_user "$KEYGO_SLUG" "admin" "admin@keygo.local" \
  "Admin1234!" "Platform" "Admin")

# 1d. Membership: admin user → keygo-ui with role 'admin'
create_membership "$KEYGO_SLUG" "$ADMIN_USER_ID" "$KEYGO_UI_APP_ID" '["admin"]' > /dev/null

# ─────────────────────────────────────────────────────────────────────────────
# 2. Tenant: dev-demo  (for UI development & testing)
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo -e "${CYAN}── [2/2] Setting up dev tenant: dev-demo ───────────────────${NC}"

DEV_SLUG=$(create_tenant "dev-demo" "devadmin@dev-demo.local")

# 2a. Client app: demo-app (PUBLIC, authorization_code + PKCE)
DEV_APP_RESULT=$(create_app "$DEV_SLUG" "demo-app" "PUBLIC" \
  "http://localhost:5173/callback" \
  '["AUTHORIZATION_CODE"]' \
  '["openid","profile","email"]')

DEV_APP_ID=$(echo "$DEV_APP_RESULT" | cut -d'|' -f1)
DEV_CLIENT_ID=$(echo "$DEV_APP_RESULT" | cut -d'|' -f2)

# 2b. Roles: admin + user (in demo-app)
create_role "$DEV_SLUG" "$DEV_APP_ID" "admin" "Admin" > /dev/null
create_role "$DEV_SLUG" "$DEV_APP_ID" "user" "Regular User" > /dev/null

# 2c. Dev admin user
DEV_ADMIN_ID=$(create_user "$DEV_SLUG" "devadmin" "devadmin@dev-demo.local" \
  "DevAdmin1!" "Dev" "Admin")

# 2d. Dev regular user
DEV_USER_ID=$(create_user "$DEV_SLUG" "devuser" "devuser@dev-demo.local" \
  "DevUser1!" "Dev" "User")

# 2e. Memberships
create_membership "$DEV_SLUG" "$DEV_ADMIN_ID" "$DEV_APP_ID" '["admin","user"]' > /dev/null
create_membership "$DEV_SLUG" "$DEV_USER_ID"  "$DEV_APP_ID" '["user"]' > /dev/null

# ─────────────────────────────────────────────────────────────────────────────
# Summary
# ─────────────────────────────────────────────────────────────────────────────
echo ""
echo -e "${GREEN}╔══════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                  Setup Complete!                         ║${NC}"
echo -e "${GREEN}╚══════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${CYAN}── Tenant: keygo ───────────────────────────────────────────${NC}"
echo "  Slug         : ${KEYGO_SLUG}"
echo "  App          : keygo-ui"
echo "  Client ID    : ${KEYGO_UI_CLIENT_ID}"
echo "  Type         : PUBLIC (PKCE)"
echo "  Redirect URI : http://localhost:5173/callback"
echo "  Admin user   : admin / Admin1234!"
echo "  Admin email  : admin@keygo.local"
echo ""
echo -e "${CYAN}── Tenant: dev-demo ────────────────────────────────────────${NC}"
echo "  Slug         : ${DEV_SLUG}"
echo "  App          : demo-app"
echo "  Client ID    : ${DEV_CLIENT_ID}"
echo "  Type         : PUBLIC (PKCE)"
echo "  Redirect URI : http://localhost:5173/callback"
echo "  Admin user   : devadmin / DevAdmin1!  (roles: admin + user)"
echo "  Regular user : devuser  / DevUser1!   (roles: user)"
echo ""
echo -e "${YELLOW}OAuth2 flow (Authorization Code + PKCE):${NC}"
echo "  Discovery    : ${BASE_URL}/api/v1/tenants/${KEYGO_SLUG}/.well-known/openid-configuration"
echo "  Authorize    : ${BASE_URL}/api/v1/tenants/${KEYGO_SLUG}/oauth2/authorize"
echo "  Login        : ${BASE_URL}/api/v1/tenants/${KEYGO_SLUG}/account/login"
echo "  Token        : ${BASE_URL}/api/v1/tenants/${KEYGO_SLUG}/oauth2/token"
echo ""
echo -e "${YELLOW}For the UI, set these env vars:${NC}"
echo "  VITE_KEYGO_BASE_URL=${BASE_URL}"
echo "  VITE_KEYGO_TENANT=${KEYGO_SLUG}"
echo "  VITE_KEYGO_CLIENT_ID=${KEYGO_UI_CLIENT_ID}"
echo "  VITE_KEYGO_REDIRECT_URI=http://localhost:5173/callback"
echo ""

