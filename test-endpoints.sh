#!/usr/bin/env bash
# Quick curl test script for Spring AI + PostgreSQL project

export PATH="$HOME/.local/bin:$PATH"
BASE_URL="http://localhost:8080"

echo "=== 1. Test PostgreSQL MCR: Fetch All Customers ==="
curl -s -X GET "${BASE_URL}/api/customers" | jq . || curl -s -X GET "${BASE_URL}/api/customers"
echo -e "\n"

echo "=== 2. Test Basic Gemini Chat ==="
curl -s -X POST "${BASE_URL}/api/ai/chat" \
  -H "Content-Type: application/json" \
  -d '{"message": "Explain in 2 sentences why Spring AI 2.0 is great for Java developers."}' | jq . || echo ""
echo -e "\n"

echo "=== 3. Test Agentic Gemini with PostgreSQL Tool Calling ==="
curl -s -X POST "${BASE_URL}/api/ai/chat-with-db" \
  -H "Content-Type: application/json" \
  -d '{"message": "Which customers are currently on an Enterprise plan and what are their notes?"}' | jq . || echo ""
echo -e "\n"

echo "=== 4. Test Spring AI 2.0 Structured Output Insight ==="
curl -s -X GET "${BASE_URL}/api/ai/insights/1" | jq . || echo ""
echo -e "\n"
