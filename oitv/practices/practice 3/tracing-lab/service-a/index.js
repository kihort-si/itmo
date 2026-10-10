const express = require('express');
const http = require('http');

const app = express();
const PORT = process.env.PORT || 4001;
const SERVICE_B_URL = process.env.SERVICE_B_URL || 'http://localhost:4002';

app.get('/api/checkout', (req, res) => {
  // Имитация небольшой обработки на Gateway
  setTimeout(() => {
    http.get(`${SERVICE_B_URL}/process-order`, (response) => {
      let data = '';
      response.on('data', (chunk) => { data += chunk; });
      response.on('end', () => {
        if (response.statusCode >= 400) {
          res.status(response.statusCode).send(`Service A Error: ${data}`);
        } else {
          res.send(`Service A Success -> ${data}`);
        }
      });
    }).on('error', (err) => {
      res.status(500).send(`Service A failed to reach Service B: ${err.message}`);
    });
  }, 50);
});

app.listen(PORT, () => {
  console.log(`Service A listening on port ${PORT}`);
});