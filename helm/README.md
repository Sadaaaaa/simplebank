# SimpleBank Helm Charts

Этот каталог содержит Helm чарты для развертывания микросервисной архитектуры SimpleBank в Kubernetes.

## Структура

```
helm/
├── simplebank/          # Umbrella чарт для всего приложения
├── microservice-base/   # Базовый library чарт для микросервисов
├── accounts/           # Чарт для accounts сервиса
├── cash/              # Чарт для cash сервиса
├── exchange/          # Чарт для exchange сервиса
├── transfer/          # Чарт для transfer сервиса
├── notifications/     # Чарт для notifications сервиса
├── gateway/           # Чарт для gateway сервиса
├── auth-server/       # Чарт для auth-server сервиса
└── front-ui/          # Чарт для front-ui сервиса
```

## Быстрый старт

### 1. Установка всего приложения

```bash
# Добавить зависимости
helm dependency update helm/simplebank

# Установить приложение
helm install simplebank helm/simplebank -n simplebank --create-namespace
```

### 2. Установка отдельных компонентов

```bash
# Только PostgreSQL и Consul
helm install simplebank helm/simplebank -n simplebank --create-namespace \
  --set accounts.enabled=false \
  --set cash.enabled=false \
  --set exchange.enabled=false \
  --set transfer.enabled=false \
  --set notifications.enabled=false \
  --set gateway.enabled=false \
  --set authServer.enabled=false \
  --set frontUi.enabled=false

# Установить отдельный микросервис
helm install accounts helm/accounts -n simplebank
```

### 3. Обновление

```bash
# Обновить все приложение
helm upgrade simplebank helm/simplebank -n simplebank

# Обновить отдельный сервис
helm upgrade accounts helm/accounts -n simplebank
```

## Конфигурация

### Основные параметры

| Параметр | Описание | По умолчанию |
|----------|----------|--------------|
| `postgresql.enabled` | Включить PostgreSQL | `true` |
| `consul.enabled` | Включить Consul | `true` |
| `accounts.enabled` | Включить accounts сервис | `true` |
| `cash.enabled` | Включить cash сервис | `true` |
| `exchange.enabled` | Включить exchange сервис | `true` |
| `transfer.enabled` | Включить transfer сервис | `true` |
| `notifications.enabled` | Включить notifications сервис | `true` |
| `gateway.enabled` | Включить gateway сервис | `true` |
| `authServer.enabled` | Включить auth-server сервис | `true` |
| `frontUi.enabled` | Включить front-ui сервис | `true` |

### Пример кастомизации values

```yaml
# values-prod.yaml
postgresql:
  enabled: false  # Использовать внешнюю БД
  
accounts:
  replicaCount: 3
  resources:
    limits:
      cpu: 1000m
      memory: 1Gi
    requests:
      cpu: 500m
      memory: 512Mi
  database:
    url: "jdbc:postgresql://external-postgres:5432/simplebank_db"
    
cash:
  autoscaling:
    enabled: true
    minReplicas: 2
    maxReplicas: 5
    targetCPUUtilizationPercentage: 70
```

```bash
helm install simplebank helm/simplebank -n simplebank -f values-prod.yaml
```

## Мониторинг

### Проверка статуса

```bash
# Статус релиза
helm status simplebank -n simplebank

# Статус подов
kubectl get pods -n simplebank

# Логи сервиса
kubectl logs -f deployment/simplebank-accounts -n simplebank
```

### Health Checks

Все микросервисы имеют health check endpoints:
- Liveness: `/actuator/health`
- Readiness: `/actuator/health`

### Доступ к UI

```bash
# Port-forward для Consul UI
kubectl port-forward svc/simplebank-consul-ui 8500:80 -n simplebank

# Port-forward для Front UI
kubectl port-forward svc/simplebank-front-ui 8080:8080 -n simplebank
```

## Разработка

### Создание Docker образов

```bash
# Собрать все образы
./build-images.sh

# Собрать отдельный образ
docker build -t accounts:latest ./accounts/
```

### Локальная разработка с Minikube

```bash
# Запустить Minikube
minikube start

# Использовать Docker registry Minikube
eval $(minikube docker-env)

# Собрать образы
./build-images.sh

# Установить чарт
helm install simplebank helm/simplebank -n simplebank --create-namespace
```

## Troubleshooting

### Общие проблемы

1. **Pods не запускаются**: Проверьте наличие образов и правильность конфигурации
2. **Ошибки БД**: Убедитесь, что PostgreSQL запущен и доступен
3. **Ошибки сети**: Проверьте Service Discovery через Consul

### Полезные команды

```bash
# Описание пода
kubectl describe pod <pod-name> -n simplebank

# Логи инициализации
kubectl logs <pod-name> -n simplebank --previous

# Exec в под
kubectl exec -it <pod-name> -n simplebank -- /bin/bash

# Удалить все ресурсы
helm uninstall simplebank -n simplebank
kubectl delete namespace simplebank
```
