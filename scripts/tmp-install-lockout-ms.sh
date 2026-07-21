#!/bin/bash
set -e
ROOT=~/NewProject
mkdir -p "$ROOT/auth-service/src/main/java/com/sms/auth/exception"
cp /tmp/lockout-ms/auth/service/*.java "$ROOT/auth-service/src/main/java/com/sms/auth/service/"
cp /tmp/lockout-ms/auth/exception/*.java "$ROOT/auth-service/src/main/java/com/sms/auth/exception/"
cp /tmp/lockout-ms/auth/entity/*.java "$ROOT/auth-service/src/main/java/com/sms/auth/entity/"
cp /tmp/lockout-ms/auth/repository/*.java "$ROOT/auth-service/src/main/java/com/sms/auth/repository/"
cp /tmp/lockout-ms/auth/controller/*.java "$ROOT/auth-service/src/main/java/com/sms/auth/controller/"
cp /tmp/lockout-ms/auth/config/GlobalExceptionHandler.java "$ROOT/auth-service/src/main/java/com/sms/auth/config/"
cp /tmp/lockout-ms/auth/config/application.yml "$ROOT/auth-service/src/main/resources/application.yml"
cp /tmp/lockout-ms/common/*.java "$ROOT/common-lib/src/main/java/com/sms/common/dto/"
cp /tmp/lockout-ms/gateway/JwtAuthenticationFilter.java "$ROOT/api-gateway/src/main/java/com/sms/gateway/filter/"
cp /tmp/lockout-ms/gateway/docker-compose.yml "$ROOT/docker-compose.yml"
cp /tmp/lockout-ms/frontend/pages/*.tsx "$ROOT/frontend/src/pages/"
cp /tmp/lockout-ms/frontend/auth.ts "$ROOT/frontend/src/api/auth.ts"
cp /tmp/lockout-ms/frontend/client.ts "$ROOT/frontend/src/api/client.ts"
cp /tmp/lockout-ms/frontend/App.tsx "$ROOT/frontend/src/App.tsx"
cp /tmp/lockout-ms/frontend/index.ts "$ROOT/frontend/src/types/index.ts"
if [ -f "$ROOT/.env" ] && ! grep -q PASSWORD_RESET_BASE_URL "$ROOT/.env"; then
  echo 'PASSWORD_RESET_BASE_URL=https://veritascampus.me/reset-password' >> "$ROOT/.env"
fi
# ensure mail starter in auth pom if missing
if ! grep -q spring-boot-starter-mail "$ROOT/auth-service/pom.xml"; then
  python3 - <<'PY'
from pathlib import Path
p = Path.home() / "NewProject/auth-service/pom.xml"
text = p.read_text()
needle = "</dependencies>"
dep = """
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-mail</artifactId>
        </dependency>
"""
if "spring-boot-starter-mail" not in text:
    text = text.replace(needle, dep + "\n    " + needle, 1)
    p.write_text(text)
    print("added mail dependency")
else:
    print("mail dependency already present")
PY
fi
echo "MS files installed"
ls -la "$ROOT/auth-service/src/main/java/com/sms/auth/service/AuthLockService.java"
ls -la "$ROOT/auth-service/src/main/java/com/sms/auth/exception/LoginRejectedException.java"
