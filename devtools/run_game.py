#!/usr/bin/env python3

import webbrowser
from http.server import SimpleHTTPRequestHandler, HTTPServer
import os
import errno

class Handler (SimpleHTTPRequestHandler):
  #def log_error (self, *args):
  #  pass
  def log_request (self, *args):
    pass


if __name__ == '__main__':
  os.chdir(os.path.dirname(os.path.realpath(__file__)))

  addr = "127.0.0.1"
  port = 63000

  while True:
    try:
      server = HTTPServer((addr,port), Handler)
      break
    except OSError as e:
      if e.errno != errno.EADDRINUSE:
        raise
      port += 1


  url = f"http://{addr}:{port}"
  print("Running web server at " + url)
  print("You should be able to close this window when loading is complete.")
  webbrowser.open(url, 1)

  try:
    server.serve_forever()
  except KeyboardInterrupt:
    pass
