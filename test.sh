#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "==============================="
echo "1. Register Trainee"
echo "==============================="
REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/trainees" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Doe",
    "dateOfBirth": "2000-01-01",
    "address": "Nizami 27"
  }')
echo $REGISTER_RESPONSE

USERNAME=$(echo $REGISTER_RESPONSE | grep -o '"username":"[^"]*"' | cut -d'"' -f4)
PASSWORD=$(echo $REGISTER_RESPONSE | grep -o '"password":"[^"]*"' | cut -d'"' -f4)
echo "Extracted username: $USERNAME"
echo "Extracted password: $PASSWORD"

echo ""
echo "==============================="
echo "2. Authenticate Trainee"
echo "==============================="
AUTH_RESPONSE=$(curl -s -X GET "$BASE_URL/trainees/authenticate" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USERNAME\",
    \"password\": \"$PASSWORD\"
  }")
echo $AUTH_RESPONSE

TOKEN=$(echo $AUTH_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
echo "Extracted token: $TOKEN"

echo ""
echo "==============================="
echo "3. Get Trainee Profile"
echo "==============================="
curl -s -X GET "$BASE_URL/trainees/$USERNAME" \
  -H "Authorization: Bearer $TOKEN" | echo $(cat)

echo ""
echo "==============================="
echo "4. Update Trainee Profile"
echo "==============================="
curl -s -X PUT "$BASE_URL/trainees/$USERNAME" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"username\": \"$USERNAME\",
    \"firstName\": \"Janet\",
    \"lastName\": \"Doe\",
    \"dateOfBirth\": \"2000-01-01\",
    \"address\": \"Rasul Rza 10\",
    \"isActive\": true
  }"

echo ""
echo "==============================="
echo "5. Change Password"
echo "==============================="
NEW_PASSWORD="newpass123"
curl -s -X PUT "$BASE_URL/trainees/$USERNAME/password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"oldPassword\": \"$PASSWORD\",
    \"newPassword\": \"$NEW_PASSWORD\"
  }"
echo "Password changed to: $NEW_PASSWORD"

echo ""
echo "==============================="
echo "6. Activate Trainee"
echo "==============================="
curl -s -X PATCH "$BASE_URL/trainees/$USERNAME/activate/true" \
  -H "Authorization: Bearer $TOKEN"

echo ""
echo "==============================="
echo "7. Get Unassigned Trainers"
echo "==============================="
curl -s -X GET "$BASE_URL/trainees/$USERNAME/unassigned-trainers" \
  -H "Authorization: Bearer $TOKEN"

echo ""
echo "==============================="
echo "8. Delete Trainee"
echo "==============================="
curl -s -X DELETE "$BASE_URL/trainees/$USERNAME" \
  -H "Authorization: Bearer $TOKEN"
echo "Trainee $USERNAME deleted"
