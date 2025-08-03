#!/bin/bash

# Список сервисов
services=("cash" "transfer" "exchange" "exchange-generator" "blocker" "notifications" "gateway" "front-ui")

# Порты для каждого сервиса
declare -A ports
ports["cash"]="8083"
ports["transfer"]="8084"
ports["exchange"]="8086"
ports["exchange-generator"]="8085"
ports["blocker"]="8087"
ports["notifications"]="8088"
ports["gateway"]="8081"
ports["front-ui"]="8080"

# Генерация Chart.yaml для каждого сервиса
for service in "${services[@]}"; do
    cat > "helm-charts/charts/${service}/Chart.yaml" << EOF
apiVersion: v2
name: ${service}
description: A Helm chart for SimpleBank ${service} Service
type: application
version: 2.0.0
appVersion: "2.0.0"
EOF
done

# Генерация values.yaml для каждого сервиса
for service in "${services[@]}"; do
    port=${ports[$service]}
    cat > "helm-charts/charts/${service}/values.yaml" << EOF
replicaCount: 2

image:
  repository: simplebank/${service}
  tag: "2.0.0"
  pullPolicy: IfNotPresent

imagePullSecrets: []
nameOverride: ""
fullnameOverride: ""

serviceAccount:
  create: true
  annotations: {}
  name: ""

podAnnotations: {}

podSecurityContext: {}

securityContext: {}

service:
  type: ClusterIP
  port: ${port}

ingress:
  enabled: false
  className: ""
  annotations: {}
  hosts:
    - host: ${service}.local
      paths:
        - path: /
          pathType: ImplementationSpecific
  tls: []

resources:
  limits:
    cpu: 500m
    memory: 512Mi
  requests:
    cpu: 250m
    memory: 256Mi

autoscaling:
  enabled: false
  minReplicas: 1
  maxReplicas: 100
  targetCPUUtilizationPercentage: 80

nodeSelector: {}

tolerations: []

affinity: {}

# Application specific configuration
config:
  database:
    url: "jdbc:postgresql://postgresql:5432/simplebank_db"
    username: "postgres"
    password: "postgres"
  
  auth:
    server:
      url: "http://auth-server:9000"
      client:
        id: "${service}-client"
        secret: "${service}-secret"
  
  server:
    port: ${port}
  
  gateway:
    url: "http://gateway:8081"
EOF
done

# Генерация Deployment для каждого сервиса
for service in "${services[@]}"; do
    port=${ports[$service]}
    mkdir -p "helm-charts/charts/${service}/templates"
    
    cat > "helm-charts/charts/${service}/templates/deployment.yaml" << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: {{ include "${service}.fullname" . }}
  labels:
    {{- include "${service}.labels" . | nindent 4 }}
spec:
  {{- if not .Values.autoscaling.enabled }}
  replicas: {{ .Values.replicaCount }}
  {{- end }}
  selector:
    matchLabels:
      {{- include "${service}.selectorLabels" . | nindent 6 }}
  template:
    metadata:
      {{- with .Values.podAnnotations }}
      annotations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      labels:
        {{- include "${service}.selectorLabels" . | nindent 8 }}
    spec:
      {{- with .Values.imagePullSecrets }}
      imagePullSecrets:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      serviceAccountName: {{ include "${service}.serviceAccountName" . }}
      securityContext:
        {{- toYaml .Values.podSecurityContext | nindent 8 }}
      containers:
        - name: {{ .Chart.Name }}
          securityContext:
            {{- toYaml .Values.securityContext | nindent 12 }}
          image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"
          imagePullPolicy: {{ .Values.image.pullPolicy }}
          ports:
            - name: http
              containerPort: ${port}
              protocol: TCP
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: http
            initialDelaySeconds: 60
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: http
            initialDelaySeconds: 30
            periodSeconds: 5
          resources:
            {{- toYaml .Values.resources | nindent 12 }}
          env:
            - name: SPRING_DATASOURCE_URL
              value: {{ .Values.config.database.url }}
            - name: SPRING_DATASOURCE_USERNAME
              value: {{ .Values.config.database.username }}
            - name: SPRING_DATASOURCE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: {{ include "${service}.fullname" . }}-db-secret
                  key: password
            - name: AUTH_SERVER_URL
              value: {{ .Values.config.auth.server.url }}
            - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AUTH_SERVER_CLIENT_ID
              value: {{ .Values.config.auth.server.client.id }}
            - name: SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AUTH_SERVER_CLIENT_SECRET
              valueFrom:
                secretKeyRef:
                  name: {{ include "${service}.fullname" . }}-oauth-secret
                  key: client-secret
            - name: GATEWAY_URL
              value: {{ .Values.config.gateway.url }}
            - name: SERVER_PORT
              value: "{{ .Values.config.server.port }}"
      {{- with .Values.nodeSelector }}
      nodeSelector:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.affinity }}
      affinity:
        {{- toYaml . | nindent 8 }}
      {{- end }}
      {{- with .Values.tolerations }}
      tolerations:
        {{- toYaml . | nindent 8 }}
      {{- end }}
EOF
done

# Генерация Service для каждого сервиса
for service in "${services[@]}"; do
    port=${ports[$service]}
    cat > "helm-charts/charts/${service}/templates/service.yaml" << EOF
apiVersion: v1
kind: Service
metadata:
  name: {{ include "${service}.fullname" . }}
  labels:
    {{- include "${service}.labels" . | nindent 4 }}
spec:
  type: {{ .Values.service.type }}
  ports:
    - port: {{ .Values.service.port }}
      targetPort: http
      protocol: TCP
      name: http
  selector:
    {{- include "${service}.selectorLabels" . | nindent 4 }}
EOF
done

# Генерация Secret для каждого сервиса
for service in "${services[@]}"; do
    cat > "helm-charts/charts/${service}/templates/secret.yaml" << EOF
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "${service}.fullname" . }}-db-secret
  labels:
    {{- include "${service}.labels" . | nindent 4 }}
type: Opaque
data:
  password: {{ .Values.config.database.password | b64enc }}
---
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "${service}.fullname" . }}-oauth-secret
  labels:
    {{- include "${service}.labels" . | nindent 4 }}
type: Opaque
data:
  client-secret: {{ .Values.config.auth.server.client.secret | b64enc }}
EOF
done

# Генерация ServiceAccount для каждого сервиса
for service in "${services[@]}"; do
    cat > "helm-charts/charts/${service}/templates/serviceaccount.yaml" << EOF
{{- if .Values.serviceAccount.create -}}
apiVersion: v1
kind: ServiceAccount
metadata:
  name: {{ include "${service}.serviceAccountName" . }}
  labels:
    {{- include "${service}.labels" . | nindent 4 }}
  {{- with .Values.serviceAccount.annotations }}
  annotations:
    {{- toYaml . | nindent 4 }}
  {{- end }}
{{- end }}
EOF
done

# Генерация _helpers.tpl для каждого сервиса
for service in "${services[@]}"; do
    cat > "helm-charts/charts/${service}/templates/_helpers.tpl" << EOF
{{/*
Expand the name of the chart.
*/}}
{{- define "${service}.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "${service}.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- \$name := default .Chart.Name .Values.nameOverride }}
{{- if contains \$name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name \$name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "${service}.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "${service}.labels" -}}
helm.sh/chart: {{ include "${service}.chart" . }}
{{ include "${service}.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "${service}.selectorLabels" -}}
app.kubernetes.io/name: {{ include "${service}.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "${service}.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "${service}.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}
EOF
done

echo "All Helm charts generated successfully!" 