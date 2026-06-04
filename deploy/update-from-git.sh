#!/bin/bash
# 在服务器 ~/mall 目录执行：./deploy/update-from-git.sh
# 要求：已安装 git、JDK17、Maven、Node18+
# 可选：服务器端编译时使用。当前推荐本机编译+scp，见 docs/git-deploy.md 附录 B
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
DEPLOY="$ROOT/deploy"

export MAVEN_OPTS="${MAVEN_OPTS:--Xmx512m}"
export NODE_OPTIONS="${NODE_OPTIONS:---max-old-space-size=512}"

dc() {
  (cd "$DEPLOY" && docker compose "$@") 2>/dev/null || (cd "$DEPLOY" && sudo docker compose "$@")
}

if [ -f "$DEPLOY/docker-compose.yml" ]; then
  echo ">> docker compose stop (free memory for build)"
  dc stop
fi

echo ">> git pull"
git pull

echo ">> build backend"
mvn clean package -DskipTests -pl ruoyi-admin -am
cp -f ruoyi-admin/target/ruoyi-admin.jar deploy/app/

echo ">> build frontend"
cd ruoyi-ui
if [ -f package-lock.json ]; then
  npm ci
else
  npm install
fi
npm run build:prod
mkdir -p ../deploy/html
rm -rf ../deploy/html/*
cp -r dist/* ../deploy/html/
cd "$ROOT"

echo ">> start containers"
dc up -d
dc ps
echo ">> done"
