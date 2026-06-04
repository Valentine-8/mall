# -*- coding: utf-8 -*-
"""Fix shop vue encoding: rewrite product via write_shop_part2, convert GBK, re-apply login patches."""
import os
import subprocess
import sys

ROOT = os.path.dirname(__file__)
VIEWS = os.path.join(ROOT, '..', 'ruoyi-ui', 'src', 'views', 'shop')


def gbk_to_utf8():
    for name in os.listdir(VIEWS):
        if not name.endswith('.vue'):
            continue
        p = os.path.join(VIEWS, name)
        raw = open(p, 'rb').read()
        try:
            raw.decode('utf-8')
            continue
        except UnicodeDecodeError:
            pass
        try:
            text = raw.decode('gbk')
            open(p, 'w', encoding='utf-8', newline='\n').write(text)
            print('gbk->utf8', name)
        except Exception as e:
            print('skip', name, e)


def main():
    subprocess.check_call([sys.executable, os.path.join(ROOT, 'write_shop_auth.py')])
    subprocess.check_call([sys.executable, os.path.join(ROOT, 'write_shop_part2.py')])
    subprocess.check_call([sys.executable, os.path.join(ROOT, 'patch_shop_user.py')])
    subprocess.check_call([sys.executable, os.path.join(ROOT, 'patch_shop_login_prompt.py')])
    gbk_to_utf8()
    # verify product
    t = open(os.path.join(VIEWS, 'product.vue'), encoding='utf-8').read()
    assert '\u52a0\u5165\u8d2d\u7269\u8f66' in t, 'product missing Chinese'
    assert 'ensureShopLogin' in t, 'product missing ensureShopLogin'
    print('all shop utf8 ok')


if __name__ == '__main__':
    main()
