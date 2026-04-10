#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== Rebuilding Services ===${NC}\n"

# Navigate to project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Build streaming-service
echo -e "${YELLOW}Building streaming-service...${NC}"
cd "$PROJECT_ROOT/services/streaming-service"
./mvnw clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to build streaming-service${NC}"
    exit 1
fi
echo -e "${GREEN}✓ streaming-service built${NC}"

# Build analytics-service
echo -e "${YELLOW}Building analytics-service...${NC}"
cd "$PROJECT_ROOT/services/analytics-service"
./mvnw clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to build analytics-service${NC}"
    exit 1
fi
echo -e "${GREEN}✓ analytics-service built${NC}"

# Return to project root
cd "$PROJECT_ROOT"

echo -e "\n${YELLOW}Rebuilding Docker containers...${NC}\n"

# Rebuild and restart containers
docker-compose up -d --build streaming-service analytics-service

echo -e "\n${GREEN}✓ Services rebuilt and restarted${NC}"
echo -e "View logs with: ${YELLOW}docker-compose logs -f${NC}"

