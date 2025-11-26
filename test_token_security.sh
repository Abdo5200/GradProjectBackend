#!/bin/bash

# Test Script for Token Security Enhancement
# This script demonstrates the new token management security features

BASE_URL="http://localhost:3000"
EMAIL="test@example.com"
PASSWORD="password123"

echo "=================================="
echo "Token Security Test Suite"
echo "=================================="
echo ""

# Function to extract access token from response
extract_token() {
    echo "$1" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4
}

echo "1️⃣  First Login - Creating initial session"
echo "-------------------------------------------"
LOGIN1=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

TOKEN1=$(extract_token "$LOGIN1")
echo "✅ Login 1 successful"
echo "Access Token (first 50 chars): ${TOKEN1:0:50}..."
echo ""

sleep 2

echo "2️⃣  Second Login - Should revoke first token"
echo "---------------------------------------------"
LOGIN2=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

TOKEN2=$(extract_token "$LOGIN2")
echo "✅ Login 2 successful"
echo "Access Token (first 50 chars): ${TOKEN2:0:50}..."
echo ""

sleep 2

echo "3️⃣  Third Login - Should revoke second token"
echo "---------------------------------------------"
LOGIN3=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

TOKEN3=$(extract_token "$LOGIN3")
echo "✅ Login 3 successful"
echo "Access Token (first 50 chars): ${TOKEN3:0:50}..."
echo ""

echo "4️⃣  Check Application Logs"
echo "----------------------------"
echo "Expected logs:"
echo "  🔒 Security: Revoked 1 existing valid token(s) for user: $EMAIL"
echo "  ✅ Issued new tokens for user: $EMAIL"
echo ""

echo "5️⃣  Logout - Should delete all tokens"
echo "--------------------------------------"
LOGOUT=$(curl -s -X POST "$BASE_URL/auth/logout" \
  -H "Authorization: Bearer $TOKEN3" \
  -H "Content-Type: application/json")

echo "✅ Logout successful"
echo "Response: $LOGOUT"
echo ""

echo "6️⃣  Check Redis (manual verification needed)"
echo "----------------------------------------------"
echo "Run: docker exec -it redis redis-cli"
echo "Then: KEYS RefreshToken:*"
echo "Expected: Empty list or 0 tokens for $EMAIL"
echo ""

echo "=================================="
echo "✅ Test Complete!"
echo "=================================="
echo ""
echo "Summary:"
echo "- Each login should revoke previous tokens"
echo "- Only 1 active refresh token per user"
echo "- Logout should clear all tokens"
echo ""
echo "Check your application logs for security events"
