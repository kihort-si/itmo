# Запуск
set -a
source .env
set +a
mvn clean package
mvn exec:java

#1. Регистрация пользователя
# Ожидаемый статус: 201 Created

curl -i -X POST http://localhost:7070/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"login":"user","password":"StrongPass123"}'

#2. Вход и получение JWT
#Ожидаемый статус: 200 OK

curl -i -X POST http://localhost:7070/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"user","password":"StrongPass123"}'

#3. Запрос защищённых данных без JWT
#Ожидаемый статус: 401 Unauthorized

curl -i http://localhost:7070/api/data

#4. Запрос защищённых данных с JWT
#Вместо JWT accessToken созданного пользователя.
#Ожидаемый статус: 200 OK

curl -i http://localhost:7070/api/data \
  -H 'Authorization: Bearer JWT'

#5. Проверка поддельного JWT
#Ожидаемый статус: 401 Unauthorized

curl -i http://localhost:7070/api/data \
  -H 'Authorization: Bearer HHHHHHHHHHHHHHHHHHHHHHHHHH'

#6. Проверка неправильного пароля
#Ожидаемый статус: 401 Unauthorized

curl -i -X POST http://localhost:7070/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"defense_user","password":"WrongPassword123"}'

#7. Проверка защиты от SQL-инъекции
#Последовательность \u0027 в JSON преобразуется сервером в символ одинарной кавычки.
#Ожидаемый статус: 401 Unauthorized

curl -i -X POST http://localhost:7070/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"login":"\u0027 OR 1=1 --","password":"WrongPassword123"}'

#8. Проверка защиты от XSS
#Опасный логин отклоняется allowlist-валидацией.
#Ожидаемый статус: 400 Bad Request

curl -i -X POST http://localhost:7070/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"login":"<script>alert(1)</script>","password":"StrongPass123"}'

#9. Проверка повторной регистрации
#Ожидаемый статус: 409 Conflict

curl -i -X POST http://localhost:7070/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"login":"user","password":"StrongPass123"}'

#10. Просмотр сохранённого bcrypt-хэша в SQLite
#Вместо открытого пароля должен отображаться хэш, начинающийся с $2a$12$.

sqlite3 secure-api.db "SELECT login, password_hash FROM users WHERE login = 'user';"

#11. Запуск всех автоматических тестов

mvn clean test


#12. Запуск теста экранирования XSS через OWASP Java Encoder

mvn -Dtest=OutputEncoderTest test
