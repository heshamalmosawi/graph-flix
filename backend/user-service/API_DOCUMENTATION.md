# User Service API Documentation

## Overview

The User Service provides authentication functionality for the GraphFlix application. It handles user registration and login using JWT (JSON Web Tokens) for stateless authentication.

**Base URL:** `http://localhost:37579` (or dynamically assigned port)

**Authentication:** JWT-based stateless authentication

---

## Endpoints

### 1. Register User

Registers a new user account in the system.

**Endpoint:** `POST /auth/register`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "string",
  "email": "string",
  "password": "string"
}
```

**Request Fields:**

| Field   | Type   | Required | Description                        |
|---------|--------|----------|------------------------------------|
| name    | string | Yes      | User's full name                  |
| email   | string | Yes      | Valid email address               |
| password | string | Yes      | Password (minimum 6 characters)  |

**Validation Rules:**
- `name`: Non-empty string
- `email`: Must match email pattern (valid email format)
- `password`: Minimum 6 characters

**Success Response:**
```
HTTP/1.1 201 Created
User registered successfully
```

**Error Responses:**

**400 Bad Request** - Invalid input:
```json
{
  "error": "Name is required"
}
```

**400 Bad Request** - Email already in use:
```json
{
  "error": "Email already in use"
}
```

**400 Bad Request** - Invalid email format:
```json
{
  "error": "Invalid email format"
}
```

**400 Bad Request** - Password too short:
```json
{
  "error": "Password must be at least 6 characters long"
}
```

**Example Request:**
```bash
curl -X POST http://localhost:37579/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "password": "securePass123"
  }'
```

---

### 2. Login

Authenticates a user and returns a JWT token for subsequent authenticated requests.

**Endpoint:** `POST /auth/login`

**Request Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Request Fields:**

| Field   | Type   | Required | Description           |
|---------|--------|----------|-----------------------|
| email   | string | Yes      | User's email address |
| password | string | Yes      | User's password      |

**Success Response:**
```
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "name": "string",
  "token": "string",
  "expiresAt": number
}
```

**Response Fields:**

| Field     | Type   | Description                                      |
|-----------|--------|--------------------------------------------------|
| name      | string | User's name from the database                    |
| token     | string | JWT authentication token                           |
| expiresAt | number | Unix timestamp when the token expires (3 days) |

**Error Responses:**

**401 Unauthorized** - Invalid credentials:
```json
{
  "error": "Invalid email or password"
}
```

**401 Unauthorized** - Email not found:
```json
{
  "error": "Invalid email or password"
}
```

**Example Request:**
```bash
curl -X POST http://localhost:37579/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securePass123"
  }'
```

**Example Response:**
```json
{
  "name": "John Doe",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwibmFtZSI6IlRlc3QgVXNlciIsImlhdCI6MTc3MTE3OTg5MSwiZXhwIjoxNzcxNDM5MDkxfQ.9DmHT63nPVniyT3Ti8TP9s1OvH-ZR5ECwxaeMvSDcXw",
  "expiresAt": 1771439091248
}
```

---

## Using the JWT Token

After successful login, include the JWT token in the `Authorization` header for authenticated requests:

**Header Format:**
```
Authorization: Bearer <your-jwt-token>
```

**Example:**
```bash
curl -X GET http://localhost:37579/protected-endpoint \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**Token Expiration:**
- Valid for **3 days** from issue
- After expiration, user must login again
- Check `expiresAt` field in login response to know when token expires

---

## Security Considerations

### Password Security
- Passwords are encrypted using **BCrypt** before storage
- Never store plain text passwords
- Consider implementing password complexity requirements (currently minimum 6 characters)

### JWT Token Security
- Tokens are signed with a secret key
- Never share tokens over unencrypted channels
- Store tokens securely on the client side (e.g., localStorage with proper safeguards)
- Implement token refresh mechanism for better UX (not currently implemented)

### Email Validation
- Email format is validated using regex: `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`
- Duplicate emails are prevented during registration

---

## Error Codes

