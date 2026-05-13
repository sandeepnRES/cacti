#!/bin/bash
# Copyright IBM Corp. All Rights Reserved.
#
# SPDX-License-Identifier: CC-BY-4.0

###############################################################################
# Weaver Asset Transfer Docker-based Test Automation Script
# 
# This script automates the complete setup and execution of Weaver asset
# transfer tests between Fabric and Corda networks using Docker containers.
#
# Based on the documentation at:
# https://hyperledger-cacti.github.io/cacti/weaver/getting-started/test-network/setup-local-docker/
#
# Prerequisites:
# - Docker and Docker Compose
# - Java 8, Go 1.20.2+, Node.js v18.18.2+
# - Protoc 3.15+
# - Make
#
# Usage: ./run-weaver-asset-transfer-docker.sh [options]
# Options:
#   --skip-build       Skip building Docker images
#   --skip-tests       Skip running tests (only setup)
#   --cleanup          Cleanup networks after tests
#   --help             Show this help message
###############################################################################

set -e  # Exit on error

# Enable alias expansion in bash
shopt -s expand_aliases 2>/dev/null || true

# Source shell configuration to ensure PATH is set correctly
if [ -f ~/.bashrc ]; then
    source ~/.bashrc 2>/dev/null || true
elif [ -f ~/.zshrc ]; then
    source ~/.zshrc 2>/dev/null || true
fi

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
WEAVER_ROOT="${REPO_ROOT}/weaver"

# Configuration
SKIP_BUILD=false
SKIP_TESTS=false
CLEANUP=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --skip-tests)
            SKIP_TESTS=true
            shift
            ;;
        --cleanup)
            CLEANUP=true
            shift
            ;;
        --help)
            grep "^#" "$0" | grep -v "#!/bin/bash" | sed 's/^# //'
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            exit 1
            ;;
    esac
done

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_section() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}$1${NC}"
    echo -e "${GREEN}========================================${NC}"
}

# Cleanup function
cleanup_networks() {
    log_section "Cleaning up networks and Docker containers"
    
    cd "${WEAVER_ROOT}"
    
    # Stop Corda drivers
    log_info "Stopping Corda drivers..."
    cd core/drivers/corda-driver
    make stop COMPOSE_ARG='--env-file docker-testnet-envs/.env.corda' || true
    make stop COMPOSE_ARG='--env-file docker-testnet-envs/.env.corda2' || true
    cd -
    
    # Stop Fabric drivers
    log_info "Stopping Fabric drivers..."
    cd core/drivers/fabric-driver
    make stop COMPOSE_ARG='--env-file docker-testnet-envs/.env.n1' || true
    make stop COMPOSE_ARG='--env-file docker-testnet-envs/.env.n2' || true
    cd -
    
    # Stop IIN Agents
    log_info "Stopping IIN Agents..."
    cd core/identity-management/iin-agent
    make stop COMPOSE_ARG='--env-file docker-testnet/envs/.env.n1.org1' || true
    make stop COMPOSE_ARG='--env-file docker-testnet/envs/.env.n2.org1' || true
    cd -
    
    # Stop Relays
    log_info "Stopping Relays..."
    cd core/relay
    make convert-compose-method2 || true
    make stop COMPOSE_ARG='--env-file docker/testnet-envs/.env.n1' || true
    make stop COMPOSE_ARG='--env-file docker/testnet-envs/.env.n2' || true
    make stop COMPOSE_ARG='--env-file docker/testnet-envs/.env.corda' || true
    make stop COMPOSE_ARG='--env-file docker/testnet-envs/.env.corda2' || true
    make convert-compose-method1 || true
    cd -
    
    # Stop Fabric networks
    log_info "Stopping Fabric networks..."
    cd tests/network-setups/fabric/dev
    make clean || true
    cd -
    
    # Stop Corda networks
    log_info "Stopping Corda networks..."
    cd tests/network-setups/corda
    make clean || true
    cd -
    
    log_success "Cleanup completed"
}

# Trap to cleanup on exit if requested
if [ "$CLEANUP" = true ]; then
    trap cleanup_networks EXIT
fi

