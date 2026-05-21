-- Default email templates (Mustache syntax: {{variable}})
-- Dollar-quoting avoids escaping issues with HTML and Mustache braces.

INSERT INTO rep.template (code, name, channel, subject, body) VALUES (
    'REGISTRATION_SUCCESS',
    'Регистрация: успешное создание аккаунта',
    'EMAIL',
    'Добро пожаловать в MyBroker, {{username}}!',
    $tmpl$<!DOCTYPE html>
<html lang="ru">
<body style="font-family:Arial,sans-serif;color:#222;max-width:600px;margin:0 auto">
  <h1 style="color:#1a73e8">Добро пожаловать в MyBroker!</h1>
  <p>Привет, <strong>{{username}}</strong>!</p>
  <p>Ваш аккаунт успешно создан.</p>
  <ul>
    <li>Email: <strong>{{email}}</strong></li>
    <li>Базовая валюта: <strong>{{baseCurrency}}</strong></li>
  </ul>
  <p>Теперь вы можете войти в приложение и начать торговать.</p>
  <p style="color:#888;font-size:12px">MyBroker — учебная платформа для торговли ценными бумагами.</p>
</body>
</html>$tmpl$
);

INSERT INTO rep.template (code, name, channel, subject, body) VALUES (
    'REGISTRATION_FAILURE',
    'Регистрация: ошибка создания аккаунта',
    'EMAIL',
    'Ошибка создания аккаунта MyBroker',
    $tmpl$<!DOCTYPE html>
<html lang="ru">
<body style="font-family:Arial,sans-serif;color:#222;max-width:600px;margin:0 auto">
  <h1 style="color:#d93025">Не удалось создать аккаунт</h1>
  <p>Привет, <strong>{{username}}</strong>!</p>
  <p>К сожалению, при создании вашего аккаунта произошла ошибка.</p>
  {{#reason}}
  <p><strong>Причина:</strong> {{reason}}</p>
  {{/reason}}
  <p>Пожалуйста, попробуйте зарегистрироваться ещё раз.</p>
  <p style="color:#888;font-size:12px">Если проблема повторяется, обратитесь в поддержку.</p>
</body>
</html>$tmpl$
);

INSERT INTO rep.template (code, name, channel, subject, body) VALUES (
    'TRADE_EXECUTED',
    'Уведомление об исполнении сделки (email)',
    'EMAIL',
    'Сделка исполнена: {{side}} {{quantity}} {{ticker}} @ {{price}}',
    $tmpl$<!DOCTYPE html>
<html lang="ru">
<body style="font-family:Arial,sans-serif;color:#222;max-width:600px;margin:0 auto">
  <h1 style="color:#1a73e8">Сделка исполнена</h1>
  <table style="border-collapse:collapse;width:100%">
    <tr><td style="padding:6px;border:1px solid #ddd;font-weight:bold">Тикер</td>
        <td style="padding:6px;border:1px solid #ddd">{{ticker}}</td></tr>
    <tr><td style="padding:6px;border:1px solid #ddd;font-weight:bold">Тип операции</td>
        <td style="padding:6px;border:1px solid #ddd">{{side}}</td></tr>
    <tr><td style="padding:6px;border:1px solid #ddd;font-weight:bold">Количество</td>
        <td style="padding:6px;border:1px solid #ddd">{{quantity}}</td></tr>
    <tr><td style="padding:6px;border:1px solid #ddd;font-weight:bold">Цена исполнения</td>
        <td style="padding:6px;border:1px solid #ddd">{{price}}</td></tr>
    <tr><td style="padding:6px;border:1px solid #ddd;font-weight:bold">Дата и время</td>
        <td style="padding:6px;border:1px solid #ddd">{{executedAt}}</td></tr>
    <tr><td style="padding:6px;border:1px solid #ddd;font-weight:bold">ID сделки</td>
        <td style="padding:6px;border:1px solid #ddd">{{tradeId}}</td></tr>
  </table>
  <p style="color:#888;font-size:12px;margin-top:16px">MyBroker — учебная платформа для торговли ценными бумагами.</p>
</body>
</html>$tmpl$
);

INSERT INTO rep.template (code, name, channel, subject, body) VALUES (
    'TRADE_EXECUTED_PUSH',
    'Уведомление об исполнении сделки (push)',
    'PUSH',
    NULL,
    $tmpl${{side}} {{quantity}} {{ticker}} @ {{price}} — сделка исполнена$tmpl$
);

INSERT INTO rep.template (code, name, channel, subject, body) VALUES (
    'EOD_REPORT',
    'Ежедневный отчёт по портфелю',
    'EMAIL',
    'Ежедневный отчёт по портфелю за {{date}}',
    $tmpl$<!DOCTYPE html>
<html lang="ru">
<body style="font-family:Arial,sans-serif;color:#222;max-width:600px;margin:0 auto">
  <h1 style="color:#1a73e8">Ежедневный отчёт</h1>
  <p>Дата: <strong>{{date}}</strong></p>
  <p>За прошедший день в вашем портфеле произошли изменения.</p>
  {{#portfolioValue}}
  <p>Текущая стоимость портфеля: <strong>{{portfolioValue}}</strong></p>
  {{/portfolioValue}}
  {{#changeAmount}}
  <p>Изменение за день: <strong>{{changeAmount}}</strong> ({{changePct}}%)</p>
  {{/changeAmount}}
  <p style="color:#888;font-size:12px;margin-top:16px">MyBroker — учебная платформа для торговли ценными бумагами.</p>
</body>
</html>$tmpl$
);