| Status Code | Description                         |
|-------------|-------------------------------------|
| 201         | User created successfully               |
| 200         | Login successful, token returned           |
| 400         | Bad request - invalid input data       |
| 401         | Unauthorized - invalid credentials       |
| 401         | Unauthorized - expired or invalid token |

---

## Data Model

### User Entity

```java
@Node
public class User {
    @Id
    private String id;
    
    @Property("name")
    private String name;
    
    @Property("email")
    private String email;
    
    @Property("password")
    private String password;  // BCrypt encrypted
    
    @Property("createdAt")
    private LocalDateTime createdAt;
}
```

**Stored in:** Neo4j database
**Database:** Neo4j Aura Cloud

---

## Testing

### Test Registration
```bash
# Register a new user
curl -X POST http://localhost:37579/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "testuser@example.com",
    "password": "password123"
  }'
```

### Test Login
```bash
# Login with registered credentials
curl -X POST http://localhost:37579/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "password123"
  }'
```

### Test with Token
```bash
# Get token from login response
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Use token for authenticated request (if any protected endpoints exist)
curl -X GET http://localhost:37579/protected \
  -H "Authorization: Bearer $TOKEN"
```

---

## Configuration

### Application Properties

```properties
# Server
server.port=0  # Random port assigned on startup

# Neo4j Connection
spring.neo4j.uri=${NEO4J_URI:}
spring.neo4j.authentication.username=neo4j
spring.neo4j.authentication.password=<your-password>

# JWT Configuration
jwt.secret=<your-jwt-secret-key>  # Should use environment variable

# Eureka Service Discovery
eureka.client.serviceUrl.defaultZone=http://localhost:8761/eureka
eureka.instance.preferIpAddress=true
```

### Environment Variables

For production deployment, use environment variables:

```bash
export NEO4J_URI=neo4j+s://your-instance.databases.neo4j.io
export NEO4J_USERNAME=your-username
export NEO4J_PASSWORD=your-password
export JWT_SECRET=your-long-random-secret-key
```

---

## Technology Stack

- **Framework:** Spring Boot 3.5.7
- **Language:** Java 21
- **Database:** Neo4j Aura Cloud
- **Authentication:** JWT (JSON Web Tokens)
- **Password Encryption:** BCrypt
- **Service Discovery:** Netflix Eureka
- **Build Tool:** Maven
- **Testing:** JUnit 5, Mockito

---

## Future Enhancements

Potential features to add:

1. **Password Update**
   - Endpoint to change password
   - Require current password for verification
   - Validate new password complexity

2. **Password Reset**
   - Email-based password reset
   - Generate reset tokens
   - Verify reset codes

3. **Token Refresh**
   - Implement refresh token mechanism
   - Short-lived access tokens
   - Long-lived refresh tokens

4. **Two-Factor Authentication**
   - TOTP/2FA support
   - QR code generation
   - Authenticator app integration

5. **Rate Limiting**
   - Implement rate limiting filter
   - Prevent brute force attacks
   - Per-IP and per-user limits

6. **Account Management**
   - Update user profile
   - Delete account
   - Account status checking

---

## Troubleshooting

### Common Issues

**Issue:** "Email already in use" when registering
- **Solution:** User with that email already exists. Try a different email or login.

**Issue:** "Invalid email or password" on login
- **Solution:** Check email and password are correct. Ensure account was registered successfully.

**Issue:** Token expires quickly
- **Solution:** Tokens are valid for 3 days. Implement token refresh mechanism or re-login.

**Issue:** Connection refused errors
- **Solution:** Ensure Neo4j credentials are correct and Neo4j Aura instance is accessible.

---

## Contact

For issues or questions about the User Service API:
- Review code: `/backend/user-service/`
- Data Model: `/DATA_MODEL.md`
- Requirements: `/requirements.md`

---

## Changelog

### Version 1.0.0 (Feb 15, 2026)
- Initial release
- User registration endpoint
- User login endpoint with JWT authentication
- Neo4j integration
- Email validation
- Password encryption with BCrypt
- Removed role-based authentication (simplified to auth-only)
