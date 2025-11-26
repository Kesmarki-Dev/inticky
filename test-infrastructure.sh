#!/bin/bash

# Infrastructure Health Check Script

echo "🔍 Testing Inticky Infrastructure Services..."
echo ""

# Function to test service
test_service() {
    local service_name=$1
    local test_command=$2
    local port=$3
    
    echo -n "Testing $service_name... "
    
    if eval "$test_command" > /dev/null 2>&1; then
        echo "✅ OK"
        return 0
    else
        echo "❌ FAILED"
        return 1
    fi
}

# Test PostgreSQL
test_service "PostgreSQL" "docker exec inticky-postgres pg_isready -U inticky" 5432

# Test Redis
test_service "Redis" "docker exec inticky-redis redis-cli ping" 6379

# Test Kafka (check if broker is responsive)
test_service "Kafka" "docker exec inticky-kafka kafka-topics --bootstrap-server localhost:9092 --list" 9092

# Test HTTP services
test_service "Kafka UI" "curl -f http://localhost:8090" 8090
test_service "Jaeger" "curl -f http://localhost:16686" 16686
test_service "Prometheus" "curl -f http://localhost:9090" 9090
test_service "Grafana" "curl -f http://localhost:3000" 3000

echo ""
echo "🎯 Quick Service Tests:"

# PostgreSQL connection test
echo -n "PostgreSQL Database Connection... "
if docker exec inticky-postgres psql -U inticky -d inticky -c "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ OK"
else
    echo "❌ FAILED"
fi

# Redis set/get test
echo -n "Redis Cache Test... "
if docker exec inticky-redis redis-cli set test_key "test_value" > /dev/null 2>&1 && \
   docker exec inticky-redis redis-cli get test_key | grep -q "test_value"; then
    echo "✅ OK"
    docker exec inticky-redis redis-cli del test_key > /dev/null 2>&1
else
    echo "❌ FAILED"
fi

# Kafka topic creation test
echo -n "Kafka Topic Creation... "
if docker exec inticky-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic test-topic --partitions 1 --replication-factor 1 > /dev/null 2>&1; then
    echo "✅ OK"
    docker exec inticky-kafka kafka-topics --bootstrap-server localhost:9092 --delete --topic test-topic > /dev/null 2>&1
else
    echo "❌ FAILED"
fi

echo ""
echo "📊 Service URLs:"
echo "  - Kafka UI: http://localhost:8090"
echo "  - Jaeger: http://localhost:16686"
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana: http://localhost:3000 (admin/admin123)"
echo ""
echo "🔧 Database Access:"
echo "  - PostgreSQL: psql -h localhost -p 5432 -U inticky -d inticky"
echo "  - Redis: redis-cli -h localhost -p 6379"
echo ""
echo "✅ Infrastructure health check completed!"
