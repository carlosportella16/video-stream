#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== Video Streaming Platform - Setup Script ===${NC}\n"

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker is installed${NC}"

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker daemon is not running. Please start Docker.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Docker daemon is running${NC}\n"

# Navigate to project root if running from scripts directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Build the project
echo -e "${YELLOW}📦 Building services with Maven...${NC}"

# Build streaming-service
echo -e "${BLUE}Building streaming-service...${NC}"
cd "$PROJECT_ROOT/services/streaming-service"
./mvnw clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to build streaming-service${NC}"
    exit 1
fi
echo -e "${GREEN}✓ streaming-service built successfully${NC}"

# Build analytics-service
echo -e "${BLUE}Building analytics-service...${NC}"
cd "$PROJECT_ROOT/services/analytics-service"
./mvnw clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to build analytics-service${NC}"
    exit 1
fi
echo -e "${GREEN}✓ analytics-service built successfully${NC}"

# Return to project root
cd "$PROJECT_ROOT"

echo -e "\n${YELLOW}🐳 Starting Docker containers...${NC}\n"

# Start docker-compose from project root
cd "$PROJECT_ROOT"

# Clean stale Kafka volume to prevent startup crashes from old data
docker volume rm video-stream_kafka_data 2>/dev/null || true

docker-compose up -d
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to start Docker services. Check logs with: docker-compose logs -f${NC}"
    exit 1
fi

echo -e "\n${YELLOW}📤 Uploading videos to S3...${NC}\n"

# Upload videos to LocalStack S3
"$SCRIPT_DIR/setup-s3.sh"
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to upload videos to S3${NC}"
    exit 1
fi

echo -e "\n${YELLOW}⏳ Waiting for services to be ready...${NC}\n"

# Wait for services to be ready
sleep 10

# Check if services are running
echo -e "${BLUE}Checking service health:${NC}"

FAILED=0

# Streaming Service
if curl -s http://localhost:8081/actuator/health > /dev/null; then
    echo -e "${GREEN}✓ Streaming Service (http://localhost:8081)${NC}"
else
    echo -e "${RED}❌ Streaming Service is not responding${NC}"
    FAILED=1
fi

# Analytics Service
if curl -s http://localhost:8083/actuator/health > /dev/null; then
    echo -e "${GREEN}✓ Analytics Service (http://localhost:8083)${NC}"
else
    echo -e "${RED}❌ Analytics Service is not responding${NC}"
    FAILED=1
fi

# Kafka
if docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Kafka (localhost:9092)${NC}"
else
    echo -e "${RED}❌ Kafka is not responding${NC}"
    FAILED=1
fi

# LocalStack
if curl -s http://localhost:4566/_localstack/health | grep -q '"services"'; then
    echo -e "${GREEN}✓ LocalStack S3 (http://localhost:4566)${NC}"
else
    echo -e "${RED}❌ LocalStack is not responding${NC}"
    FAILED=1
fi

echo -e "\n${GREEN}🎉 Setup complete!${NC}\n"

echo -e "${BLUE}Services Running:${NC}"
echo -e "  📺 Streaming Service: ${YELLOW}http://localhost:8081${NC}"
echo -e "  📊 Analytics Service: ${YELLOW}http://localhost:8083${NC}"
echo -e "  🌐 Frontend: ${YELLOW}http://localhost${NC}"
echo -e "  📡 Kafka: ${YELLOW}localhost:9092${NC}"
echo -e "  🪣 LocalStack S3: ${YELLOW}http://localhost:4566${NC}"

echo -e "\n${BLUE}Useful Commands:${NC}"
echo -e "  Stop all services:  ${YELLOW}docker-compose down${NC}"
echo -e "  View logs:          ${YELLOW}docker-compose logs -f${NC}"
echo -e "  View service logs:  ${YELLOW}docker-compose logs -f <service-name>${NC}"
echo -e "  Rebuild services:   ${YELLOW}./scripts/rebuild.sh${NC}"
echo -e "  Setup S3 bucket:    ${YELLOW}./scripts/setup-s3.sh${NC}"

echo ""

if [ $FAILED -ne 0 ]; then
    echo -e "${RED}⚠️ One or more services failed health checks.${NC}"
    echo -e "${YELLOW}Run: docker-compose logs -f kafka streaming-service analytics-service${NC}"
    exit 1
fi

