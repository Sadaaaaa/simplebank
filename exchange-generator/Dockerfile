FROM openjdk:21-jdk-slim

WORKDIR /app

# Копируем JAR файл
COPY target/exchange-generator-0.0.1-SNAPSHOT.jar app.jar

# Создаем пользователя для запуска приложения
RUN groupadd -r spring && useradd -r -g spring spring
RUN chown -R spring:spring /app
USER spring

# Открываем порт
EXPOSE 8085

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]