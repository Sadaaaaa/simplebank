# 🏦 SimpleBank - Микросервисная банковская система

Микросервисная архитектура банковской системы, построенная на Spring Boot с использованием Docker, Kubernetes и Helm для оркестрации.

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
- **Kubernetes** - оркестрация контейнеров
- **Helm** - управление пакетами Kubernetes
- **Docker & Docker Compose** - контейнеризация
- **Maven** - сборка проекта
- **Jenkins** - CI/CD

## 🚀 Запуск проекта

### Вариант 1: Локальная разработка с Docker Compose

#### Предварительные требования
- Java 21
- Maven 3.9+
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

### Вариант 2: Развертывание в Kubernetes

#### Предварительные требования
- Kubernetes кластер (minikube, kind, или production кластер)
- Helm 3.x
- kubectl
- Docker registry

#### Развертывание в Kubernetes

1. **Настройте Docker registry:**
   ```bash
   # Обновите DOCKER_REGISTRY в helm-charts/values.yaml
   # или передайте через --set global.imageRegistry=your-registry.com
   ```

2. **Соберите и запушьте Docker образы:**
   ```bash
   # Сборка всех образов
   mvn clean package -DskipTests
   
   # Сборка Docker образов
   for service in auth-server accounts cash transfer exchange exchange-generator blocker notifications gateway front-ui; do
     docker build -t your-registry.com/simplebank/$service:latest -f $service/Dockerfile .
     docker push your-registry.com/simplebank/$service:latest
   done
   ```

3. **Разверните в Kubernetes:**
   ```bash
   # Добавьте репозиторий Helm
   helm repo add bitnami https://charts.bitnami.com/bitnami
   
   # Обновите зависимости
   helm dependency update helm-charts
   
   # Разверните приложение
   helm install simplebank helm-charts \
     --namespace simplebank \
     --create-namespace \
     --set global.imageRegistry=your-registry.com
   ```

4. **Проверьте развертывание:**
   ```bash
   kubectl get pods -n simplebank
   kubectl get services -n simplebank
   kubectl get ingress -n simplebank
   ```

### Вариант 3: CI/CD с Jenkins

#### Настройка Jenkins

1. **Создайте Jenkins pipeline job:**
   - Создайте новый Pipeline job
   - Укажите Git repository
   - Используйте Jenkinsfile из корня проекта

2. **Настройте credentials в Jenkins:**
   - `DOCKER_USERNAME` - имя пользователя Docker registry
   - `DOCKER_PASSWORD` - пароль Docker registry
   - `KUBECONFIG` - конфигурация Kubernetes

3. **Обновите переменные окружения в Jenkinsfile:**
   ```groovy
   environment {
       DOCKER_REGISTRY = 'your-registry.com'
       KUBERNETES_NAMESPACE = 'simplebank'
   }
   ```

#### Запуск CI/CD

1. **Для тестовой среды:**
   ```bash
   # Создайте ветку develop и запушьте изменения
   git checkout -b develop
   git push origin develop
   # Jenkins автоматически запустит pipeline и развернет в test environment
   ```

2. **Для продакшн среды:**
   ```bash
   # Создайте pull request из develop в main
   # После merge в main Jenkins автоматически развернет в production
   ```

## 🌐 Доступные URL

### Локальная разработка (Docker Compose)
- **Frontend UI:** http://localhost:8080
- **PostgreSQL:** localhost:5432

### Kubernetes развертывание
- **Frontend UI:** http://simplebank.local (настройте DNS или добавьте в /etc/hosts)
- **API Gateway:** http://gateway.simplebank.local

## 🔧 Конфигурация

### База данных
- **Host:** postgresql (в Kubernetes) или localhost (локально)
- **Port:** 5432
- **Database:** simplebank_db
- **Username:** postgres
- **Password:** postgres

### Kubernetes конфигурация
- **Namespace:** simplebank
- **Storage Class:** default
- **Ingress Class:** nginx

## 📝 Структура проекта

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
├── helm-charts/          # Helm чарты для Kubernetes
│   ├── Chart.yaml        # Зонтичный чарт
│   ├── values.yaml       # Основные значения
│   └── charts/           # Сабчарты для каждого сервиса
├── Jenkinsfile           # CI/CD pipeline
├── docker-compose.yaml   # Docker Compose для локальной разработки
└── README.md
```

## 🧪 Тестирование

### Локальные тесты
```bash
# Запуск всех тестов
mvn test

# Запуск тестов конкретного сервиса
cd auth-server && mvn test
```

### Helm тесты
```bash
# Тестирование Helm чартов
helm test simplebank -n simplebank
```

### Интеграционные тесты
```bash
# Запуск интеграционных тестов (требует запущенных сервисов)
mvn verify -P integration-test
```

## 🛑 Остановка

### Docker Compose
```bash
# Остановка всех сервисов
docker-compose down

# Остановка с удалением образов
docker-compose down --rmi all
```

### Kubernetes
```bash
# Удаление Helm релиза
helm uninstall simplebank -n simplebank

# Удаление namespace
kubectl delete namespace simplebank
```

## 🔄 Обновление

### Обновление в Kubernetes
```bash
# Обновление Helm релиза
helm upgrade simplebank helm-charts -n simplebank

# Обновление с новыми значениями
helm upgrade simplebank helm-charts -n simplebank --set replicaCount=3
```

### Откат изменений
```bash
# Откат к предыдущей версии
helm rollback simplebank -n simplebank

# Просмотр истории релизов
helm history simplebank -n simplebank
```

## 📚 Разработка

### Добавление нового микросервиса

1. **Создайте новый Spring Boot проект**
2. **Добавьте Helm чарт:**
   ```bash
   # Создайте новый чарт
   helm create helm-charts/charts/new-service
   ```
3. **Обновите зонтичный чарт:**
   ```yaml
   # В helm-charts/Chart.yaml добавьте зависимость
   dependencies:
     - name: new-service
       version: 2.0.0
       condition: new-service.enabled
   ```
4. **Обновите Jenkinsfile:**
   ```groovy
   // Добавьте этапы для нового сервиса
   stage('Build New Service') {
       steps {
           script {
               buildMicroservice('new-service', '8089')
           }
       }
   }
   ```

### Мониторинг и логирование

```bash
# Просмотр логов
kubectl logs -f deployment/auth-server -n simplebank

# Просмотр метрик
kubectl top pods -n simplebank

# Доступ к базе данных
kubectl port-forward svc/postgresql 5432:5432 -n simplebank
```

## 🤝 Вклад в проект

1. Fork репозитория
2. Создайте feature branch (`git checkout -b feature/amazing-feature`)
3. Commit изменения (`git commit -m 'Add amazing feature'`)
4. Push в branch (`git push origin feature/amazing-feature`)
5. Откройте Pull Request

## 📄 Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для деталей.

## 🆘 Поддержка

Если у вас есть вопросы или проблемы:
- Создайте Issue в GitHub
- Обратитесь к документации в папке `docs/`
- Проверьте логи сервисов в Kubernetes
