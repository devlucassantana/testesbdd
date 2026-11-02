#!/usr/bin/env bash
# Smoke test pós-deploy. Uso: bash scripts/smoke-test.sh <porta>
set -euo pipefail
PORT="${1:-8080}"
BASE="http://localhost:${PORT}"
EMAIL="${SMOKE_EMAIL:-admin@openinvestment.com}"
SENHA="${SMOKE_PASS:-Admin@123}"

echo "== 1. Health check (${BASE}/actuator/health) =="
out=""
for _ in $(seq 1 30); do
  out=$(curl -s "${BASE}/actuator/health" || true)
  if echo "$out" | grep -q '"status":"UP"'; then break; fi
  sleep 5
done
echo "$out"
echo "$out" | grep -q '"status":"UP"'

echo "== 2. Login (POST /api/auth/login) =="
resp=$(curl -s -X POST "${BASE}/api/auth/login" -H 'Content-Type: application/json' \
  -d "{\"email\":\"${EMAIL}\",\"senha\":\"${SENHA}\"}")
token=$(echo "$resp" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')
[ -n "$token" ]
echo "Token JWT recebido (${#token} caracteres)"

echo "== 3. GET /api/projetos com token (esperado 200) =="
code=$(curl -s -o /tmp/projetos.json -w '%{http_code}' -H "Authorization: Bearer ${token}" "${BASE}/api/projetos")
echo "HTTP ${code}"
head -c 300 /tmp/projetos.json; echo
[ "$code" = "200" ]

echo "== 4. GET /api/projetos sem token (esperado 401) =="
code=$(curl -s -o /dev/null -w '%{http_code}' "${BASE}/api/projetos")
echo "HTTP ${code}"
[ "$code" = "401" ]

echo "SMOKE TEST OK (porta ${PORT})"
