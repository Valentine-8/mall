#!/bin/bash
# 在服务器项目根目录执行：./deploy/run-sql.sh <sql文件> [更多文件...]
# 示例：
#   ./deploy/run-sql.sh sql/mall_social.sql
#   ./deploy/run-sql.sh sql/mall_order_status_refund.sql sql/mall_social.sql
#   ./deploy/run-sql.sh --extra    # 执行常用补充脚本（社交登录、退款字典）
set -e

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
DEPLOY="$ROOT/deploy"
SQL_DIR="$ROOT/sql"
ENV_FILE="$DEPLOY/.env"
DB_NAME="ry-vue"

dc() {
  (cd "$DEPLOY" && docker compose "$@") 2>/dev/null || (cd "$DEPLOY" && sudo docker compose "$@")
}

usage() {
  echo "用法:"
  echo "  ./deploy/run-sql.sh <sql路径> [路径2 ...]"
  echo "  ./deploy/run-sql.sh --extra          # mall_social + mall_order_status_refund"
  echo "  ./deploy/run-sql.sh --list           # 列出 sql/ 目录下脚本"
  echo ""
  echo "示例:"
  echo "  ./deploy/run-sql.sh sql/mall_social.sql"
  echo "  ./deploy/run-sql.sh mall_social.sql    # 可只写文件名，自动在 sql/ 下查找"
}

load_env() {
  if [ ! -f "$ENV_FILE" ]; then
    echo "错误: 未找到 $ENV_FILE，请先 cp .env.example .env 并设置 MYSQL_ROOT_PASSWORD"
    exit 1
  fi
  # shellcheck disable=SC1090
  set -a
  source "$ENV_FILE"
  set +a
  if [ -z "${MYSQL_ROOT_PASSWORD:-}" ]; then
    echo "错误: .env 中未设置 MYSQL_ROOT_PASSWORD"
    exit 1
  fi
}

resolve_sql() {
  local arg="$1"
  local path=""
  if [ -f "$arg" ]; then
    path="$arg"
  elif [ -f "$ROOT/$arg" ]; then
    path="$ROOT/$arg"
  elif [ -f "$SQL_DIR/$arg" ]; then
    path="$SQL_DIR/$arg"
  elif [ -f "$SQL_DIR/$(basename "$arg")" ]; then
    path="$SQL_DIR/$(basename "$arg")"
  else
    echo "错误: 找不到 SQL 文件: $arg"
    exit 1
  fi
  echo "$path"
}

check_mysql() {
  if ! dc ps --status running 2>/dev/null | grep -q mall-mysql; then
    if ! dc ps 2>/dev/null | grep -q mall-mysql; then
      echo "错误: MySQL 容器 mall-mysql 未运行，请先: cd deploy && docker compose up -d mysql"
      exit 1
    fi
  fi
}

run_one() {
  local file
  file="$(resolve_sql "$1")"
  echo ">> 执行: $file  (数据库: $DB_NAME)"
  dc exec -T mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$DB_NAME" < "$file"
  echo ">> 完成: $(basename "$file")"
}

if [ $# -eq 0 ]; then
  usage
  exit 1
fi

case "$1" in
  -h|--help)
    usage
    exit 0
    ;;
  --list)
    echo "sql/ 目录下的脚本:"
    ls -1 "$SQL_DIR"/*.sql 2>/dev/null || echo "(无)"
    exit 0
    ;;
  --extra)
    load_env
    check_mysql
    run_one "sql/mall_social.sql"
    run_one "sql/mall_order_status_refund.sql"
    echo ">> 全部完成"
    exit 0
    ;;
esac

load_env
check_mysql

for arg in "$@"; do
  run_one "$arg"
done

echo ">> 全部完成"
