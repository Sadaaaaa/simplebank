# Конфигурация Kubernetes для SimpleBank

Этот документ описывает ConfigMaps и Secrets, созданные для микросервисов SimpleBank.

## Структура конфигурации

### Secrets

#### 1. Общий Secret для PostgreSQL
```yaml
postgresql-secret
```
Содержит:
- `username`: имя пользователя БД (postgres)
- `password`: пароль БД (postgres) 
- `url`: строка подключения к БД

#### 2. OAuth2 Secrets для каждого сервиса
```yaml
accounts-oauth2-secret
blocker-oauth2-secret
cash-oauth2-secret
exchange-oauth2-secret
exchange-generator-oauth2-secret
gateway-oauth2-secret
notifications-oauth2-secret
transfer-oauth2-secret
```
Каждый содержит:
- `client-id`: идентификатор OAuth2 клиента
- `client-secret`: секрет OAuth2 клиента

### ConfigMaps

#### 1. Общий ConfigMap
```yaml
common-config
```
Содержит общие настройки:
- URLs сервисов
- Настройки БД
- Общие Spring Boot настройки

#### 2. ConfigMaps для каждого сервиса
```yaml
accounts-config
auth-server-config
blocker-config
cash-config
exchange-config
exchange-generator-config
gateway-config
notifications-config
transfer-config
front-ui-config
```

## Применение конфигурации

### 1. Создание namespace
```bash
kubectl create namespace simplebank
```

### 2. Применение Secrets
```bash
kubectl apply -f templates/common-secrets.yaml
kubectl apply -f templates/oauth2-secrets.yaml
```

### 3. Применение ConfigMaps
```bash
kubectl apply -f templates/common-configmap.yaml
kubectl apply -f charts/*/templates/configmap.yaml
```

## Использование в Deployments

### Переменные окружения из Secrets
```yaml
env:
- name: DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: postgresql-secret
      key: username
- name: OAUTH2_CLIENT_ID
  valueFrom:
    secretKeyRef:
      name: accounts-oauth2-secret
      key: client-id
```

### Монтирование ConfigMap как файла
```yaml
volumeMounts:
- name: config-volume
  mountPath: /app/config
  readOnly: true
volumes:
- name: config-volume
  configMap:
    name: accounts-config
```

## Обновление конфигурации

### Обновление Secrets
```bash
# Обновить пароль БД
kubectl create secret generic postgresql-secret \
  --from-literal=username=postgres \
  --from-literal=password=new_password \
  --from-literal=url=jdbc:postgresql://postgresql-service:5432/simplebank_db \
  --dry-run=client -o yaml | kubectl apply -f -
```

### Обновление ConfigMaps
```bash
kubectl apply -f charts/accounts/templates/configmap.yaml
```

## Проверка конфигурации

### Просмотр Secrets
```bash
kubectl get secrets -n simplebank
kubectl describe secret postgresql-secret -n simplebank
```

### Просмотр ConfigMaps
```bash
kubectl get configmaps -n simplebank
kubectl describe configmap accounts-config -n simplebank
```

### Проверка в поде
```bash
kubectl exec -it <pod-name> -n simplebank -- env | grep DB_
kubectl exec -it <pod-name> -n simplebank -- cat /app/config/application.properties
```

## Безопасность

1. **Secrets** содержат чувствительные данные и кодируются в base64
2. **ConfigMaps** содержат обычные настройки конфигурации
3. Доступ к ресурсам контролируется через RBAC
4. Рекомендуется использовать внешние системы управления секретами (Vault, AWS Secrets Manager) для продакшена

## Мониторинг

Все сервисы настроены с Actuator endpoints:
- `/actuator/health` - проверка здоровья
- `/actuator/info` - информация о приложении

Эти endpoints используются для liveness и readiness проб в Kubernetes.

