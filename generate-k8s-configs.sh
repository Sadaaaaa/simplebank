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

# Генерация application-k8s.properties для каждого сервиса
for service in "${services[@]}"; do
    port=${ports[$service]}
    
    cat > "${service}/src/main/resources/application-k8s.properties" << EOF
# Kubernetes configuration for ${service}
spring.application.name=${service}

# Database configuration
spring.datasource.url=\${SPRING_DATASOURCE_URL:jdbc:postgresql://postgresql:5432/simplebank_db}
spring.datasource.username=\${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=\${SPRING_DATASOURCE_PASSWORD:postgres}

# JPA configuration
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.default_schema=${service}_schema
spring.jpa.hibernate.ddl-auto=none
spring.jpa.properties.hibernate.format_sql=true

# SQL initialization
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql

# Actuator configuration
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always

# Server configuration
server.port=\${SERVER_PORT:${port}}

# OAuth2 configuration
auth.server.url=\${AUTH_SERVER_URL:http://auth-server:9000}

# OAuth2 Client configuration
spring.security.oauth2.client.registration.auth-server.client-id=\${SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AUTH_SERVER_CLIENT_ID:${service}-client}
spring.security.oauth2.client.registration.auth-server.client-secret=\${SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_AUTH_SERVER_CLIENT_SECRET:${service}-secret}
spring.security.oauth2.client.registration.auth-server.authorization-grant-type=client_credentials
spring.security.oauth2.client.registration.auth-server.scope=read,write

spring.security.oauth2.client.provider.auth-server.token-uri=\${AUTH_SERVER_URL:http://auth-server:9000}/oauth2/token

# Gateway configuration
gateway.url=\${GATEWAY_URL:http://gateway:8081}

# Session configuration
server.servlet.session.cookie.same-site=lax
server.servlet.session.cookie.path=/
EOF
done

echo "All Kubernetes configurations generated successfully!" 