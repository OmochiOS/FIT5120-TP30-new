const fs = require('fs');
const http = require('http');
const path = require('path');
const { URL } = require('url');

const args = process.argv.slice(2);

function getArg(name, fallback) {
  const index = args.indexOf(name);
  if (index === -1 || index === args.length - 1) {
    return fallback;
  }
  return args[index + 1];
}

const host = getArg('--host', '127.0.0.1');
const port = Number(getArg('--port', '4174'));
const backendHost = getArg('--backend-host', '127.0.0.1');
const backendPort = Number(getArg('--backend-port', '8080'));
const distDir = path.resolve(__dirname, '..', 'dist');

const mimeTypes = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.svg': 'image/svg+xml',
  '.txt': 'text/plain; charset=utf-8',
  '.webp': 'image/webp'
};

function send(res, status, body, headers = {}) {
  res.writeHead(status, headers);
  res.end(body);
}

function serveStatic(req, res) {
  const url = new URL(req.url, `http://${req.headers.host}`);
  let filePath = path.join(distDir, url.pathname);
  if (url.pathname === '/' || !fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    filePath = path.join(distDir, 'index.html');
  }

  if (!filePath.startsWith(distDir)) {
    send(res, 403, 'Forbidden');
    return;
  }

  fs.readFile(filePath, (error, data) => {
    if (error) {
      send(res, 404, 'Not Found');
      return;
    }
    const ext = path.extname(filePath).toLowerCase();
    send(res, 200, data, {
      'Content-Length': Buffer.byteLength(data),
      'Content-Type': mimeTypes[ext] || 'application/octet-stream'
    });
  });
}

function proxyApi(req, res) {
  const options = {
    host: backendHost,
    port: backendPort,
    path: req.url,
    method: req.method,
    headers: {
      ...req.headers,
      host: `${backendHost}:${backendPort}`,
      connection: 'close'
    }
  };

  const proxyReq = http.request(options, (proxyRes) => {
    const chunks = [];
    proxyRes.on('data', (chunk) => chunks.push(chunk));
    proxyRes.on('end', () => {
      const body = Buffer.concat(chunks);
      const headers = { ...proxyRes.headers };
      delete headers['transfer-encoding'];
      headers['content-length'] = Buffer.byteLength(body);
      res.writeHead(proxyRes.statusCode || 502, headers);
      res.end(body);
    });
  });

  proxyReq.on('error', (error) => {
    const body = Buffer.from(JSON.stringify({ error: 'backend unavailable', detail: error.message }));
    res.writeHead(502, {
      'Content-Length': Buffer.byteLength(body),
      'Content-Type': 'application/json; charset=utf-8'
    });
    res.end(body);
  });

  req.on('data', (chunk) => proxyReq.write(chunk));
  req.on('end', () => proxyReq.end());
}

const server = http.createServer((req, res) => {
  if (req.url.startsWith('/api/')) {
    proxyApi(req, res);
    return;
  }
  serveStatic(req, res);
});

server.listen(port, host);
