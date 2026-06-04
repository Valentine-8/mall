# -*- coding: utf-8 -*-
"""Convert mall/shop Vue files saved as GBK to UTF-8 (Windows editor issue)."""
import os

ROOT = os.path.join(os.path.dirname(__file__), '..', 'ruoyi-ui', 'src', 'views')
DIRS = ['mall/category', 'mall/product', 'mall/order', 'shop']


def fix_file(path: str) -> str:
    raw = open(path, 'rb').read()
    try:
        text = raw.decode('utf-8')
        if any('\u4e00' <= c <= '\u9fff' for c in text):
            return 'skip'
    except UnicodeDecodeError:
        pass
    text = raw.decode('gbk')
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(text)
    return 'fixed'


def main():
    for d in DIRS:
        folder = os.path.normpath(os.path.join(ROOT, d))
        for name in os.listdir(folder):
            if name.endswith('.vue'):
                status = fix_file(os.path.join(folder, name))
                print(status, os.path.join(d, name))


if __name__ == '__main__':
    main()
