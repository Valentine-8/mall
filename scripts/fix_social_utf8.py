# -*- coding: utf-8 -*-
import os

ROOT = os.path.join(os.path.dirname(__file__), '..')

SQL = (
    "-- Third-party login bind table (WeChat / Alipay)\n"
    "USE `ry-vue`;\n\n"
    "DROP TABLE IF EXISTS sys_social_bind;\n"
    "CREATE TABLE sys_social_bind (\n"
    "  bind_id        bigint(20)   NOT NULL AUTO_INCREMENT,\n"
    "  user_id        bigint(20)   NOT NULL,\n"
    "  social_type    varchar(20)  NOT NULL,\n"
    "  open_id        varchar(128) NOT NULL,\n"
    "  union_id       varchar(128) DEFAULT NULL,\n"
    "  nickname       varchar(64)  DEFAULT '',\n"
    "  avatar         varchar(255) DEFAULT '',\n"
    "  create_time    datetime,\n"
    "  PRIMARY KEY (bind_id),\n"
    "  UNIQUE KEY uk_type_open (social_type, open_id)\n"
    ") ENGINE=InnoDB;\n"
)

MSGS = [
    "\u672a\u542f\u7528\u5fae\u4fe1\u767b\u5f55",
    "\u8bf7\u5728 application.yml \u914d\u7f6e social.wechat.app-id \u4e0e app-secret",
    "\u672a\u542f\u7528\u652f\u4ed8\u5b9d\u767b\u5f55",
    "\u8bf7\u5728 application.yml \u914d\u7f6e social.alipay.app-id",
    "\u4e0d\u652f\u6301\u7684\u767b\u5f55\u65b9\u5f0f",
    "\u6a21\u62df\u767b\u5f55\u672a\u5f00\u542f",
    "\u6388\u6743\u7801\u4e3a\u7a7a",
    "\u652f\u4ed8\u5b9d\u6b63\u5f0f\u6388\u6743\u9700\u63a5\u5165\u652f\u4ed8\u5b9d SDK\uff0c\u8bf7\u5148\u4f7f\u7528\u6a21\u62df\u767b\u5f55\u6216\u914d\u7f6e\u540e\u6269\u5c55",
    "\u5fae\u4fe1\u6388\u6743\u5931\u8d25\uff1a",
    "\u7ed1\u5b9a\u7528\u6237\u4e0d\u5b58\u5728",
    "\u5fae\u4fe1\u7528\u6237",
    "\u652f\u4ed8\u5b9d\u7528\u6237",
]

def fix_java():
    path = os.path.join(ROOT, 'ruoyi-framework', 'src', 'main', 'java',
        'com', 'ruoyi', 'framework', 'web', 'service', 'SocialLoginService.java')
    with open(path, 'rb') as f:
        raw = f.read()
    try:
        t = raw.decode('utf-8')
    except UnicodeDecodeError:
        t = raw.decode('gbk', errors='replace')

    import re
    throws = list(re.finditer(r'throw new ServiceException\("([^"]*)"', t))
    idx = 0
    for m in throws:
        if idx >= len(MSGS) - 2:
            break
        if idx == 8:
            old = m.group(0)
            new = 'throw new ServiceException("' + MSGS[8] + '" + json.getString("errmsg")'
            t = t.replace(old + '");', new + ');', 1)
            idx += 1
            continue
        old = m.group(1)
        if old and ord(old[0]) < 128 and 'application.yml' not in old:
            idx += 1
            continue
        t = t.replace('throw new ServiceException("' + old + '")',
                      'throw new ServiceException("' + MSGS[idx] + '")', 1)
        idx += 1

    t = t.replace('? "??" : "??"',
                  '? "' + MSGS[10] + '" : "' + MSGS[11] + '"')
    t = t.replace('nickname = "??"',
                  'nickname = "' + MSGS[10] + '"')

    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(t)
    print('fixed java, throws patched:', idx)

def write_sql():
    p = os.path.join(ROOT, 'sql', 'mall_social.sql')
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(SQL)
    print('wrote mall_social.sql')

write_sql()
fix_java()
