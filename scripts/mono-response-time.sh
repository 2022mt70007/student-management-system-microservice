#!/bin/bash
set -euo pipefail

BASE="${1:-http://localhost:8090}"
OUT="/tmp/mono-rt-results.txt"
: > "$OUT"

measure() {
  local id="$1" run="$2" method="$3" path="$4" extra="${5:-}"
  local tmpbody="/tmp/rt-body.json"
  local url="${BASE}${path}"
  local code time_ms

  if [ -n "$extra" ] && [ -f "$extra" ]; then
    read -r code time_ms < <(curl -s -o /dev/null -w "%{http_code} %{time_total}" -X "$method" "$url" -H "Content-Type: application/json" -H "Authorization: Bearer ${ADMIN_TOKEN:-}" -d @"$extra")
  elif [ "$method" = "GET" ]; then
    read -r code time_ms < <(curl -s -o /dev/null -w "%{http_code} %{time_total}" -X GET "$url" -H "Authorization: Bearer ${ADMIN_TOKEN:-}")
  else
    read -r code time_ms < <(curl -s -o /dev/null -w "%{http_code} %{time_total}" -X "$method" "$url" -H "Content-Type: application/json" -d @"$tmpbody")
  fi
  # convert seconds to ms
  time_ms=$(awk -v t="$time_ms" 'BEGIN{printf "%.0f", t*1000}')
  echo "${id},${run},${code},${time_ms}" | tee -a "$OUT"
}

# Login body
cat > /tmp/rt-body.json <<'EOF'
{"email":"sidharthsangamam@gmail.com","password":"wrong-password-rt-test"}
EOF

echo "=== RT-01 Login (10 req x 3 runs) ==="
for run in 1 2 3; do
  for i in $(seq 1 10); do
    measure "RT-01" "$run" "POST" "/api/auth/login" "/tmp/rt-body.json"
  done
done

# Try to get admin token with provided password from env or skip dashboards
if [ -n "${ADMIN_PASSWORD:-}" ]; then
  cat > /tmp/rt-login-real.json <<EOF
{"email":"sidharthsangamam@gmail.com","password":"${ADMIN_PASSWORD}"}
EOF
  RESP=$(curl -s -X POST "${BASE}/api/auth/login" -H "Content-Type: application/json" -d @/tmp/rt-login-real.json)
  ADMIN_TOKEN=$(echo "$RESP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('data',{}).get('token','') or '')" 2>/dev/null || true)
fi

if [ -n "${ADMIN_TOKEN:-}" ]; then
  echo "=== RT-02 Admin dashboard ==="
  for run in 1 2 3; do
    for i in $(seq 1 10); do
      measure "RT-02" "$run" "GET" "/api/admin/dashboard"
    done
  done
else
  echo "SKIP RT-02: no admin token (set ADMIN_PASSWORD env)"
fi

if [ -n "${STUDENT_TOKEN:-}" ]; then
  echo "=== RT-03 Student dashboard ==="
  ADMIN_TOKEN_SAVE=$ADMIN_TOKEN
  ADMIN_TOKEN=$STUDENT_TOKEN
  for run in 1 2 3; do
    for i in $(seq 1 10); do
      measure "RT-03" "$run" "GET" "/api/students/dashboard"
    done
  done
  ADMIN_TOKEN=$ADMIN_TOKEN_SAVE
else
  echo "SKIP RT-03: no student on monolith"
fi

if [ -n "${TEACHER_TOKEN:-}" ]; then
  echo "=== RT-04 Teacher dashboard ==="
  ADMIN_TOKEN_SAVE=$ADMIN_TOKEN
  ADMIN_TOKEN=$TEACHER_TOKEN
  for run in 1 2 3; do
    for i in $(seq 1 10); do
      measure "RT-04" "$run" "GET" "/api/teachers/dashboard"
    done
  done
else
  echo "SKIP RT-04: no teacher on monolith"
fi

echo "=== Summary (avg ms per scenario/run) ==="
python3 <<'PY'
from collections import defaultdict
import statistics, os
groups = defaultdict(list)
path = "/tmp/mono-rt-results.txt"
if not os.path.exists(path):
    print("no results")
    raise SystemExit(0)
for line in open(path):
    line=line.strip()
    if not line: continue
    sid, run, code, ms = line.split(',')
    groups[(sid, run)].append(int(ms))
for (sid, run), vals in sorted(groups.items()):
    avg = round(statistics.mean(vals))
    p95 = sorted(vals)[max(0, int(len(vals)*0.95)-1)]
    print(f"{sid} run{run}: n={len(vals)} avg={avg}ms p95={p95}ms")
PY

echo "Results file: $OUT"
