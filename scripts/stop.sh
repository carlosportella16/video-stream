#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== Stopping all services ===${NC}\n"

# Navigate to project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

docker-compose down

echo -e "\n${GREEN}✓ All services stopped${NC}"
echo -e "\nTo remove all volumes and start fresh:"
echo -e "  ${YELLOW}docker-compose down -v${NC}\n"

