#!/bin/bash
# 在服务器 ~/mall 目录执行：./deploy/update-from-git.sh
# 要求：已安装 git、JDK17、Maven、Node18+
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

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

echo ">> restart containers"
cd deploy
docker compose restart mall-api nginx
docker compose ps
echo ">> done"
