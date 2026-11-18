import http.server
import os

state = {"ready": True, "alive": True}
hostname = os.environ.get("HOSTNAME", "unknown")

class Handler(http.server.BaseHTTPRequestHandler):
    def do_GET(self):
        path = self.path

        if path == "/hostname":
            status, body = 200, hostname + "\n"
        elif path == "/readyz":
            status = 200 if state["ready"] else 503
            body = "READY" if state["ready"] else "NOT READY"
        elif path == "/healthz":
            status = 200 if state["alive"] else 500
            body = "ALIVE" if state["alive"] else "NOT ALIVE"
        elif path == "/make-not-ready":
            state["ready"] = False
            status, body = 200, "Readiness disabled"
        elif path == "/make-ready":
            state["ready"] = True
            status, body = 200, "Readiness restored"
        elif path == "/make-not-alive":
            state["alive"] = False
            state["ready"] = False
            status, body = 200, "Liveness disabled"
        else:
            status, body = 404, "Not Found"

        self.send_response(status)
        self.end_headers()
        self.wfile.write(body.encode())

http.server.HTTPServer(("0.0.0.0", 8080), Handler).serve_forever()
