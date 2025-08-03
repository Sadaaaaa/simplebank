package com.kitchentech.integration;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SimpleBankIntegrationTest {

    @Container
    private static final DockerComposeContainer<?> environment = new DockerComposeContainer<>(
            new File("docker-compose.yaml"))
            .withExposedService("auth-server", 9000)
            .withExposedService("accounts", 8082)
            .withExposedService("cash", 8083)
            .withExposedService("transfer", 8084)
            .withExposedService("exchange", 8086)
            .withExposedService("exchange-generator", 8085)
            .withExposedService("blocker", 8087)
            .withExposedService("notifications", 8088)
            .withExposedService("gateway", 8081)
            .withExposedService("front-ui", 8080)
            .withExposedService("postgres", 5432)
            .waitingFor("auth-server", Wait.forHttp("/actuator/health").forPort(9000))
            .waitingFor("accounts", Wait.forHttp("/actuator/health").forPort(8082))
            .waitingFor("cash", Wait.forHttp("/actuator/health").forPort(8083))
            .waitingFor("transfer", Wait.forHttp("/actuator/health").forPort(8084))
            .waitingFor("exchange", Wait.forHttp("/actuator/health").forPort(8086))
            .waitingFor("exchange-generator", Wait.forHttp("/actuator/health").forPort(8085))
            .waitingFor("blocker", Wait.forHttp("/actuator/health").forPort(8087))
            .waitingFor("notifications", Wait.forHttp("/actuator/health").forPort(8088))
            .waitingFor("gateway", Wait.forHttp("/actuator/health").forPort(8081))
            .waitingFor("front-ui", Wait.forHttp("/actuator/health").forPort(8080));

    @BeforeAll
    void setUp() {
        environment.start();
        
        // Настройка базовых URL для тестов
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = environment.getServicePort("gateway", 8081);
    }

    @Test
    void testHealthEndpoints() {
        // Тест health endpoints всех сервисов
        given()
            .when()
                .get("/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
    }

    @Test
    void testUserRegistration() {
        // Тест регистрации пользователя
        String userData = """
            {
                "username": "testuser",
                "password": "password123",
                "email": "test@example.com",
                "firstName": "Test",
                "lastName": "User",
                "birthDate": "1990-01-01"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(userData)
            .when()
                .post("/api/public/register")
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("username", equalTo("testuser"));
    }

    @Test
    void testAccountCreation() {
        // Тест создания счета
        String accountData = """
            {
                "userId": 1,
                "username": "testuser",
                "currency": "RUB",
                "name": "Основной счет",
                "balance": 1000.00
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(accountData)
            .when()
                .post("/api/cash/accounts")
            .then()
                .statusCode(200)
                .body("currency", equalTo("RUB"))
                .body("balance", equalTo(1000.0f));
    }

    @Test
    void testCashOperations() {
        // Тест операций с деньгами
        String operationData = """
            {
                "accountId": 1,
                "amount": 500.00,
                "operationType": "DEPOSIT"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(operationData)
            .when()
                .post("/api/cash/operation")
            .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void testTransferOperations() {
        // Тест переводов
        String transferData = """
            {
                "fromAccountId": 1,
                "toAccountId": 2,
                "amount": 100.00,
                "description": "Тестовый перевод"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(transferData)
            .when()
                .post("/api/transfer/internal")
            .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    void testExchangeRates() {
        // Тест получения курсов валют
        given()
            .when()
                .get("/api/exchange/rates")
            .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)));
    }

    @Test
    void testNotifications() {
        // Тест создания уведомлений
        String notificationData = """
            {
                "userId": 1,
                "message": "Тестовое уведомление"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(notificationData)
            .when()
                .post("/api/notifications/create")
            .then()
                .statusCode(200)
                .body("message", equalTo("Тестовое уведомление"));
    }

    @Test
    void testBlockerService() {
        // Тест сервиса блокировки
        String blockerData = """
            {
                "fromUserId": 1,
                "toUserId": 2,
                "amount": 50000.00,
                "currency": "RUB"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(blockerData)
            .when()
                .post("/api/blocker/check-transfer")
            .then()
                .statusCode(200)
                .body("allowed", equalTo(true));
    }

    @Test
    void testFrontendAccess() {
        // Тест доступа к фронтенду
        given()
            .when()
                .get("/")
            .then()
                .statusCode(200)
                .body(containsString("SimpleBank"));
    }

    @Test
    void testOAuth2Endpoints() {
        // Тест OAuth2 endpoints
        given()
            .when()
                .get("/.well-known/jwks.json")
            .then()
                .statusCode(200)
                .body("keys", hasSize(greaterThan(0)));
    }
} 