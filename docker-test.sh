#!/bin/bash

# WorldCRUD Plugin Docker Test Script
# Tests the plugin in a Docker container environment

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if Docker is running
check_docker() {
    if ! docker info &> /dev/null; then
        print_error "Docker is not running. Please start Docker."
        exit 1
    fi
    print_success "Docker is running"
}

# Build the plugin
build_plugin() {
    print_status "Building plugin..."
    mvn clean package -q
    
    if [[ -f "target/worldcrud-1.0.0.jar" ]]; then
        print_success "Plugin built successfully"
    else
        print_error "Plugin build failed"
        exit 1
    fi
}

# Start Docker container
start_container() {
    print_status "Starting Docker container..."
    
    # Stop existing container if running
    if docker ps -q -f name=worldcrud-test &> /dev/null; then
        print_warning "Stopping existing container..."
        docker stop worldcrud-test &> /dev/null || true
        docker rm worldcrud-test &> /dev/null || true
    fi
    
    # Start new container
    docker-compose up -d
    
    print_success "Container started"
}

# Wait for server to be ready
wait_for_server() {
    print_status "Waiting for server to start..."
    
    local attempts=0
    local max_attempts=60
    
    while [[ $attempts -lt $max_attempts ]]; do
        if docker logs worldcrud-minecraftbe 2>&1 | grep -q "Done"; then
            print_success "Server is ready"
            return 0
        fi
        
        echo -n "."
        sleep 2
        ((attempts++))
    done
    
    print_error "Server failed to start within timeout"
    exit 1
}

# Test plugin functionality
test_plugin() {
    print_status "Testing plugin functionality..."
    
    # Test basic commands
    print_status "Testing basic commands..."
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud help"
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud version"
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud list"
    
    # Test world creation
    print_status "Testing world creation..."
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud create testworld NORMAL"
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud create voidworld VOID"
    
    # Test world info
    print_status "Testing world info..."
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud info testworld"
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud list"
    
    # Test world management
    print_status "Testing world management..."
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud save testworld"
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud clear testworld"
    
    # Test cleanup
    print_status "Testing cleanup..."
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud delete testworld"
    docker exec worldcrud-minecraftbe rcon-cli "worldcrud delete voidworld"
    
    print_success "Plugin tests completed"
}

# Show server logs
show_logs() {
    print_status "Showing recent server logs..."
    docker logs --tail=50 worldcrud-minecraftbe
}

# Cleanup
cleanup() {
    print_status "Cleaning up..."
    docker-compose down -v
    print_success "Cleanup completed"
}

# Main execution
main() {
    print_status "Starting WorldCRUD Docker test..."
    
    check_docker
    build_plugin
    start_container
    wait_for_server
    
    # Give server a moment to fully initialize
    sleep 5
    
    test_plugin
    show_logs
    
    # Ask if user wants to keep container running
    echo ""
    echo -n "Keep container running for manual testing? (y/N): "
    read -r response
    
    if [[ ! $response =~ ^[Yy]$ ]]; then
        cleanup
    else
        print_success "Container is still running. Use 'docker-compose down' to stop it."
        print_status "Connect to server at localhost:25565"
        print_status "Use 'docker logs -f worldcrud-minecraftbe' to view logs"
    fi
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Run main function
main
