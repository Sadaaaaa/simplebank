# Развертывание SimpleBank в Kubernetes

## Предварительные требования

### Необходимое ПО
- Kubernetes кластер (minikube, kind, или production кластер)
- Helm 3.x
- kubectl
- Docker
- Docker registry (Docker Hub, AWS ECR, GCR, или локальный)

### Системные требования
- Минимум 4 CPU
- Минимум 8 GB RAM
- Минимум 20 GB свободного места

## Подготовка к развертыванию

### 1. Настройка Docker Registry

Обновите переменную `DOCKER_REGISTRY` в файле `helm-charts/values.yaml`:

```yaml
global:
  imageRegistry: "your-registry.com"
```

### 2. Сборка и публикация Docker образов

```bash
# Сборка всех образов
for service in auth-server accounts cash transfer exchange exchange-generator blocker notifications gateway front-ui; do
  docker build -t your-registry.com/simplebank/$service:latest -f $service/Dockerfile .
  docker push your-registry.com/simplebank/$service:latest
done
```

### 3. Настройка Kubernetes кластера

#### Для minikube:
```bash
minikube start --cpus=4 --memory=8192 --disk-size=20g
minikube addons enable ingress
```

#### Для kind:
```bash
kind create cluster --name simplebank --config - <<EOF
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
nodes:
- role: control-plane
  kubeadmConfigPatches:
  - |
    kind: InitConfiguration
    nodeRegistration:
      kubeletExtraArgs:
        node-labels: "ingress-ready=true"
  extraPortMappings:
  - containerPort: 80
    hostPort: 80
    protocol: TCP
  - containerPort: 443
    hostPort: 443
    protocol: TCP
- role: worker
- role: worker
EOF
```

## Развертывание

### 1. Добавление Helm репозиториев

```bash
helm repo add bitnami https://charts.bitnami.com/bitnami
helm repo update
```

### 2. Обновление зависимостей

```bash
cd helm-charts
helm dependency update
```

### 3. Создание namespace

```bash
kubectl create namespace simplebank
```

### 4. Установка приложения

```bash
helm install simplebank . \
  --namespace simplebank \
  --set global.imageRegistry=your-registry.com \
  --wait --timeout=10m
```

### 5. Проверка развертывания

```bash
# Проверка подов
kubectl get pods -n simplebank

# Проверка сервисов
kubectl get services -n simplebank

# Проверка ingress
kubectl get ingress -n simplebank

# Проверка секретов
kubectl get secrets -n simplebank
```

## Конфигурация

### Переменные окружения

Основные переменные, которые можно настроить:

```bash
helm install simplebank . \
  --namespace simplebank \
  --set global.imageRegistry=your-registry.com \
  --set postgresql.auth.postgresPassword=your-password \
  --set auth-server.replicaCount=2 \
  --set accounts.replicaCount=3 \
  --set cash.replicaCount=3 \
  --set transfer.replicaCount=3 \
  --set gateway.replicaCount=2 \
  --set front-ui.replicaCount=2
```

### Масштабирование

```bash
# Масштабирование конкретного сервиса
kubectl scale deployment simplebank-accounts --replicas=5 -n simplebank

# Или через Helm
helm upgrade simplebank . \
  --namespace simplebank \
  --set accounts.replicaCount=5
```

### Обновление приложения

```bash
# Обновление с новыми образами
helm upgrade simplebank . \
  --namespace simplebank \
  --set global.imageRegistry=your-registry.com \
  --set auth-server.image.tag=2.1.0 \
  --set accounts.image.tag=2.1.0
```

## Мониторинг и логирование

### Просмотр логов

```bash
# Логи конкретного пода
kubectl logs -f deployment/simplebank-auth-server -n simplebank

# Логи всех подов сервиса
kubectl logs -f -l app.kubernetes.io/name=auth-server -n simplebank
```

### Мониторинг ресурсов

```bash
# Использование ресурсов
kubectl top pods -n simplebank

# Использование ресурсов по узлам
kubectl top nodes
```

### Health checks

```bash
# Проверка health endpoints
kubectl port-forward svc/simplebank-auth-server 9000:9000 -n simplebank
curl http://localhost:9000/actuator/health
```

## Troubleshooting

### Проблемы с подключением к базе данных

```bash
# Проверка подключения к PostgreSQL
kubectl port-forward svc/postgresql 5432:5432 -n simplebank
psql -h localhost -U postgres -d simplebank_db
```

### Проблемы с OAuth2

```bash
# Проверка OAuth2 endpoints
kubectl port-forward svc/simplebank-auth-server 9000:9000 -n simplebank
curl http://localhost:9000/.well-known/jwks.json
```

### Проблемы с Ingress

```bash
# Проверка Ingress контроллера
kubectl get pods -n ingress-nginx

# Проверка Ingress правил
kubectl describe ingress -n simplebank
```

### Откат изменений

```bash
# Откат к предыдущей версии
helm rollback simplebank -n simplebank

# Просмотр истории релизов
helm history simplebank -n simplebank
```

## Удаление

### Полное удаление приложения

```bash
# Удаление Helm релиза
helm uninstall simplebank -n simplebank

# Удаление namespace
kubectl delete namespace simplebank

# Удаление Persistent Volumes (если нужно)
kubectl get pv | grep simplebank | awk '{print $1}' | xargs kubectl delete pv
```

## Безопасность

### Секреты

Все секреты хранятся в Kubernetes Secrets:

```bash
# Просмотр секретов
kubectl get secrets -n simplebank

# Обновление секретов
kubectl create secret generic db-secret \
  --from-literal=password=new-password \
  --dry-run=client -o yaml | kubectl apply -f -
```

### RBAC

Приложение использует ServiceAccounts для доступа к Kubernetes API:

```bash
# Просмотр ServiceAccounts
kubectl get serviceaccounts -n simplebank

# Просмотр ролей
kubectl get roles -n simplebank
kubectl get rolebindings -n simplebank
```

## Производительность

### Рекомендации по ресурсам

| Сервис | CPU Request | CPU Limit | Memory Request | Memory Limit |
|--------|-------------|-----------|----------------|--------------|
| auth-server | 250m | 500m | 256Mi | 512Mi |
| accounts | 250m | 500m | 256Mi | 512Mi |
| cash | 250m | 500m | 256Mi | 512Mi |
| transfer | 250m | 500m | 256Mi | 512Mi |
| exchange | 150m | 300m | 128Mi | 256Mi |
| exchange-generator | 150m | 300m | 128Mi | 256Mi |
| blocker | 150m | 300m | 128Mi | 256Mi |
| notifications | 150m | 300m | 128Mi | 256Mi |
| gateway | 250m | 500m | 256Mi | 512Mi |
| front-ui | 150m | 300m | 128Mi | 256Mi |

### Автоматическое масштабирование

```bash
# Включение HPA для сервисов
helm upgrade simplebank . \
  --namespace simplebank \
  --set accounts.autoscaling.enabled=true \
  --set accounts.autoscaling.minReplicas=2 \
  --set accounts.autoscaling.maxReplicas=10 \
  --set accounts.autoscaling.targetCPUUtilizationPercentage=80
``` 