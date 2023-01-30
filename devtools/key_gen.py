#!/usr/bin/env/python3

"""
A sloppy script for generating key code source files
"""

keys = """ArrowUp
ArrowDown
ArrowLeft
ArrowRight
ShiftLeft
ShiftRight
ControlLeft
ControlRight
AltLeft
AltRight
OSLeft/MetaLeft
OSRight/MetaRight
Period .
Comma ,
Quote '
Semicolon ;
Home
End
PageUp
PageDown
Insert
Delete 7f
Minus -
Equal =
Backquote `
BracketLeft [
BracketRight ]
Slash /
Backslash \\
Tab 09
Space 20
Backspace 08
Enter 0a
NumpadEnter
NumpadSubtract
NumpadAdd
NumpadMultiply
NumpadDivide
NumpadDecimal
""".strip().split("\n")

import string
for k in string.ascii_uppercase:
  keys.append(f"Key{k} {k}")
for k in string.digits:
  keys.append(f"Digit{k} {k}")
  keys.append(f"Numpad{k}")
for k in range(1, 13):
  keys.append(f"F{k}")

nextcode = 128
c_to_ns = {}
n_to_c = {}
def addkey (n,c):
  n_to_c[n] = c
  if c not in c_to_ns:
    c_to_ns[c] = []
  assert n not in c_to_ns[c]
  c_to_ns[c].append(n)

for n in keys:
  if " " in n:
    n,c = n.split()
    if len(c) == 2:
      c = int(c, 16)
    else:
      c = ord(c)
      assert c < 256
  else:
    c = nextcode
    nextcode += 1
  for nn in n.split("/"):
    addkey(nn,c)

print("""package GaFr;

public enum GFKey
{
  public final int code;
  private GFKey (int code) { this.code = code; }
  public static final GFKey[] codeMap;
  static
  {
    codeMap = new GFKey[255];
    for (int i = 0; i < codeMap.length; ++i)
      codeMap[i] = Invalid;
    for (GFKey k : GFKey.values())
      codeMap[k.code] = k;
  }
""")

for i,(name,code) in reversed(list(enumerate(reversed(sorted(n_to_c.items()))))):
  if name.startswith("Key"): name = name[3:]
  #print(f"  {name}({code})" + (";" if i == 0 else ","))
  print(f"  {name}({code}),")
print("  Invalid({max(n_to_c.values())+1});")
print("}")
print()

print()
print("""package GaFr;

public class GFKey
{""")
for name,code in sorted(n_to_c.items()):
  if name.startswith("Key"): name = name[3:]
  print(f"  public static final int {name} = {code};")
print("}")
print()


print()
print("/** Convert a JavaScript key code to a GaFr key code */")
print("function gafr_keyConvert (code)")
print("{")
print("  switch (code)")
print("  {")
for code,names in c_to_ns.items():
  for n in names:
    print(f'    case "{n}":')
  print(f"      return {code};")
print("  }")
print("  return 0;")
print("}")
