#!/bin/bash

# Run YugabyteDB examples as standalone applications
# This avoids exec:java classloader issues with Testcontainers

set -e

EXAMPLE_CLASS=${1:-YugabyteDBEmbeddingStoreExample}

echo "🚀 Building project..."
../mvnw clean compile -q

echo "📦 Running example: $EXAMPLE_CLASS"
echo ""

# Run with proper classpath
../mvnw exec:exec -Dexec.executable="java" \
  -Dexec.args="-cp %classpath $EXAMPLE_CLASS" \
  -q

echo ""
echo "✅ Example execution completed!"

