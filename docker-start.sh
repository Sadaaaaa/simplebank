#!/bin/bash

echo "🚀 Запуск SimpleBank микросервисов через Docker..."

if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен. Пожалуйста, установите Docker."
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose не установлен. Пожалуйста, установите Docker Compose."
    exit 1
fi

echo "🛑 Остановка существующих контейнеров..."
docker-compose down

read -p "🗑️ Удалить старые образы? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "🗑️ Удаление старых образов..."
    docker-compose down --rmi all
fi

echo "🔨 Сборка и запуск сервисов..."
docker-compose up --build -d

echo "⏳ Ожидание запуска сервисов..."
sleep 30

echo "📊 Статус контейнеров:"
docker-compose ps

echo ""
echo "✅ SimpleBank запущен!"
echo ""
echo "🌐 Доступные сервисы:"
echo "   Frontend UI: http://localhost:8080"
echo "   Gateway: http://localhost:8081"
echo "   Auth Server: http://localhost:9000"
echo "   Accounts: http://localhost:8082"
echo "   Cash: http://localhost:8083"
echo "   Transfer: http://localhost:8084"
echo "   Exchange Generator: http://localhost:8085"
echo "   Exchange: http://localhost:8086"
echo "   Blocker: http://localhost:8087"
echo "   Notifications: http://localhost:8088"
echo "   Consul UI: http://localhost:8500"
echo "   PostgreSQL: localhost:5432"
echo ""
echo "📝 Для просмотра логов используйте: docker-compose logs -f [service-name]"
echo "🛑 Для остановки используйте: docker-compose down" 