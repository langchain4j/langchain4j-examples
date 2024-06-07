#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"
# Without enforcer.skip, the quartus example cannot compile successfully
mvn install -DskipTests -Denforcer.skip=true -U
