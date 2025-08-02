# 🏦 SimpleBank - Микросервисная банковская система

Микросервисная архитектура банковской системы, построенная на Spring Boot с использованием Docker и Consul для оркестрации.

## 🏗️ Архитектура

Система состоит из 10 микросервисов:

| Сервис | Порт | Описание |
|--------|------|----------|
| **auth-server** | 9000 | Сервер аутентификации и авторизации (OAuth2) |
| **accounts** | 8082 | Управление пользователями и аккаунтами |
| **cash** | 8083 | Управление счетами и балансами |
| **transfer** | 8084 | Переводы между счетами |
| **exchange** | 8086 | Валютные операции |
| **exchange-generator** | 8085 | Генератор курсов валют |
| **blocker** | 8087 | Блокировка подозрительных операций |
| **notifications** | 8088 | Уведомления пользователей |
| **gateway** | 8081 | API Gateway |
| **front-ui** | 8080 | Веб-интерфейс |

## 🛠️ Технологии

- **Spring Boot 3.5.0** - основной фреймворк
- **Spring Security** - безопасность и OAuth2
- **Spring Data JPA** - работа с базой данных
- **PostgreSQL** - основная база данных
- **Consul** - service discovery и конфигурация
- **Docker & Docker Compose** - контейнеризация
- **Maven** - сборка проекта

## 🚀 Запуск проекта

### Вариант 1: Запуск через Maven

#### Предварительные требования
- Java 21
- Maven 3.9+
- PostgreSQL 15
- Consul 1.15+

#### Пошаговый запуск

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/Sadaaaaa/simplebank.git
   cd simplebank
   ```

2. **Запустите PostgreSQL и Consul:**
   ```bash
   # PostgreSQL
   docker run -d --name postgres \
     -e POSTGRES_DB=simplebank_db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:15

   # Consul
   docker run -d --name consul \
     -p 8500:8500 \
     -p 8600:8600/udp \
     consul:1.15.4 agent -dev -client=0.0.0.0 -ui
   ```

3. **Соберите и запустите сервисы:**
   ```bash
   # Сборка всех сервисов
   mvn clean package -DskipTests
   
   # Запуск auth-server
   mvn spring-boot:run -pl auth-server
   
   # Запуск accounts
   mvn spring-boot:run -pl accounts
   
   # Запуск cash
   mvn spring-boot:run -pl cash
   
   # Запуск transfer
   mvn spring-boot:run -pl transfer
   
   # Запуск exchange
   mvn spring-boot:run -pl exchange
   
   # Запуск exchange-generator
   mvn spring-boot:run -pl exchange-generator
   
   # Запуск blocker
   mvn spring-boot:run -pl blocker
   
   # Запуск notifications
   mvn spring-boot:run -pl notifications
   
   # Запуск gateway
   mvn spring-boot:run -pl gateway
   
   # Запуск front-ui
   mvn spring-boot:run -pl front-ui
   ```

### Вариант 2: Запуск через Docker

#### Предварительные требования
- Docker
- Docker Compose

#### Быстрый запуск

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/Sadaaaaa/simplebank.git
   cd simplebank
   ```

2. **Запустите все микросервисы одной командой:**
   ```bash
   # Запуск всех сервисов
   docker-compose up -d
   ```

3. **Проверьте статус:**
   ```bash
   docker-compose ps
   ```

## 🌐 Доступные URL

### Публичные сервисы
- **Frontend UI:** http://localhost:8080
- **Consul UI:** http://localhost:8500
- **PostgreSQL:** localhost:5432

## 🔧 Конфигурация

### База данных
- **Host:** localhost
- **Port:** 5432
- **Database:** simplebank_db
- **Username:** postgres
- **Password:** postgres

### Consul
- **Host:** localhost
- **Port:** 8500
- **UI:** http://localhost:8500

## 🛑 Остановка

### Maven
```bash
# Остановите каждый сервис (Ctrl+C в терминале)
```

### Docker
```bash
# Остановка всех сервисов
docker-compose down

# Остановка с удалением образов
docker-compose down --rmi all
```

## 📝 Восстановление бэкапов Consul

В папке `backups/` хранятся бэкапы конфигурации Consul. 
Восстановление происходит автоматически при запуске Consul, но можно выполнить вручную:

```bash
# Восстановление бэкапа вручную
docker exec -it consul consul snapshot restore /consul/backups/backup.snap
```

## 📚 Разработка

### Структура проекта
```
simplebank/
├── auth-server/          # Сервер аутентификации
├── accounts/             # Управление аккаунтами
├── cash/                 # Управление счетами
├── transfer/             # Переводы
├── exchange/             # Валютные операции
├── exchange-generator/   # Генератор курсов
├── blocker/              # Блокировка операций
├── notifications/        # Уведомления
├── gateway/              # API Gateway
├── front-ui/             # Веб-интерфейс
└── docker-compose.yaml   # Docker Compose
```
