#!/bin/bash
set -e
ROOT=~/sms-monolith
if [ ! -d "$ROOT" ]; then
  ROOT=~/NewProjectMonolithic
fi
if [ ! -d "$ROOT" ]; then
  echo "Cannot find monolith root"; exit 1
fi
echo "Using ROOT=$ROOT"
mkdir -p "$ROOT/monolith-service/src/main/java/com/sms/auth/exception"
cp /tmp/lockout-mono/auth/service/*.java "$ROOT/monolith-service/src/main/java/com/sms/auth/service/"
cp /tmp/lockout-mono/auth/exception/*.java "$ROOT/monolith-service/src/main/java/com/sms/auth/exception/"
cp /tmp/lockout-mono/auth/entity/*.java "$ROOT/monolith-service/src/main/java/com/sms/auth/entity/"
cp /tmp/lockout-mono/auth/repository/*.java "$ROOT/monolith-service/src/main/java/com/sms/auth/repository/"
cp /tmp/lockout-mono/auth/controller/*.java "$ROOT/monolith-service/src/main/java/com/sms/auth/controller/"
cp /tmp/lockout-mono/config/MonolithGlobalExceptionHandler.java "$ROOT/monolith-service/src/main/java/com/sms/monolith/config/"
cp /tmp/lockout-mono/security/JwtAuthenticationFilter.java "$ROOT/monolith-service/src/main/java/com/sms/monolith/security/"
cp /tmp/lockout-mono/common/*.java "$ROOT/common-lib/src/main/java/com/sms/common/dto/"
cp /tmp/lockout-mono/resources/application.yml "$ROOT/monolith-service/src/main/resources/application.yml"
cp /tmp/lockout-mono/resources/docker-compose.prod.yml "$ROOT/docker-compose.prod.yml"
cp /tmp/lockout-mono/frontend/pages/*.tsx "$ROOT/frontend/src/pages/"
cp /tmp/lockout-mono/frontend/auth.ts "$ROOT/frontend/src/api/auth.ts"
cp /tmp/lockout-mono/frontend/client.ts "$ROOT/frontend/src/api/client.ts"
cp /tmp/lockout-mono/frontend/App.tsx "$ROOT/frontend/src/App.tsx"
cp /tmp/lockout-mono/frontend/index.ts "$ROOT/frontend/src/types/index.ts"
if [ -f "$ROOT/.env" ] && ! grep -q PASSWORD_RESET_BASE_URL "$ROOT/.env"; then
  echo 'PASSWORD_RESET_BASE_URL=https://veritascampus.page/reset-password' >> "$ROOT/.env"
fi
echo "Mono files installed"
ls -la "$ROOT/monolith-service/src/main/java/com/sms/auth/service/AuthLockService.java"
