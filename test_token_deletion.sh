#!/bin/bash

# Quick Test: Verify Single Token Per User

echo "🧪 Testing Token Deletion on Login"
echo "===================================="
echo ""

# Check Redis before test
echo "1️⃣  Clearing any existing test tokens..."
docker exec -it redis redis-cli DEL "RefreshToken:*" 2>/dev/null || echo "Redis not in Docker, skipping cleanup"
echo ""

echo "2️⃣  Login 3 times with same user (each should delete previous tokens)"
echo "-----------------------------------------------------------------------"

for i in {1..3}; do
    echo "Login attempt $i..."
    curl -s -X POST http://localhost:8080/auth/login \
      -H "Content-Type: application/json" \
      -d '{"email":"test@example.com","password":"password123"}' > /dev/null
    
    sleep 1
    
    # Count tokens in Redis
    TOKEN_COUNT=$(docker exec redis redis-cli KEYS "RefreshToken:*" 2>/dev/null | wc -l)
    echo "   → Tokens in Redis: $TOKEN_COUNT"
done

echo ""
echo "3️⃣  Expected Result: Only 1 token should remain"
echo "------------------------------------------------"

# Final check
FINAL_COUNT=$(docker exec redis redis-cli KEYS "RefreshToken:*" 2>/dev/null | wc -l)
if [ "$FINAL_COUNT" -eq "1" ]; then
    echo "✅ SUCCESS: Only 1 token found (correct behavior)"
else
    echo "❌ FAILED: Found $FINAL_COUNT tokens (should be 1)"
fi

echo ""
echo "4️⃣  Check application logs for security messages:"
echo "---------------------------------------------------"
echo "Look for: '🔒 Security: Revoked N existing valid token(s) for user: test@example.com'"
