#!/usr/bin/env python3
"""
Minimal auth validator for MCP reverse proxy.

Expects X-API-Key header; returns 200 if it matches MCP_API_KEY env var, 401 otherwise.
Used by Nginx auth_request to protect the MCP endpoint.
"""
import os
from http.server import HTTPServer, BaseHTTPRequestHandler


class ValidatorHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        expected = os.environ.get("MCP_API_KEY", "").strip()
        if not expected:
            self.send_response(500)
            self.send_header("Content-Type", "text/plain")
            self.end_headers()
            self.wfile.write(b"MCP_API_KEY not configured")
            return
        key = self.headers.get("X-API-Key", "").strip()
        if key and key == expected:
            self.send_response(200)
            self.send_header("Content-Type", "text/plain")
            self.end_headers()
            self.wfile.write(b"OK")
        else:
            self.send_response(401)
            self.send_header("Content-Type", "text/plain")
            self.send_header("WWW-Authenticate", 'Bearer realm="MCP"')
            self.end_headers()
            self.wfile.write(b"Unauthorized")

    def log_message(self, format, *args):
        # Reduce noise; optional: log to stderr for debugging
        pass


def main():
    port = int(os.environ.get("VALIDATOR_PORT", "9090"))
    server = HTTPServer(("0.0.0.0", port), ValidatorHandler)
    server.serve_forever()


if __name__ == "__main__":
    main()
