#!/usr/bin/env python3

import gzip

def fopen (fn):
  if fn.endswith(".gz"):
    return gzip.open(fn, 'rb')
  return open(fn, 'rb')


def readmagic (f):
  m1,m2 = f.read(2)
  if m1 == 0x36 and m2 == 0x04: return 1
  if m1 == 0x72 and m2 == 0xb5:
    m1,m2 = f.read(2)
    if m1 == 0x4a and m2 == 0x86:
      return 2
  return None

HB = 1
LB = 0

def read_v1_unicode_table (f):
  tab = {}
  g = -1
  while True:
    g += 1
    uc = f.read(2)
    if not uc: return tab
    uc = uc[HB] << 8 | uc[LB]
    if uc != 0xfffe:
      #print("%02x " % (uc,), end="")
      tab[uc] = g
    while True:
      uc = f.read(2)
      if not uc: return tab
      uc = uc[HB] << 8 | uc[LB]
      if uc == 0xffff: break

def read_v2_unicode_table (f):
  # Based on https://wiki.osdev.org/PC_Screen_Font
  tab = {}
  g = 0
  s = 0
  data = f.read()
  while s < len(data):
    uc = data[s]
    if uc == 0xff:
      g += 1
      s += 1
      continue
    if uc & 128:
      if (uc & 32) == 0:
        uc = ((data[s] & 0x1F)<<6)+(data[s+1] & 0x3F)
        s += 1
      elif (uc & 16) == 0:
        uc = ((((data[s+0] & 0xF)<<6)+(data[s+1] & 0x3F))<<6)+(data[s+2] & 0x3F)
        s += 2
      elif (uc & 8) == 0:
        uc = ((((((data[s+0] & 0x7)<<6)+(data[s+1] & 0x3F))<<6)+(data[s+2] & 0x3F))<<6)+(data[s+3] & 0x3F)
        s += 3
      else:
        uc = 0
    tab[uc] = g
    s += 1
  return tab

def readint (f):
  o = 0
  vv = f.read(4)
  for v in reversed(vv):
    o <<= 8
    o |= v
  return o


def convert (f, basename, lpad=0, rpad=0,tpad=0, bpad=0,
             bgcolor=0x00000000, fgcolor=0xffffffff, outlinecolor=None, sh=None,
             xspc=None, yspc=None):
  version = readmagic(f)
  if version is None:
    raise RuntimeError("Bad version")

  if version == 1:
    mode,charsize = f.read(2)
    w = 8
    h = charsize
    num_chars = 256
    if mode & 1: num_chars = 512
    raw = f.read(h * num_chars)
    assert len(raw) == h * num_chars
    if mode & 2:
      tab = read_v1_unicode_table(f)
    else:
      tab = None#dict([(x,x) for x in range(0,256)])
    #print(tab)
  elif version == 2:
    vers2 = readint(f)
    headersize = readint(f)
    flags = readint(f)
    num_chars = readint(f)
    charsize = readint(f)
    h = readint(f)
    w = readint(f)
    f.seek(headersize)
    raw = f.read(charsize * num_chars)
    if flags & 1:
      tab = read_v2_unicode_table(f)
    else:
      tab = None#dict([(x,x) for x in range(0,256)])

  ow = (w+lpad+rpad)*num_chars # output width
  oh = h+tpad+bpad

  data = [bgcolor] * (ow * oh)
  from array import array
  data = array("I", data)

  for i in range(num_chars):
    off = charsize * i
    xoff = (w+lpad+rpad) * i + lpad
    for y in range(h):
      for x in range(w):
        bbit = 1<<(7 - (x % 8))
        bbyte = x // 8
        d = raw[off + y * charsize // h + bbyte]
        if d & bbit:
          data[xoff + x + (y+tpad)*ow] = fgcolor

  def samp (xx,yy):
    if xx < 0 or xx >= ow: return bgcolor
    if yy < 0 or yy >= oh: return bgcolor
    return data[xx+yy*ow]

  def maybe_outline (x, y):
    #data[x+y*ow] = 0xff00ff00
    for yy in range(y-1,y+2):
      for xx in range(x-1,x+2):
        if samp(xx,yy) == fgcolor:
          data[x+y*ow] = outlinecolor
          return

  if outlinecolor is not None:
    for y in range(oh):
      for x in range(ow):
        if samp(x,y) == bgcolor:
          maybe_outline(x,y)

  if sh is not None:
    for y in reversed(range(oh)):
      for x in range(ow):
        if samp(x,y) == bgcolor:
          if samp(x-1,y-1) != bgcolor:
            data[x+y*ow] = sh

  from PIL import Image
  im = Image.frombytes("RGBA", (ow,oh), data.tobytes(), 'raw')
  im.save(basename + ".ffont.png")
  import json
  if xspc is None: xspc = w+lpad
  if yspc is None: yspc = h+tpad
  o = dict( x_spacing=w+lpad, y_spacing=h+tpad, charmap=list(tab.items()),
            pad=[lpad,rpad,tpad,bpad], count=num_chars,
            width=w, height=h)
  with open(basename + ".ffont.json", "w") as of:
    of.write(json.dumps(o, indent=2))


import argparse
import os
import sys

p = argparse.ArgumentParser(prog=sys.argv[0])
p.add_argument("filename", nargs='+')
p.add_argument("-d", "--out-dir")
p.add_argument("-l", "--pad-left", type=int)
p.add_argument("-r", "--pad-right", type=int)
p.add_argument("-t", "--pad-top", type=int)
p.add_argument("-b", "--pad-bottom", type=int)
p.add_argument("-p", "--pad", type=int, default=None)
p.add_argument("-bg", "--background")
p.add_argument("-ol", "--outline")
p.add_argument("-sh", "--shadow")
p.add_argument("-fg", "--foreground")
p.add_argument("-xs", "--x-space", default=None)
p.add_argument("-ys", "--y-space", default=None)

args = p.parse_args()

lpad = rpad = bpad = tpad = 0

if args.pad is not None:
  lpad = rpad = tpad = bpad = args.pad

if args.pad_left is not None: lpad = args.pad_left
if args.pad_right is not None: lpad = args.pad_right
if args.pad_top is not None: lpad = args.pad_top
if args.pad_bottom is not None: lpad = args.pad_bottom

bg=0x00000000
fg=0xffffffff
ol=None #0xff000000
sh=None

if args.background: bg = int(args.background, 0)
if args.foreground: fg = int(args.foreground, 0)
if args.outline:    ol = int(args.outline,    0)
if args.shadow:     sh = int(args.shadow,     0)

for fn in args.filename:
  bname = fn.split(".")
  while bname:
    l = bname[-1]
    if "/" in l: break
    if l == "gz": del bname[-1]
    elif l == "psf": del bname[-1]
    elif l == "psfu": del bname[-1]
    else: break
  bname = ".".join(bname)

  if args.out_dir:
    bname = os.path.basename(bname)
    bname = os.path.join(args.out_dir, bname)

  convert(fopen(fn), bname,
          lpad=lpad, rpad=rpad, tpad=tpad, bpad=bpad,
          fgcolor=fg, bgcolor=bg, outlinecolor=ol, sh=sh,
          xspc=args.x_space, yspc=args.y_space)
