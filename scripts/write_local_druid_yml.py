# -*- coding: utf-8 -*-
"""Write complete application-druid.yml for local dev (UTF-8 safe)."""
import os
from pathlib import Path

MYSQL_PASSWORD = os.environ.get("MYSQL_ROOT_PASSWORD", "ChangeMe_LocalMySql")

CONTENT = f"""# Local MySQL (do not commit; in .gitignore)
spring:
    datasource:
        type: com.alibaba.druid.pool.DruidDataSource
        driverClassName: com.mysql.cj.jdbc.Driver
        druid:
            master:
                url: jdbc:mysql://localhost:3306/ry-vue?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
                username: root
                password: {MYSQL_PASSWORD}
            slave:
                enabled: false
                url:
                username:
                password:
            initialSize: 5
            minIdle: 10
            maxActive: 20
            maxWait: 60000
            connectTimeout: 30000
            socketTimeout: 60000
            timeBetweenEvictionRunsMillis: 60000
            minEvictableIdleTimeMillis: 300000
            maxEvictableIdleTimeMillis: 900000
            validationQuery: SELECT 1 FROM DUAL
            testWhileIdle: true
            testOnBorrow: false
            testOnReturn: false
            webStatFilter:
                enabled: true
            statViewServlet:
                enabled: true
                allow:
                url-pattern: /druid/*
                login-username: ruoyi
                login-password: 123456
            filter:
                stat:
                    enabled: true
                    log-slow-sql: true
                    slow-sql-millis: 1000
                    merge-sql: true
                wall:
                    config:
                        multi-statement-allow: true
"""

def main() -> None:
    path = Path(__file__).resolve().parent.parent / 'ruoyi-admin' / 'src' / 'main' / 'resources' / 'application-druid.yml'
    path.write_text(CONTENT, encoding='utf-8', newline='\n')
    print('wrote', path)


if __name__ == '__main__':
    main()
