const express = require('express');
const { Pool } = require('pg');
const trace = require('@opentelemetry/api').trace;

const app = express();
const PORT = process.env.PORT || 4002;

const pool = new Pool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'app_user',
  password: process.env.DB_PASSWORD || 'app_password',
  database: process.env.DB_NAME || 'orders_db',
  port: 5432,
});

app.get('/process-order', async (req, res) => {
  console.log('traceparent:', req.headers['traceparent']);
  const tracer = trace.getTracer('service-b-custom-tracer');

  // Ловушка №1: Искусственная плавающая задержка (Bottleneck)
  const isSlow = Math.random() < 0.4; // 40% запросов с задержкой 2 секунды
  const delay = isSlow ? 2000 : 100;

  // Ловушка №2: Искусственный сбой (500 Error)
  const isError = false;

  await new Promise((resolve) => setTimeout(resolve, delay));

  if (isError) {
    const activeSpan = trace.getActiveSpan();
    if (activeSpan) {
      activeSpan.setStatus({ code: 2, message: 'Payment gateway timeout error' }); // ERROR status
      activeSpan.recordException(new Error('Payment gateway timeout error'));
    }
    return res.status(500).send('Order processing failed: Internal Payment Error');
  }

  try {
    // Внутренний кастомный Span для детализации тяжелой операции
    await tracer.startActiveSpan('db-heavy-query', async (span) => {
      const dbResult = await pool.query('SELECT * FROM orders ORDER BY created_at DESC LIMIT 5');
      span.setAttribute('db.rows_count', dbResult.rowCount);
      span.end();
      
      res.json({
        status: 'SUCCESS',
        delay_applied_ms: delay,
        orders: dbResult.rows,
      });
    });
  } catch (err) {
    res.status(500).send(`DB Error: ${err.message}`);
  }
});

app.listen(PORT, () => {
  console.log(`Service B listening on port ${PORT}`);
});