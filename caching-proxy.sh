#!/bin/bash
# caching-proxy.sh script

# Find the jar file
JAR_FILE=$(find ./target -name "logia-cache-proxy-*.jar" | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "Caching proxy JAR file not found. Did you run 'mvn clean package'?"
    exit 1
fi

# Process arguments to convert them to Spring Boot format
SPRING_ARGS=()
while [[ $# -gt 0 ]]; do
    case "$1" in
        --port)
            SPRING_ARGS+=("--port=$2")
            shift 2
            ;;
        --origin)
            SPRING_ARGS+=("--origin=$2")
            shift 2
            ;;
        --clear-cache)
            SPRING_ARGS+=("--clearCache=true")
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Run the JAR with the converted arguments
echo "Starting with arguments: ${SPRING_ARGS[@]}"
java -jar "$JAR_FILE" "${SPRING_ARGS[@]}"