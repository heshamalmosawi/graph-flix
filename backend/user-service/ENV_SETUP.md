# User Service - Environment Variables Setup

## Security Notice

⚠️ **IMPORTANT:** Never commit secrets or credentials to Git!

This repository uses environment variables to protect sensitive information:
- Neo4j database credentials
- JWT signing secret

---

## Setup Instructions

### 1. Create Environment File

Copy the provided environment template:

```bash
cp /tmp/user-service.env backend/user-service/.env
```

### 2. Load Environment Variables

**Option A: Source the file manually**
```bash
cd backend/user-service
source .env
mvn spring-boot:run
```

**Option B: Use dotenv in IDE**
- Install an IDE plugin that supports `.env` files
- IDE will automatically load variables

**Option C: Set in OS environment**
```bash
export NEO4J_URI=neo4j+s://<your-instance-id>.databases.neo4j.io
export NEO4J_USERNAME=neo4j
export NEO4J_PASSWORD=<your-password>
export JWT_SECRET=<your-long-random-secret-key>
```

### 3. Run the Application

```bash
cd backend/user-service
mvn spring-boot:run
```

The application will automatically read the environment variables.

---

## Environment Variables

### Neo4j Database

| Variable          | Description                                  | Example                                              |
|-------------------|----------------------------------------------|------------------------------------------------------|
| NEO4J_URI         | Neo4j Aura Cloud connection URI            | `neo4j+s://<your-instance-id>.databases.neo4j.io` |
| NEO4J_USERNAME    | Neo4j username                               | `neo4j`                                           |
| NEO4J_PASSWORD    | Neo4j password (from .neo4j-creds)           | `<your-password>`                                    |

### JWT Authentication

| Variable   | Description                    | Example                                              |
|-----------|-------------------------------|------------------------------------------------------|
| JWT_SECRET | Secret key for signing JWTs | `<long-random-secret-key>`                              |

---

## .gitignore Protection

The following files are protected by `.gitignore` to prevent accidental commits:

```
.env              # Environment variables file
.env.local        # Local environment overrides
.neo4j-creds      # Neo4j credentials file
opencode.json     # IDE configuration with secrets
```

---

## Production Deployment

For production, use environment variable management:

### Option 1: Kubernetes/Docker
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: user-service-config
data:
  NEO4J_URI: "neo4j+s://production.databases.neo4j.io"
  NEO4J_USERNAME: "production-user"
  NEO4J_PASSWORD: "production-password"
  JWT_SECRET: "production-jwt-secret"
```

### Option 2: AWS Secrets Manager
```bash
aws secretsmanager get-secret-value --secret-id /prod/userservice/neo4j-password
aws secretsmanager get-secret-value --secret-id /prod/userservice/jwt-secret
```

### Option 3: Vault
```bash
vault kv get -mount=secret userservice neo4j-password
vault kv get -mount=secret userservice jwt-secret
```

---

## Verification

Check that environment variables are set:

```bash
# Check if variables are loaded
echo "NEO4J_URI: $NEO4J_URI"
echo "NEO4J_USERNAME: $NEO4J_USERNAME"
echo "JWT_SECRET: $JWT_SECRET"
```

Should see values (or empty if not set).

---

## Troubleshooting

**Issue:** "Neo4j connection failed"
- **Solution:** Check that NEO4J_URI, NEO4J_USERNAME, and NEO4J_PASSWORD are set correctly
- **Solution:** Verify Neo4j Aura Cloud instance is accessible
- **Solution:** Ensure credentials are correct (check .neo4j-creds file)

**Issue:** "JWT secret not set"
- **Solution:** Set JWT_SECRET environment variable
- **Solution:** Ensure .env file is sourced before running the application

**Issue:** Application can't find .env file
- **Solution:** Ensure .env file is in the user-service directory
- **Solution:** Run `source .env` from the backend/user-service directory

---

## Best Practices

1. **Never commit .env files** - Always in .gitignore
2. **Use strong secrets** - Minimum 32 characters for JWT_SECRET
3. **Rotate secrets regularly** - Change passwords and secrets periodically
4. **Use secret management** - For production, use proper secret managers
5. **Limit access** - Only give credentials to those who need them
6. **Audit access** - Monitor who accesses credentials

---

## Quick Start

```bash
# Navigate to user service
cd backend/user-service

# Source environment variables
source .env

# Start the service
mvn spring-boot:run
```

The service will now connect to Neo4j using credentials from environment variables.
