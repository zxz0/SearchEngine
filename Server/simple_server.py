#! /usr/local/bin/python3
# Zixuan Zhang
# 2017/04/05

# simple_server:
# simple HTTP server can handle request and run CGI scripts using 8080 port
# will run forever if not stopped
# run: python3 simple_server.py | stop: control + z
# find: sudo lsof -i:8080 | kill: kill pid

from http.server import HTTPServer, CGIHTTPRequestHandler

port = 8080

http_server = HTTPServer(('', port), CGIHTTPRequestHandler)
print("Starting simple_server on: http://localhost:" + str(http_server.server_port) + '/')
http_server.serve_forever()
