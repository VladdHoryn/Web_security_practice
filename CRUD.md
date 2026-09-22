Отримати всі книги
Method: GET
URL: http://localhost:8081/api/books

Отримати доступні книги
Method: GET
URL: http://localhost:8081/api/books/available

Знайти книгу за ID
Method: GET
URL: http://localhost:8081/api/books/1

Додати книгу
Method: POST
URL: http://localhost:8081/api/books
Headers: Content-Type: application/json
Body:
{
"title": "Хіба ревуть воли, як ясла повні?",
"author": "Панас Мирний"
}

Змінити статус книги
Method: PATCH
URL: http://localhost:8081/api/books/1/status?reserved=true

Видалити книгу
Method: DELETE
URL: http://localhost:8081/api/books/1

Отримати всіх користувачів
Method: GET
URL: http://localhost:8081/api/users

Знайти користувача за ID
Method: GET
URL: http://localhost:8081/api/users/1

Знайти користувача за email
Method: GET
URL: http://localhost:8081/api/users/search?email=oleksandr.petренко@example.com

Додати користувача
Method: POST
URL: http://localhost:8081/api/users
Headers: Content-Type: application/json
Body:
{
"name": "Степан Бандера",
"email": "stepan.b@example.com"
}

Оновити користувача
Method: PUT
URL: http://localhost:8081/api/users/1
Headers: Content-Type: application/json
Body:
{
"name": "Олександр Петренко",
"email": "oleksandr.new@example.com"
}

Видалити користувача
Method: DELETE
URL: http://localhost:8081/api/users/1

Отримати всі бронювання
Method: GET
URL: http://localhost:8081/api/reservations

Знайти бронювання за ID
Method: GET
URL: http://localhost:8081/api/reservations/1

Отримати бронювання користувача
Method: GET
URL: http://localhost:8081/api/reservations/user/1

Створити бронювання
Method: POST
URL: http://localhost:8081/api/reservations?bookId=1&userId=2