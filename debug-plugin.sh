#!/bin/bash

# WorldCRUD Plugin Debug Script
# Interactive debugging tool for testing plugin functionality

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_header() {
    echo -e "${BLUE}===========================================${NC}"
    echo -e "${BLUE}    WorldCRUD Plugin Debug Tool${NC}"
    echo -e "${BLUE}===========================================${NC}"
}

print_section() {
    echo -e "${YELLOW}--- $1 ---${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Check if server is running
check_server() {
    if ! screen -list | grep -q "minecraft"; then
        print_error "Server is not running. Please start the server first with 'make start'"
        exit 1
    fi
}

# Send command to server
send_command() {
    local cmd="$1"
    screen -S minecraft -p 0 -X stuff "$cmd$(printf \\r)"
    echo "Sent: $cmd"
    sleep 2
}

# Test basic commands
test_basic_commands() {
    print_section "Testing Basic Commands"
    
    send_command "worldcrud help"
    send_command "worldcrud version"
    send_command "worldcrud list"
    send_command "worldcrud permissions"
    
    print_success "Basic commands tested"
}

# Test world creation
test_world_creation() {
    print_section "Testing World Creation"
    
    send_command "worldcrud create testworld NORMAL"
    send_command "worldcrud create voidworld VOID"
    send_command "worldcrud create flatworld FLAT"
    send_command "worldcrud create islandworld ISLAND"
    
    print_success "World creation commands sent"
}

# Test world management
test_world_management() {
    print_section "Testing World Management"
    
    send_command "worldcrud info testworld"
    send_command "worldcrud save testworld"
    send_command "worldcrud clear testworld"
    send_command "worldcrud list"
    
    print_success "World management commands sent"
}

# Test teleportation (requires a player)
test_teleportation() {
    print_section "Testing Teleportation"
    echo "Note: These commands require a player to be online"
    
    send_command "worldcrud teleport testworld"
    send_command "worldcrud setspawn testworld"
    
    print_success "Teleportation commands sent"
}

# Test admin commands
test_admin_commands() {
    print_section "Testing Admin Commands"
    
    send_command "worldcrud debug on"
    send_command "worldcrud reload"
    send_command "worldcrud debug off"
    
    print_success "Admin commands sent"
}

# Test cleanup
test_cleanup() {
    print_section "Testing Cleanup (Optional)"
    echo "This will delete test worlds. Press Enter to continue or Ctrl+C to skip..."
    read -r
    
    send_command "worldcrud delete testworld"
    send_command "worldcrud delete voidworld"
    send_command "worldcrud delete flatworld"
    send_command "worldcrud delete islandworld"
    
    print_success "Cleanup commands sent"
}

# Interactive menu
show_menu() {
    echo ""
    echo "Select a test to run:"
    echo "1) Basic Commands"
    echo "2) World Creation"
    echo "3) World Management"
    echo "4) Teleportation"
    echo "5) Admin Commands"
    echo "6) Cleanup Test Worlds"
    echo "7) Run All Tests"
    echo "8) View Live Logs"
    echo "9) Exit"
    echo ""
    echo -n "Enter your choice (1-9): "
}

# View live logs
view_logs() {
    print_section "Viewing Live Logs"
    echo "Press Ctrl+C to stop viewing logs"
    
    if [[ -f "server/logs/latest.log" ]]; then
        tail -f server/logs/latest.log
    else
        print_error "No log file found"
    fi
}

# Main script
main() {
    print_header
    check_server
    
    while true; do
        show_menu
        read -r choice
        
        case $choice in
            1)
                test_basic_commands
                ;;
            2)
                test_world_creation
                ;;
            3)
                test_world_management
                ;;
            4)
                test_teleportation
                ;;
            5)
                test_admin_commands
                ;;
            6)
                test_cleanup
                ;;
            7)
                test_basic_commands
                test_world_creation
                test_world_management
                test_teleportation
                test_admin_commands
                echo ""
                echo "All tests completed! Check the server console for results."
                echo "Use option 8 to view live logs."
                ;;
            8)
                view_logs
                ;;
            9)
                echo "Goodbye!"
                exit 0
                ;;
            *)
                print_error "Invalid choice. Please select 1-9."
                ;;
        esac
    done
}

# Run the main function
main
