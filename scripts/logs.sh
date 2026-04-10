#!/bin/bash

# Colors for output
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}=== Viewing Application Logs ===${NC}\n"

# Navigate to project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

if [ -z "$1" ]; then
    echo -e "Usage: ${YELLOW}./scripts/logs.sh [service]${NC}"
    echo ""
    echo -e "Services:"
    echo -e "  ${YELLOW}streaming-service${NC}   - Video streaming API"
    echo -e "  ${YELLOW}analytics-service${NC}   - Analytics consumer"
    echo -e "  ${YELLOW}kafka${NC}               - Message broker"
    echo -e "  ${YELLOW}localstack${NC}         - S3 storage"
    echo -e "  ${YELLOW}frontend${NC}           - Web server"
    echo ""
    echo -e "Examples:"
    echo -e "  ${YELLOW}./scripts/logs.sh streaming-service${NC}"
    echo -e "  ${YELLOW}./scripts/logs.sh kafka${NC}"
    echo ""
    echo -e "View all logs: ${YELLOW}docker-compose logs -f${NC}"
else
    docker-compose logs -f "$1"
fi