# Check prerequisites
check_prerequisites() {
    log_section "Checking Prerequisites"
    
    local missing_deps=()
    
    if ! command -v docker &> /dev/null; then
        missing_deps+=("Docker")
    fi
    
    if ! command -v java &> /dev/null; then
        missing_deps+=("Java 8")
    fi
    
    if ! command -v go &> /dev/null; then
        missing_deps+=("Go 1.20.2+")
    fi
    
    if ! command -v node &> /dev/null; then
        missing_deps+=("Node.js v18.18.2+")
    fi
    
    if ! command -v make &> /dev/null; then
        missing_deps+=("Make")
    fi
    
    if [ ${#missing_deps[@]} -ne 0 ]; then
        log_error "Missing dependencies: ${missing_deps[*]}"
        exit 1
    fi
    
    log_success "All prerequisites satisfied"
}

# Setup Protoc
setup_protoc() {
    log_section "Setting up Protoc 3.15"
    
    # Check if protoc is already installed and has correct version
    if command -v protoc &> /dev/null; then
        PROTOC_VERSION=$(protoc --version | awk '{print $2}')
        log_info "Found protoc version: $PROTOC_VERSION"
        
        # Check if version is 3.12 or higher
        if [ "$(printf '%s\n' "3.12" "$PROTOC_VERSION" | sort -V | head -n1)" = "3.12" ]; then
            log_success "Protoc is already installed with sufficient version"
            
            # Still install Go plugins if not present
            if ! command -v protoc-gen-go &> /dev/null || ! command -v protoc-gen-go-grpc &> /dev/null; then
                log_info "Installing protoc-gen-go plugins..."
                go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.34.2
                go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.4.0
            fi
            
            return 0
        else
            log_warning "Protoc version $PROTOC_VERSION is too old, will download newer version"
        fi
    fi
    
    cd "${REPO_ROOT}"
    
    if [ ! -d "protoc" ]; then
        log_info "Downloading Protoc 3.15.6..."
        curl -LO https://github.com/protocolbuffers/protobuf/releases/download/v3.15.6/protoc-3.15.6-linux-x86_64.zip
        unzip -q protoc-3.15.6-linux-x86_64.zip -d protoc
        rm protoc-3.15.6-linux-x86_64.zip
    fi
    
    export PATH="${PATH}:${REPO_ROOT}/protoc/bin"
    
    log_info "Installing protoc-gen-go..."
    go install google.golang.org/protobuf/cmd/protoc-gen-go@v1.34.2
    go install google.golang.org/grpc/cmd/protoc-gen-go-grpc@v1.4.0
    
    log_success "Protoc setup completed"
}

# Build components
build_components() {
    if [ "$SKIP_BUILD" = true ]; then
        log_warning "Skipping build phase"
        return
    fi
    
    log_section "Building Weaver Components"
    
    cd "${WEAVER_ROOT}" || { log_error "Failed to cd to ${WEAVER_ROOT}"; exit 1; }
    
    # Build Protos
    log_info "Building GO Protos..."
    cd common/protos-go || { log_error "Failed to cd to common/protos-go"; exit 1; }
    if ! make build; then
        log_error "Failed to build GO Protos"
        exit 1
    fi
    cd ../.. || exit 1
    
    log_info "Building JS Protos..."
    cd common/protos-js || { log_error "Failed to cd to common/protos-js"; exit 1; }
    if ! make build; then
        log_error "Failed to build JS Protos"
        exit 1
    fi
    cd ../.. || exit 1
    
    log_info "Building Java Protos..."
    cd common/protos-java-kt || { log_error "Failed to cd to common/protos-java-kt"; exit 1; }
    # On ARM64 Macs, use Rosetta 2 for x86_64 emulation (grpc 1.29.0 doesn't have ARM64 builds)
    if [[ "$(uname -m)" == "arm64" ]] && [[ "$(uname -s)" == "Darwin" ]]; then
        log_info "Detected ARM64 Mac, using Rosetta 2 for Java proto build..."
        if ! arch -x86_64 make build; then
            log_error "Failed to build Java Protos with Rosetta 2"
            exit 1
        fi
    else
        if ! make build; then
            log_error "Failed to build Java Protos"
            exit 1
        fi
    fi
    cd ../.. || exit 1
    
    # Build Corda components
    log_info "Building Corda Interop App..."
    cd core/network/corda-interop-app || { log_error "Failed to cd to corda-interop-app"; exit 1; }
    if ! make build-local; then
        log_error "Failed to build Corda Interop App"
        exit 1
    fi
    cd ../../.. || exit 1
    
    log_info "Building Corda Interop SDK..."
    cd sdks/corda || { log_error "Failed to cd to sdks/corda"; exit 1; }
    if ! make build; then
        log_error "Failed to build Corda Interop SDK"
        exit 1
    fi
    cd ../.. || exit 1
    
    log_info "Building Corda SimpleApplication..."
    cd samples/corda/corda-simple-application || { log_error "Failed to cd to corda-simple-application"; exit 1; }
    if ! make build-local; then
        log_error "Failed to build Corda SimpleApplication"
        exit 1
    fi
    cd ../../.. || exit 1
    
    # Build Fabric components
    log_info "Building Fabric Interop SDK..."
    cd sdks/fabric/interoperation-node-sdk || { log_error "Failed to cd to interoperation-node-sdk"; exit 1; }
    if ! make build-local; then
        log_error "Failed to build Fabric Interop SDK"
        exit 1
    fi
    cd ../../.. || exit 1
    
    log_info "Building Fabric CLI..."
    cd samples/fabric/fabric-cli || { log_error "Failed to cd to fabric-cli"; exit 1; }
    if ! make build-local; then
        log_error "Failed to build Fabric CLI"
        exit 1
    fi
    cd ../../.. || exit 1
    
    # Build Docker images
    log_info "Building Relay Docker image..."
    cd core/relay || { log_error "Failed to cd to core/relay"; exit 1; }
    if ! make build-server-local; then
        log_error "Failed to build Relay Docker image"
        exit 1
    fi
    cd ../.. || exit 1
    
    log_info "Building Fabric Driver Docker image..."
    cd core/drivers/fabric-driver || { log_error "Failed to cd to fabric-driver"; exit 1; }
    if ! make build-image-local; then
        log_error "Failed to build Fabric Driver Docker image"
        exit 1
    fi
    cd ../../.. || exit 1
    
    log_info "Building Corda Driver Docker image..."
    cd core/drivers/corda-driver || { log_error "Failed to cd to corda-driver"; exit 1; }
    if ! make image-local; then
        log_error "Failed to build Corda Driver Docker image"
        exit 1
    fi
    cd ../../.. || exit 1
    
    log_info "Building IIN Agent Docker image..."
    cd core/identity-management/iin-agent || { log_error "Failed to cd to iin-agent"; exit 1; }
    if ! make build-image-local; then
        log_error "Failed to build IIN Agent Docker image"
        exit 1
    fi
    cd ../../.. || exit 1
    
    log_success "All components built successfully"
}

# Start networks
start_networks() {
    log_section "Starting Blockchain Networks"
    
    cd "${WEAVER_ROOT}"
    
    # Start Corda Network
    log_info "Starting Corda Network..."
    cd tests/network-setups/corda
    sed -i.bak "/docker logs corda_partya_1 -f/"' s/^/#/' "scripts/start-nodes.sh" || true
    make start-local &> corda-net.out &
    cd ../../..
    
    # Start Fabric Network with simpleassettransfer chaincode
    log_info "Starting Fabric Network with simpleassettransfer chaincode..."
    cd tests/network-setups/fabric/dev
    make start-interop-local CHAINCODE_NAME=simpleassettransfer
    cd ../../../..
    
    # Wait for networks to be ready
    log_info "Waiting for networks to initialize..."
    sleep 30
    
    # Check Corda logs
    log_info "Checking Corda Network logs..."
    cat tests/network-setups/corda/corda-net.out || true
    docker logs corda_partya_1 || true
    docker logs corda_network2_partya_1 || true
    
    log_success "Networks started successfully"
}

# Start relays
start_relays() {
    log_section "Starting Relays in Docker"
    
    cd "${WEAVER_ROOT}/core/relay"
    
    log_info "Editing Relay docker compose..."
    make convert-compose-method2
    
    log_info "Starting Relay for Fabric network1..."
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-relay-server#g" docker/testnet-envs/.env.n1
    make start-server COMPOSE_ARG='--env-file docker/testnet-envs/.env.n1'
    sleep 5
    
    log_info "Starting Relay for Fabric network2..."
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-relay-server#g" docker/testnet-envs/.env.n2
    make start-server COMPOSE_ARG='--env-file docker/testnet-envs/.env.n2'
    sleep 5
    
    log_info "Starting Relay for Corda_Network..."
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-relay-server#g" docker/testnet-envs/.env.corda
    make start-server COMPOSE_ARG='--env-file docker/testnet-envs/.env.corda'
    sleep 5
    
    log_info "Starting Relay for Corda_Network2..."
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-relay-server#g" docker/testnet-envs/.env.corda2
    make start-server COMPOSE_ARG='--env-file docker/testnet-envs/.env.corda2'
    sleep 5
    
    log_success "All relays started"
}

# Start drivers
start_drivers() {
    log_section "Starting Drivers in Docker"
    
    cd "${WEAVER_ROOT}"
    
    # Setup and start Fabric Drivers
    log_info "Setting up Fabric Drivers..."
    cd core/drivers/fabric-driver
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-driver-fabric#g" docker-testnet-envs/.env.n1
    sed -i.bak "s#<PATH-TO-WEAVER>#${WEAVER_ROOT}#g" docker-testnet-envs/.env.n1
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-driver-fabric#g" docker-testnet-envs/.env.n2
    sed -i.bak "s#<PATH-TO-WEAVER>#${WEAVER_ROOT}#g" docker-testnet-envs/.env.n2
    
    log_info "Starting Fabric Driver for network1..."
    make deploy COMPOSE_ARG='--env-file docker-testnet-envs/.env.n1' NETWORK_NAME=$(grep NETWORK_NAME docker-testnet-envs/.env.n1 | cut -d '=' -f 2)
    sleep 5
    
    log_info "Starting Fabric Driver for network2..."
    make deploy COMPOSE_ARG='--env-file docker-testnet-envs/.env.n2' NETWORK_NAME=$(grep NETWORK_NAME docker-testnet-envs/.env.n2 | cut -d '=' -f 2)
    sleep 5
    cd ../../..
    
    # Start Corda Drivers
    log_info "Setting up Corda Drivers..."
    cd core/drivers/corda-driver
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-driver-corda#g" docker-testnet-envs/.env.corda
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-driver-corda#g" docker-testnet-envs/.env.corda2
    
    log_info "Starting Corda_Network Driver..."
    make deploy COMPOSE_ARG='--env-file docker-testnet-envs/.env.corda'
    sleep 5
    
    log_info "Starting Corda_Network2 Driver..."
    make deploy COMPOSE_ARG='--env-file docker-testnet-envs/.env.corda2'
    sleep 5
    cd ../../..
    
    log_success "All drivers started"
}

# Start IIN Agents
start_iin_agents() {
    log_section "Starting IIN Agents in Docker"
    
    cd "${WEAVER_ROOT}/core/identity-management/iin-agent"
    
    log_info "Setting up Fabric IIN Agents..."
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-iin-agent#g" docker-testnet/envs/.env.n1.org1
    sed -i.bak "s#<PATH-TO-WEAVER>#${WEAVER_ROOT}#g" docker-testnet/envs/.env.n1.org1
    sed -i.bak "s#^AUTO_SYNC=true#AUTO_SYNC=false#g" docker-testnet/envs/.env.n1.org1
    
    sed -i.bak "s#^DOCKER_IMAGE_NAME=.*#DOCKER_IMAGE_NAME=cacti-weaver-iin-agent#g" docker-testnet/envs/.env.n2.org1
    sed -i.bak "s#<PATH-TO-WEAVER>#${WEAVER_ROOT}#g" docker-testnet/envs/.env.n2.org1
    sed -i.bak "s#^AUTO_SYNC=true#AUTO_SYNC=false#g" docker-testnet/envs/.env.n2.org1
    
    log_info "Starting Fabric IIN Agent for network1..."
    make deploy COMPOSE_ARG='--env-file docker-testnet/envs/.env.n1.org1' DLT_SPECIFIC_DIR=$(grep DLT_SPECIFIC_DIR docker-testnet/envs/.env.n1.org1 | cut -d '=' -f 2)
    sleep 5
    
    log_info "Starting Fabric IIN Agent for network2..."
    make deploy COMPOSE_ARG='--env-file docker-testnet/envs/.env.n2.org1' DLT_SPECIFIC_DIR=$(grep DLT_SPECIFIC_DIR docker-testnet/envs/.env.n2.org1 | cut -d '=' -f 2)
    sleep 5
    
    log_success "IIN Agents started"
}

# Configure Fabric CLI
configure_fabric_cli() {
    log_section "Configuring Fabric CLI"
    
    cd "${WEAVER_ROOT}/samples/fabric/fabric-cli"
    
    log_info "Setting up Fabric CLI Config..."
    cp config.template.json config.json
    sed -i.bak "s#<PATH-TO-WEAVER>#${WEAVER_ROOT}#g" config.json
    
    log_info "Setting up Fabric CLI ENV..."
    cp .env.template .env
    ./bin/fabric-cli env set-file ./.env
    ./bin/fabric-cli env set MEMBER_CREDENTIAL_FOLDER "${WEAVER_ROOT}/samples/fabric/fabric-cli/src/data/credentials"
    ./bin/fabric-cli env set CONFIG_PATH "${WEAVER_ROOT}/samples/fabric/fabric-cli/config.json"
    ./bin/fabric-cli env set DEFAULT_APPLICATION_CHAINCODE simpleassettransfer
    ./bin/fabric-cli env set REMOTE_CONFIG_PATH "${WEAVER_ROOT}/samples/fabric/fabric-cli/remote-network-config.json"
    ./bin/fabric-cli env set CHAINCODE_PATH "${WEAVER_ROOT}/samples/fabric/fabric-cli/chaincode.json"
    
    ./bin/fabric-cli config set network2 aclPolicyPrincipalType ca
    ./bin/fabric-cli config set network1 chaincode simpleassettransfer
    ./bin/fabric-cli config set network2 chaincode simpleassettransfer
    cp chaincode.json.template chaincode.json
    cp remote-network-config.json.template remote-network-config.json
    
    log_success "Fabric CLI configured"
}

# Initialize networks
initialize_networks() {
    log_section "Initializing Networks"
    
    # Initialize Fabric CLI
    log_info "Initializing Fabric networks..."
    cd "${WEAVER_ROOT}/samples/fabric/fabric-cli"
    ./bin/fabric-cli configure create all --local-network=network1
    ./bin/fabric-cli configure create all --local-network=network2
    ./bin/fabric-cli configure network --local-network=network1
    ./bin/fabric-cli configure network --local-network=network2
    
    log_info "Initializing assets for transfer..."
    ./scripts/initAssetsForTransfer.sh
    
    # Sync membership using IIN Agent
    log_info "Syncing Fabric membership using IIN Agent..."
    ./bin/fabric-cli configure membership --local-network=network1 --target-network=network2 --iin-agent-endpoint=localhost:9500
    sleep 10
    docker logs --tail 10 iin-agent-Org1MSP-network1 || true
    
    ./bin/fabric-cli configure membership --local-network=network2 --target-network=network1 --iin-agent-endpoint=localhost:9501
    sleep 10
    docker logs --tail 10 iin-agent-Org1MSP-network2 || true
    
    # Initialize Corda
    log_info "Setting up Corda CLI configuration..."
    cd "${WEAVER_ROOT}/samples/corda/corda-simple-application/clients/src/main/resources/config"
    cp remote-network-config.json.template remote-network-config.json
    
    log_info "Initializing Corda vault for asset transfer..."
    cd "${WEAVER_ROOT}/samples/corda/corda-simple-application"
    make initialise-vault-asset-transfer
    
    log_success "Networks initialized"
}

# Run asset transfer tests
run_asset_transfer_tests() {
    if [ "$SKIP_TESTS" = true ]; then
        log_warning "Skipping tests"
        return
    fi
    
    log_section "Running Asset Transfer Tests"
    
    # Run the same tests as in the GitHub workflow
    cd "${WEAVER_ROOT}/samples/corda/corda-simple-application"
    
    log_info "=== Corda to Corda Asset Transfer Tests ==="
    bash -c '
    COUNT=0
    TOTAL=9
    
    # Test 1: Issue t1:5 tokens
    NETWORK_NAME="Corda_Network" CORDA_PORT=10006 ./clients/build/install/clients/bin/clients issue-asset-state 5 t1 1> tmp.out
    cat tmp.out | grep "AssetState(quantity=5, tokenType=t1, owner=O=PartyA" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    # Test 2: Pledge Asset
    NETWORK_NAME="Corda_Network" CORDA_PORT=10006 ./clients/build/install/clients/bin/clients transfer pledge-asset --fungible --timeout="3600" --import-network-id="Corda_Network2" --recipient="O=PartyA, L=London, C=GB" --param="t1:5" 1> tmp.out
    cat tmp.out | grep "AssetPledgeState created with pledge-id" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    PID=$(cat tmp.out | grep "AssetPledgeState created with pledge-id " | awk -F "'"'"'" '"'"'{print $2}'"'"')
    
    # Test 3: Check if pledged
    CORDA_PORT=10006 ./clients/build/install/clients/bin/clients transfer is-asset-pledged -pid $PID 1> tmp.out
    cat tmp.out | grep "Is asset pledged for transfer response: true" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    # Test 4: Claim Remote Asset
    NETWORK_NAME="Corda_Network2" CORDA_PORT=30006 ./clients/build/install/clients/bin/clients transfer claim-remote-asset --pledge-id=$PID --locker="O=PartyA, L=London, C=GB" --transfer-category="token.corda" --export-network-id="Corda_Network" --param="t1:5" --import-relay-address="localhost:9082" 1> tmp.out
    cat tmp.out | grep "Pledged asset claim response: Right(b=SignedTransaction(id=" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    # Tests 5-6: Verify transfer
    CORDA_PORT=10006 ./clients/build/install/clients/bin/clients get-asset-states-by-type t1 1> tmp.out
    cat tmp.out | grep "\[\]" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    CORDA_PORT=30006 ./clients/build/install/clients/bin/clients get-asset-states-by-type t1 1> tmp.out
    cat tmp.out | grep "AssetState(quantity=5, tokenType=t1, owner=O=PartyA, L=London, C=GB," && COUNT=$((COUNT + 1)) && echo "PASS"
    
    # Tests 7-9: Reclaim scenario
    NETWORK_NAME="Corda_Network" CORDA_PORT=10006 ./clients/build/install/clients/bin/clients issue-asset-state 5 t2 1> tmp.out
    NETWORK_NAME="Corda_Network" CORDA_PORT=10006 ./clients/build/install/clients/bin/clients transfer pledge-asset --fungible --timeout="20" --import-network-id="Corda_Network2" --recipient="O=PartyA, L=London, C=GB" --param="t2:5" 1> tmp.out
    PID=$(cat tmp.out | grep "AssetPledgeState created with pledge-id " | awk -F "'"'"'" '"'"'{print $2}'"'"')
    sleep 20
    
    CORDA_PORT=10006 ./clients/build/install/clients/bin/clients transfer is-asset-pledged -pid $PID 1> tmp.out
    cat tmp.out | grep "Is asset pledged for transfer response: false" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    NETWORK_NAME=Corda_Network CORDA_PORT=10006 ./clients/build/install/clients/bin/clients transfer reclaim-pledged-asset --pledge-id=$PID --export-relay-address="localhost:9081" --transfer-category="token.corda" --import-network-id="Corda_Network2" --param="t2:5" 1> tmp.out
    cat tmp.out | grep "Pledged Asset Reclaim Response: Right(b=SignedTransaction(id=" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    CORDA_PORT=10006 ./clients/build/install/clients/bin/clients get-asset-states-by-type t2 1> tmp.out
    cat tmp.out | grep "AssetState(quantity=5, tokenType=t2, owner=O=PartyA, L=London, C=GB," && COUNT=$((COUNT + 1)) && echo "PASS"
    
    echo "Corda Tests: Passed $COUNT/$TOTAL"
    exit $((TOTAL - COUNT))
    '
    
    CORDA_RESULT=$?
    
    cd "${WEAVER_ROOT}/samples/fabric/fabric-cli"
    
    log_info "=== Fabric to Fabric Non-Fungible Asset Transfer Tests ==="
    bash -c '
    COUNT=0
    TOTAL=8
    
    # Tests as per workflow...
    ./bin/fabric-cli asset transfer pledge --source-network=network1 --dest-network=network2 --recipient=bob --expiry-secs=3600 --type=bond --ref=a03 --data-file=src/data/assetsForTransfer.json &> tmp.out
    tail -n 1 tmp.out | grep "Asset pledged with ID" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    CID=$(cat tmp.out | grep "Asset pledged with ID " | sed -e '"'"'s/Asset pledged with ID //'"'"')
    
    ./bin/fabric-cli asset transfer claim --source-network=network1 --dest-network=network2 --user=bob --owner=alice --type=bond.fabric --pledge-id=$CID --param=bond01:a03 &> tmp.out
    tail -n 1 tmp.out | grep "Called Function ClaimRemoteAsset. With Args: $CID" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli chaincode query --user=alice mychannel simpleassettransfer ReadAsset '"'"'["bond01","a03"]'"'"' --local-network=network1 &> tmp.out
    tail -n 2 tmp.out | grep "Error: the asset a03 does not exist" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli chaincode query --user=bob mychannel simpleassettransfer ReadAsset '"'"'["bond01","a03"]'"'"' --local-network=network2 &> tmp.out
    cat tmp.out | tr '"'"'\n'"'"' '"'"' '"'"' | grep "Result from network query: {     \"type\": \"bond01\",     \"id\": \"a03\"" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    # Reclaim tests
    ./bin/fabric-cli asset transfer pledge --source-network=network1 --dest-network=network2 --recipient=bob --expiry-secs=20 --type=bond --ref=a04 --data-file=src/data/assetsForTransfer.json &> tmp.out
    CID=$(cat tmp.out | grep "Asset pledged with ID " | sed -e '"'"'s/Asset pledged with ID //'"'"')
    sleep 20
    
    ./bin/fabric-cli asset transfer claim --source-network=network1 --dest-network=network2 --user=bob --owner=alice --type=bond.fabric --pledge-id=$CID --param=bond01:a04 &> tmp.out
    tail -n 1 tmp.out | grep "cannot claim asset with pledgeId $CID as the expiry time has elapsed" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli asset transfer reclaim --source-network=network1 --user=alice --type=bond.fabric --pledge-id=$CID --param=bond01:a04 &> tmp.out
    tail -n 1 tmp.out | grep "Called Function ReclaimAsset. With Args: $CID" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli chaincode query --user=alice mychannel simpleassettransfer ReadAsset '"'"'["bond01","a04"]'"'"' --local-network=network1 &> tmp.out
    cat tmp.out | tr '"'"'\n'"'"' '"'"' '"'"' | grep "Result from network query: {     \"type\": \"bond01\",     \"id\": \"a04\"" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli chaincode query --user=bob mychannel simpleassettransfer ReadAsset '"'"'["bond01","a04"]'"'"' --local-network=network2 &> tmp.out
    tail -n 2 tmp.out | grep "Error: the asset a04 does not exist" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    echo "Fabric Non-Fungible Tests: Passed $COUNT/$TOTAL"
    exit $((TOTAL - COUNT))
    '
    
    FABRIC_NF_RESULT=$?
    
    log_info "=== Fabric to Fabric Fungible Asset Transfer Tests ==="
    bash -c '
    COUNT=0
    TOTAL=8
    
    ./bin/fabric-cli asset transfer pledge --source-network=network1 --dest-network=network2 --recipient=bob --expiry-secs=3600 --type=token --units=50 --owner=alice --data-file=src/data/tokensForTransfer.json &> tmp.out
    tail -n 1 tmp.out | grep "Asset pledged with ID" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    CID=$(cat tmp.out | grep "Asset pledged with ID " | sed -e '"'"'s/Asset pledged with ID //'"'"')
    
    ./bin/fabric-cli asset transfer claim --source-network=network1 --dest-network=network2 --user=bob --owner=alice --type=token.fabric --pledge-id=$CID --param=token1:50 &> tmp.out
    tail -n 1 tmp.out | grep "Called Function ClaimRemoteTokenAsset. With Args: $CID" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli chaincode query --user=alice mychannel simpleassettransfer GetMyWallet '"'"'[]'"'"' --local-network=network1 &> tmp.out
    tail -n 2 tmp.out | grep "Result from network query: token1=\"9950\"" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli chaincode query --user=bob mychannel simpleassettransfer GetMyWallet '"'"'[]'"'"' --local-network=network2 &> tmp.out
    tail -n 2 tmp.out | grep "Result from network query: token1=\"50\"" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    # Reclaim tests
    ./bin/fabric-cli asset transfer pledge --source-network=network1 --dest-network=network2 --recipient=bob --expiry-secs=20 --type=token --units=100 --owner=alice --data-file=src/data/tokensForTransfer.json &> tmp.out
    CID=$(cat tmp.out | grep "Asset pledged with ID " | sed -e '"'"'s/Asset pledged with ID //'"'"')
    sleep 20
    
    ./bin/fabric-cli asset transfer claim --source-network=network1 --dest-network=network2 --user=bob --owner=alice --type=token.fabric --pledge-id=$CID --param=token1:100 &> tmp.out
    tail -n 1 tmp.out | grep "cannot claim asset with pledgeId $CID as the expiry time has elapsed" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli asset transfer reclaim --source-network=network1 --user=alice --type=token.fabric --pledge-id=$CID --param=token1:100 &> tmp.out
    tail -n 1 tmp.out | grep "Called Function ReclaimTokenAsset. With Args: $CID" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli chaincode query --user=alice mychannel simpleassettransfer GetMyWallet '"'"'[]'"'"' --local-network=network1 &> tmp.out
    tail -n 2 tmp.out | grep "Result from network query: token1=\"9950\"" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    ./bin/fabric-cli chaincode query --user=bob mychannel simpleassettransfer GetMyWallet '"'"'[]'"'"' --local-network=network2 &> tmp.out
    tail -n 2 tmp.out | grep "Result from network query: token1=\"50\"" && COUNT=$((COUNT + 1)) && echo "PASS"
    
    echo "Fabric Fungible Tests: Passed $COUNT/$TOTAL"
    exit $((TOTAL - COUNT))
    '
    
    FABRIC_F_RESULT=$?
    
    # Print summary
    log_section "Test Summary"
    if [ $CORDA_RESULT -eq 0 ]; then
        log_success "Corda Tests: PASSED"
    else
        log_error "Corda Tests: FAILED ($CORDA_RESULT failures)"
    fi
    
    if [ $FABRIC_NF_RESULT -eq 0 ]; then
        log_success "Fabric Non-Fungible Tests: PASSED"
    else
        log_error "Fabric Non-Fungible Tests: FAILED ($FABRIC_NF_RESULT failures)"
    fi
    
    if [ $FABRIC_F_RESULT -eq 0 ]; then
        log_success "Fabric Fungible Tests: PASSED"
    else
        log_error "Fabric Fungible Tests: FAILED ($FABRIC_F_RESULT failures)"
    fi
    
    TOTAL_FAILURES=$((CORDA_RESULT + FABRIC_NF_RESULT + FABRIC_F_RESULT))
    if [ $TOTAL_FAILURES -eq 0 ]; then
        log_success "All tests passed! ✓"
        return 0
    else
        log_error "Some tests failed! Total failures: $TOTAL_FAILURES"
        return 1
    fi
}

# Main execution
main() {
    log_section "Weaver Asset Transfer Docker-based Test Automation"
    log_info "Starting at $(date)"
    
    check_prerequisites
    setup_protoc
    build_components
    start_networks
    start_relays
    start_drivers
    start_iin_agents
    configure_fabric_cli
    initialize_networks
    run_asset_transfer_tests
    
    if [ "$CLEANUP" = true ]; then
        cleanup_networks
    else
        log_warning "Networks are still running. Use --cleanup flag to stop them."
        log_info "To manually cleanup, run this script with --cleanup flag"
    fi
    
    log_section "Automation Complete!"
    log_success "Finished at $(date)"
}

# Run main function
main "$@"

# Made with Bob

