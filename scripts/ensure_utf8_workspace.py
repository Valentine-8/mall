# -*- coding: utf-8 -*-
"""
Scan mall workspace and convert non-UTF-8 text files (usually GBK on Windows) to UTF-8.

Usage:
  python scripts/ensure_utf8_workspace.py          # fix
  python scripts/ensure_utf8_workspace.py --check  # exit 1 if issues remain
"""
from __future__ import annotations

import argparse
import os
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))

TEXT_EXT = {
    '.java', '.vue', '.js', '.ts', '.jsx', '.tsx', '.mjs', '.cjs',
    '.xml', '.yml', '.yaml', '.properties', '.sql', '.md', '.markdown',
    '.scss', '.css', '.less', '.html', '.htm', '.json', '.txt', '.py',
    '.sh', '.bat', '.cmd', '.ini', '.conf', '.editorconfig', '.gitattributes',
}

SKIP_DIRS = {
    'node_modules', 'target', 'dist', '.git', '.idea', '.vscode',
    'build', '.cursor', 'coverage', '.nuxt', '.output',
}

SKIP_FILES = {
    'package-lock.json', 'yarn.lock', 'pnpm-lock.yaml',
}


def has_cjk(text: str) -> bool:
    return any('\u4e00' <= c <= '\u9fff' for c in text)


def looks_mojibake_utf8(text: str) -> bool:
    """UTF-8 decoded text that is likely GBK bytes misread as UTF-8."""
    if '\ufffd' in text:
        return True
    # Common GBK-as-UTF-8 fragments for Chinese UI
    bad_fragments = (
        '\u00c9\u00cc', '\u00b3\u00c7', '\u00b5\u00bd', '\u00d6\u00b8',
        '\u00c3\u00e8', '\u00b1\u00b8', '\u00d3\u00f2', '\u00bc\u00b0',
        '\u00bc\u00bc', '\u00b5\u00c7', '\u00c2\u00bc', '\u00b9\u00ba',
    )
    return any(f in text for f in bad_fragments)


def try_decode_gbk(raw: bytes) -> str | None:
    for enc in ('gbk', 'gb18030', 'cp936'):
        try:
            text = raw.decode(enc)
            if has_cjk(text):
                return text
        except UnicodeDecodeError:
            continue
    return None


def analyze_file(path: str) -> str | None:
    """Return 'convert' if should rewrite as UTF-8, else None."""
    with open(path, 'rb') as f:
        raw = f.read()
    if not raw:
        return None
    if raw.startswith(b'\xef\xbb\xbf'):
        raw = raw[3:]

    utf8_text: str | None = None
    try:
        utf8_text = raw.decode('utf-8')
    except UnicodeDecodeError:
        g = try_decode_gbk(raw)
        return 'convert' if g else 'invalid'

    assert utf8_text is not None
    if looks_mojibake_utf8(utf8_text):
        g = try_decode_gbk(raw)
        if g:
            return 'convert'
    # File is valid UTF-8 but might be GBK on disk that accidentally decodes as utf-8?
    # Heuristic: if no CJK expected in path but has mojibake - already handled
    # Pure ASCII / valid UTF-8 Chinese
    if has_cjk(utf8_text):
        return None
    # No CJK in utf-8 decode; if gbk gives CJK, file is GBK
    g = try_decode_gbk(raw)
    if g and has_cjk(g):
        return 'convert'
    return None


def convert_file(path: str) -> bool:
    with open(path, 'rb') as f:
        raw = f.read()
    if raw.startswith(b'\xef\xbb\xbf'):
        raw = raw[3:]
    text = None
    try:
        t = raw.decode('utf-8')
        if looks_mojibake_utf8(t):
            text = try_decode_gbk(raw)
        else:
            g = try_decode_gbk(raw)
            if g and has_cjk(g) and not has_cjk(t):
                text = g
            else:
                return False
    except UnicodeDecodeError:
        text = try_decode_gbk(raw)
    if not text:
        return False
    text = text.replace('\r\n', '\n').replace('\r', '\n')
    with open(path, 'w', encoding='utf-8', newline='\n') as f:
        f.write(text)
    return True


def iter_files():
    for dirpath, dirnames, filenames in os.walk(ROOT):
        dirnames[:] = [d for d in dirnames if d not in SKIP_DIRS]
        for fn in filenames:
            if fn in SKIP_FILES:
                continue
            ext = os.path.splitext(fn)[1].lower()
            if ext not in TEXT_EXT and fn not in ('.editorconfig', '.gitattributes'):
                continue
            yield os.path.join(dirpath, fn)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--check', action='store_true', help='only check, do not fix')
    args = parser.parse_args()

    need_fix: list[str] = []
    fixed: list[str] = []
    invalid: list[str] = []

    for path in iter_files():
        rel = os.path.relpath(path, ROOT)
        try:
            action = analyze_file(path)
        except OSError as e:
            print('error', rel, e)
            continue
        if action == 'convert':
            need_fix.append(rel)
            if not args.check:
                if convert_file(path):
                    fixed.append(rel)
                else:
                    invalid.append(rel)

    if args.check:
        if need_fix:
            print('NON-UTF-8 or mojibake (run without --check to fix):')
            for r in need_fix:
                print(' ', r)
            sys.exit(1)
        print('OK: all scanned text files are UTF-8')
        sys.exit(0)

    if fixed:
        print('Converted to UTF-8 (%d):' % len(fixed))
        for r in fixed:
            print(' ', r)
    if invalid:
        print('Could not fix (%d):' % len(invalid))
        for r in invalid:
            print(' ', r)
    if not fixed and not need_fix:
        print('No conversion needed')
    elif not fixed and need_fix:
        print('Still need attention:', len(need_fix))


if __name__ == '__main__':
    main()
