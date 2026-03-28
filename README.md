# Recipe-Manager

http://localhost:8080/swagger-ui/index.html - Swagger
http://localhost:3000/ - Frontend
http://localhost:8080/actuator/prometheus - Метрики
Запуск тестов:
.\gradlew.bat test
Отчёт в: 
backend\build\reports\tests\test\index.html

Запуск приложения в корне проекта:
docker-compose up --build

## Стек технологий

| Технология | Роль в проекте |
|------------|----------------|
| **Java 17**, **Spring Boot 3.2** | Основа приложения: веб-слой, DI, автоконфигурация |
| **Spring Web** | HTTP API (`/api/...`) |
| **Spring Security** + **JWT** | Аутентификация и защита эндпоинтов (кроме явно открытых: auth, Swagger, часть Actuator) |
| **PostgreSQL** | БД; пользователи, рецепты, ингредиенты, связь рецепт–ингредиент |
| **Flyway** | Версионирование схемы: SQL-миграции в `backend/src/main/resources/db/migration` применяются при старте |
| **jOOQ** | Доступ к БД и типобезопасные запросы; код генерируется при сборке из тех же миграций расхождение схемы и кода ломает сборку |
| **Spring Validation** | Проверка DTO (`@Valid`, ограничения на поля) |
| **SpringDoc OpenAPI** | Swagger UI и описание API |
| **Spring Boot Actuator** + **Micrometer Prometheus** | Метрики и эндпоинт `/actuator/prometheus` |
| **SLF4J / Logback** (по умолчанию в Boot) | Логирование; уровни для пакетов задаются в `application.yml` |
| **JUnit 5**, **Mockito**, **Spring Test**, **MockMvc** | Модульные и интеграционные тесты |
| **Gradle** | Сборка, зависимости, запуск тестов |
| **Docker**, **Docker Compose** | Контейнеры БД, бэкенда и фронта |
| **Фронтенд** | Статические HTML/CSS/JS + **nginx** (в Compose): список рецептов, CRUD через API |
