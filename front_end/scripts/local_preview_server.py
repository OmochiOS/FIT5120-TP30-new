from __future__ import annotations

import argparse
import http.client
import mimetypes
import os
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit


DIST_DIR = Path(__file__).resolve().parent.parent / "dist"


class PreviewHandler(BaseHTTPRequestHandler):
    dist_dir = DIST_DIR
    backend_host = "127.0.0.1"
    backend_port = 8080

    def do_GET(self) -> None:
        if self.path.startswith("/api/"):
            self._proxy()
            return
        self._serve_static()

    def do_POST(self) -> None:
        if self.path.startswith("/api/"):
            self._proxy()
            return
        self.send_error(405)

    def do_PUT(self) -> None:
        if self.path.startswith("/api/"):
            self._proxy()
            return
        self.send_error(405)

    def do_DELETE(self) -> None:
        if self.path.startswith("/api/"):
            self._proxy()
            return
        self.send_error(405)

    def do_OPTIONS(self) -> None:
        if self.path.startswith("/api/"):
            self._proxy()
            return
        self.send_response(204)
        self.end_headers()

    def _proxy(self) -> None:
        parsed = urlsplit(self.path)
        target_path = parsed.path
        if parsed.query:
            target_path = f"{target_path}?{parsed.query}"

        body = None
        content_length = self.headers.get("Content-Length")
        if content_length:
            body = self.rfile.read(int(content_length))

        headers = {
            key: value
            for key, value in self.headers.items()
            if key.lower() not in {"host", "connection", "content-length"}
        }

        connection = http.client.HTTPConnection(self.backend_host, self.backend_port, timeout=60)
        try:
            connection.request(self.command, target_path, body=body, headers=headers)
            response = connection.getresponse()
            payload = response.read()

            self.send_response(response.status)
            for key, value in response.getheaders():
                lower = key.lower()
                if lower in {"transfer-encoding", "connection", "content-encoding"}:
                    continue
                self.send_header(key, value)
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            if payload:
                self.wfile.write(payload)
        except OSError as exc:
            message = f'{{"error":"backend unavailable","detail":"{str(exc).replace(chr(34), chr(39))}"}}'
            payload = message.encode("utf-8")
            self.send_response(502)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(payload)))
            self.end_headers()
            self.wfile.write(payload)
        finally:
            connection.close()

    def _serve_static(self) -> None:
        relative_path = urlsplit(self.path).path.lstrip("/")
        candidate = (self.dist_dir / relative_path).resolve() if relative_path else self.dist_dir / "index.html"

        if not str(candidate).startswith(str(self.dist_dir)) or not candidate.exists() or candidate.is_dir():
            candidate = self.dist_dir / "index.html"

        content_type = mimetypes.guess_type(candidate.name)[0] or "application/octet-stream"
        data = candidate.read_bytes()
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, format: str, *args: object) -> None:
        return


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=4174)
    parser.add_argument("--backend-host", default="127.0.0.1")
    parser.add_argument("--backend-port", type=int, default=8080)
    args = parser.parse_args()

    PreviewHandler.backend_host = args.backend_host
    PreviewHandler.backend_port = args.backend_port

    server = ThreadingHTTPServer((args.host, args.port), PreviewHandler)
    try:
        server.serve_forever()
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
