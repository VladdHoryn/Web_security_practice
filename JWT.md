# Lab 8 — JWT авторизація: перевірка доступу в Postman

Колекція: [`postman/Library-JWT.postman_collection.json`](postman/Library-JWT.postman_collection.json)
(Postman → **Import**; Talend API Tester → **Import → Postman collection**).

## Як працює

1. `POST /api/auth/login` з `{ "username", "password" }` → сервер перевіряє користувача в таблиці `accounts`
   (пароль — BCrypt-хеш) і повертає JWT (HS256, 60 хв) з claim `role`.
2. Подальші запити — із заголовком `Authorization: Bearer <token>`.
3. Без токена / з недійсним токеном → **401**, з токеном без потрібної ролі → **403**.

| Логін       | Пароль          | Роль      | Доступ                                                       |
|-------------|-----------------|-----------|--------------------------------------------------------------|
| `user`      | `User123!`      | USER      | перегляд книг, створення бронювання                          |
| `librarian` | `Librarian123!` | LIBRARIAN | USER + додавання книг, статус, керування бронюваннями         |
| `admin`     | `Adminamdin123!`| ADMIN     | повний доступ (видалення книг, керування користувачами)      |

## Скріншоти

### 1. Логін ADMIN → 200, отримано JWT
![img.png](img.png)

### 2. Логін з неправильним паролем → 401
![img_1.png](img_1.png)

### 3. GET /api/books без токена → 401
![img_2.png](img_2.png)

### 4. USER: GET /api/books → 200
![img_3.png](img_3.png)

### 5. USER: POST /api/books → 403
![img_5.png](img_5.png)

### 6. LIBRARIAN: POST /api/books → 201
![img_6.png](img_6.png)

### 7. LIBRARIAN: DELETE /api/books/2 → 403
![img_7.png](img_7.png)

### 8. LIBRARIAN: GET /api/users → 403
![img_8.png](img_8.png)

### 9. ADMIN: GET /api/users → 200
![img_9.png](img_9.png)

### 10. ADMIN: DELETE /api/books/2 → 204
![img_10.png](img_10.png)
