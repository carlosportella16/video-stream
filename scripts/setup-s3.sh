#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}=== Setting up S3 Bucket ===${NC}\n"

# Navigate to project root
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$PROJECT_ROOT"

# Check if LocalStack is running
if ! curl -s http://localhost:4566/_localstack/health > /dev/null; then
    echo -e "${RED}❌ LocalStack is not running. Please start Docker containers first.${NC}"
    exit 1
fi

echo -e "${GREEN}✓ LocalStack is running${NC}\n"

# Create S3 bucket
echo -e "${YELLOW}Creating S3 bucket 'videos'...${NC}"

docker exec localstack aws s3api create-bucket \
  --bucket videos \
  --region us-east-1 \
  --endpoint-url http://localhost:4566 \
  2>/dev/null

if [ $? -eq 0 ] || grep -q "BucketAlreadyOwnedByYou" <<< ""; then
    echo -e "${GREEN}✓ Bucket 'videos' is ready${NC}"
else
    # Check if bucket exists
    docker exec localstack aws s3 ls \
      --endpoint-url http://localhost:4566 | grep -q videos

    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Bucket 'videos' already exists${NC}"
    else
        echo -e "${RED}❌ Failed to create bucket${NC}"
        exit 1
    fi
fi

# Upload test video if available
if [ -d "$PROJECT_ROOT/services/streaming-service/local/bucket" ]; then
    echo -e "\n${YELLOW}Uploading test videos...${NC}"

    for movie_dir in "$PROJECT_ROOT/services/streaming-service/local/bucket"/*/; do
        movie_name=$(basename "$movie_dir")
        echo -e "${BLUE}Uploading $movie_name...${NC}"

        # Copy files into the localstack container, then upload using awslocal inside
        docker cp "$movie_dir" localstack:/tmp/$movie_name > /dev/null 2>&1

        docker exec localstack awslocal s3 cp \
          /tmp/$movie_name/ \
          s3://videos/$movie_name/ \
          --recursive > /dev/null

        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ $movie_name uploaded${NC}"
            # Clean up temp files inside container
            docker exec localstack rm -rf /tmp/$movie_name
        else
            echo -e "${RED}❌ Failed to upload $movie_name${NC}"
        fi
    done
else
    echo -e "${YELLOW}ℹ No local bucket found. You can upload videos manually.${NC}"
fi

echo -e "\n${GREEN}✓ S3 setup complete!${NC}\n"
echo -e "${BLUE}List buckets with:${NC}"
echo -e "  ${YELLOW}docker exec localstack aws s3 ls --endpoint-url http://localhost:4566${NC}\n"

