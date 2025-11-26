#!/bin/bash

# Development environment startup script for Inticky Ticketing System

echo "🚀 Starting Inticky Development Environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install docker-compose first."
    exit 1
fi

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from template..."
    cp env.example .env
    echo "⚠️  Please edit .env file with your configuration before running services!"
fi

# Function to check service health
check_service_health() {
    local service=$1
    local port=$2
    local max_attempts=30
    local attempt=1
    
    echo "⏳ Waiting for $service to be ready on port $port..."
    
    while [ $attempt -le $max_attempts ]; do
        if nc -z localhost $port > /dev/null 2>&1 || curl -f http://localhost:$port/actuator/health > /dev/null 2>&1; then
            echo "✅ $service is ready"
            return 0
        fi
        
        echo "   Attempt $attempt/$max_attempts - waiting for $service..."
        sleep 2
        ((attempt++))
    done
    
    echo "❌ $service failed to start within expected time"
    return 1
}

# Parse command line arguments
INFRASTRUCTURE_ONLY=false
FULL_STACK=false
SERVICES_ONLY=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --infrastructure-only)
            INFRASTRUCTURE_ONLY=true
            shift
            ;;
        --full-stack)
            FULL_STACK=true
            shift
            ;;
        --services-only)
            SERVICES_ONLY=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--infrastructure-only|--full-stack|--services-only]"
            exit 1
            ;;
    esac
done

if [ "$INFRASTRUCTURE_ONLY" = true ]; then
    echo "📦 Starting infrastructure services only..."
    docker-compose up -d postgres redis zookeeper kafka kafka-ui jaeger prometheus grafana
    
    # Wait for core services
    check_service_health "PostgreSQL" 5432
    check_service_health "Redis" 6379
    check_service_health "Kafka" 9092
    
    echo ""
    echo "🎉 Infrastructure services are ready!"
    echo ""
    echo "📊 Available services:"
    echo "  - PostgreSQL: localhost:5432 (user: inticky, password: inticky123)"
    echo "  - Redis: localhost:6379"
    echo "  - Kafka: localhost:9092"
    echo "  - Kafka UI: http://localhost:8090"
    echo "  - Jaeger: http://localhost:16686"
    echo "  - Prometheus: http://localhost:9090"
    echo "  - Grafana: http://localhost:3000 (admin/admin123)"
    echo ""
    echo "🛠️  To start application services, run:"
    echo "  ./dev-start.sh --services-only"
    
elif [ "$SERVICES_ONLY" = true ]; then
    echo "🚀 Starting application services only..."
    docker-compose up -d api-gateway tenant-service user-service ticket-service ai-service notification-service
    
    # Wait for services
    check_service_health "API Gateway" 8080
    check_service_health "Tenant Service" 8081
    check_service_health "User Service" 8082
    check_service_health "Ticket Service" 8083
    check_service_health "AI Service" 8084
    check_service_health "Notification Service" 8085
    
    echo ""
    echo "🎉 Application services are ready!"
    echo ""
    echo "🌐 API Endpoints:"
    echo "  - API Gateway: http://localhost:8080"
    echo "  - Tenant Service: http://localhost:8081"
    echo "  - User Service: http://localhost:8082"
    echo "  - Ticket Service: http://localhost:8083"
    echo "  - AI Service: http://localhost:8084"
    echo "  - Notification Service: http://localhost:8085"
    
elif [ "$FULL_STACK" = true ]; then
    echo "🚀 Starting full stack (infrastructure + services)..."
    docker-compose up -d
    
    # Wait for all services
    echo "⏳ Waiting for all services to be ready..."
    sleep 60
    
    # Check infrastructure
    check_service_health "PostgreSQL" 5432
    check_service_health "Redis" 6379
    check_service_health "Kafka" 9092
    
    # Check application services
    check_service_health "API Gateway" 8080
    check_service_health "Tenant Service" 8081
    check_service_health "User Service" 8082
    check_service_health "Ticket Service" 8083
    check_service_health "AI Service" 8084
    check_service_health "Notification Service" 8085
    
    echo ""
    echo "🎉 Full stack is ready!"
    
else
    # Default: infrastructure only
    echo "📦 Starting infrastructure services (default)..."
    docker-compose up -d postgres redis zookeeper kafka kafka-ui jaeger prometheus grafana
    
    # Wait for core services
    check_service_health "PostgreSQL" 5432
    check_service_health "Redis" 6379
    check_service_health "Kafka" 9092
    
    echo ""
    echo "🎉 Infrastructure services are ready!"
fi

echo ""
echo "📊 All Available Services:"
echo "  Infrastructure:"
echo "    - PostgreSQL: localhost:5432"
echo "    - Redis: localhost:6379"
echo "    - Kafka: localhost:9092"
echo "    - Kafka UI: http://localhost:8090"
echo "    - Jaeger: http://localhost:16686"
echo "    - Prometheus: http://localhost:9090"
echo "    - Grafana: http://localhost:3000"
echo ""
echo "  Application Services:"
echo "    - API Gateway: http://localhost:8080"
echo "    - Tenant Service: http://localhost:8081"
echo "    - User Service: http://localhost:8082"
echo "    - Ticket Service: http://localhost:8083"
echo "    - AI Service: http://localhost:8084"
echo "    - Notification Service: http://localhost:8085"
echo ""
echo "📖 API Documentation:"
echo "  - Swagger UI: http://localhost:8080/swagger-ui.html"
echo ""
echo "🛑 To stop all services:"
echo "  docker-compose down"
echo ""
echo "🔧 Useful commands:"
echo "  - View logs: docker-compose logs -f [service-name]"
echo "  - Restart service: docker-compose restart [service-name]"
echo "  - Check status: docker-compose ps"
