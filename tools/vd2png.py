"""Rasterise the shipped VectorDrawable to the flat PNG the Play listing needs.

An adaptive icon cannot supply that asset, and redrawing the mark by hand would
let the store icon and the launcher icon drift apart. This reads the SAME
pathData `ic_launcher_foreground.xml` ships, so they cannot.

    python3 tools/vd2png.py app/src/main/res/drawable/ic_launcher_foreground.xml store/play-icon-512.png

Nonzero winding fill, 4x supersampled. The 108dp viewport is cropped to the
72dp area the launcher mask actually reveals before scaling, so the mark carries
the same weight on the store tile as it does on a home screen.

Needs numpy and Pillow. Original docstring follows.

Reads the SAME pathData the adaptive icon uses, so the Play listing icon and the
launcher icon cannot drift. Nonzero winding fill, 4x supersampled.
"""
import re, sys, xml.etree.ElementTree as ET
import numpy as np
from PIL import Image

AND = "{http://schemas.android.com/apk/res/android}"
NUM = re.compile(r'[-+]?(?:\d*\.\d+|\d+)(?:[eE][-+]?\d+)?')

def tokenise(d):
    out, i = [], 0
    for m in re.finditer(r'([MmLlHhVvQqCcZz])([^MmLlHhVvQqCcZz]*)', d):
        cmd, args = m.group(1), [float(x) for x in NUM.findall(m.group(2))]
        out.append((cmd, args))
    return out

def flatten(d, steps=24):
    """-> list of closed subpaths, each a list of (x, y)."""
    subs, cur, start, pos = [], [], (0.0, 0.0), (0.0, 0.0)
    for cmd, a in tokenise(d):
        up = cmd.upper(); rel = cmd.islower()
        k = 0
        while True:
            if up == 'Z':
                if cur:
                    subs.append(cur); cur = []
                pos = start; break
            if up == 'M':
                x, y = a[k], a[k+1]; k += 2
                if rel: x += pos[0]; y += pos[1]
                if cur: subs.append(cur)
                cur = [(x, y)]; pos = (x, y); start = (x, y)
                up = 'L'                      # subsequent pairs are implicit lineto
            elif up == 'L':
                x, y = a[k], a[k+1]; k += 2
                if rel: x += pos[0]; y += pos[1]
                cur.append((x, y)); pos = (x, y)
            elif up == 'H':
                x = a[k]; k += 1
                if rel: x += pos[0]
                cur.append((x, pos[1])); pos = (x, pos[1])
            elif up == 'V':
                y = a[k]; k += 1
                if rel: y += pos[1]
                cur.append((pos[0], y)); pos = (pos[0], y)
            elif up == 'Q':
                x1, y1, x, y = a[k:k+4]; k += 4
                if rel: x1 += pos[0]; y1 += pos[1]; x += pos[0]; y += pos[1]
                x0, y0 = pos
                for s in range(1, steps + 1):
                    t = s / steps; u = 1 - t
                    cur.append((u*u*x0 + 2*u*t*x1 + t*t*x,
                                u*u*y0 + 2*u*t*y1 + t*t*y))
                pos = (x, y)
            else:
                raise SystemExit("unsupported command " + cmd)
            if k >= len(a):
                break
    if cur:
        subs.append(cur)
    return subs

def fill(subs, size, nonzero=True):
    """Scanline fill with nonzero winding. Returns a float mask of `size`."""
    x0 = []; y0 = []; x1 = []; y1 = []
    for sp in subs:
        pts = sp + [sp[0]]
        for i in range(len(pts) - 1):
            ax, ay = pts[i]; bx, by = pts[i+1]
            if ay != by:
                x0.append(ax); y0.append(ay); x1.append(bx); y1.append(by)
    x0 = np.array(x0); y0 = np.array(y0); x1 = np.array(x1); y1 = np.array(y1)
    direction = np.where(y1 > y0, 1, -1)
    out = np.zeros((size, size), dtype=np.uint8)
    for py in range(size):
        yc = py + 0.5
        hit = ((y0 <= yc) & (y1 > yc)) | ((y1 <= yc) & (y0 > yc))
        if not hit.any():
            continue
        t = (yc - y0[hit]) / (y1[hit] - y0[hit])
        xs = x0[hit] + t * (x1[hit] - x0[hit])
        ds = direction[hit]
        order = np.argsort(xs)
        xs = xs[order]; wind = np.cumsum(ds[order])
        inside = wind != 0 if nonzero else (np.arange(len(xs)) % 2 == 0)
        for i in np.nonzero(inside[:-1])[0]:
            a = int(np.ceil(xs[i] - 0.5)); b = int(np.ceil(xs[i+1] - 0.5))
            if b > a:
                out[py, max(a, 0):min(b, size)] = 1
    return out

def main(vector_xml, out_png, px=512, crop=72.0, ss=4):
    root = ET.parse(vector_xml).getroot()
    vw = float(root.get(AND + "viewportWidth"))
    work = px * ss
    # map the launcher's VISIBLE area (the 72dp mask circle) onto the square, so
    # the mark carries the same weight here as it does on a home screen
    scale = work / crop
    off = (vw - crop) / 2.0
    ground = np.zeros((work, work), dtype=np.uint8)
    paths = [p for p in root if p.tag.endswith("path")]
    for p in paths:
        d = p.get(AND + "pathData")
        subs = flatten(d)
        subs = [[((x - off) * scale, (y - off) * scale) for x, y in sp] for sp in subs]
        stroke = p.get(AND + "strokeWidth")
        fillc = p.get(AND + "fillColor") or "#00000000"
        if stroke and fillc.endswith("00000000"):
            w = float(stroke) * scale
            for sp in subs:                      # axis-aligned rect only
                xs = [q[0] for q in sp]; ys = [q[1] for q in sp]
                a, b, c, d2 = min(xs), min(ys), max(xs), max(ys)
                outer = np.zeros_like(ground)
                outer[int(b - w/2):int(d2 + w/2), int(a - w/2):int(c + w/2)] = 1
                outer[int(b + w/2):int(d2 - w/2), int(a + w/2):int(c - w/2)] = 0
                ground |= outer
        else:
            ground |= fill(subs, work)
    img = Image.fromarray((1 - ground) * 255).convert("L").resize((px, px), Image.LANCZOS)
    alpha = np.asarray(img, dtype=np.float32) / 255.0      # 1 = ground, 0 = ink
    ivory = np.array([0xFF, 0xFD, 0xED], dtype=np.float32)
    ink = np.array([0, 0, 0], dtype=np.float32)
    rgb = alpha[..., None] * ivory + (1 - alpha[..., None]) * ink
    Image.fromarray(rgb.astype(np.uint8)).save(out_png)
    print("wrote", out_png, px, "x", px)

if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2])
