# Multi-tenant JWT Authentication Starter

A Spring Boot starter for implementing multi-tenant JWT authentication with refresh token support.

## Features

- JWT-based authentication with tenant ID enforcement
- Refresh token mechanism
- Configurable token expiration
- Database-backed refresh token storage
- Spring Security integration
- Auto-configuration support

## Usage

1. Add the GitHub Packages Maven repository (and authenticate server `github` with a PAT that has `read:packages`):

```xml
<repository>
    <id>github</id>
    <url>https://maven.pkg.github.com/Clairlabs-AI/io-linc-common</url>
    <snapshots>
        <enabled>true</enabled>
    </snapshots>
</repository>
```

In `~/.m2/settings.xml`:

```xml
<server>
    <id>github</id>
    <username>YOUR_GITHUB_USERNAME</username>
    <password>YOUR_GITHUB_PAT</password>
</server>
```

Then add the dependency:

```xml
<dependency>
    <groupId>com.medgenome</groupId>
    <artifactId>multi-tenant-jwt-auth-starter</artifactId>
    <version>1.1.1-SNAPSHOT</version>
</dependency>
```

Internal Nexus remain the default deploy target in this project's `pom.xml`. A copy of the same JAR is published to GitHub Packages from [Clairlabs-AI/io-linc-common](https://github.com/Clairlabs-AI/io-linc-common).

2. Configure the properties in your application.properties/yaml:

```properties
auth.jwt.secret-key=yourSecretKey
auth.jwt.access-token-validity-minutes=15
auth.jwt.refresh-token-validity-days=7
auth.jwt.issuer=your-application
```

3. Implement UserDetailsService for your user management system.

4. The following endpoints will be available:

- POST /api/auth/login
- POST /api/auth/refresh

## API Documentation

### Login

POST /api/auth/login
```json
{
    "username": "user@example.com",
    "password": "password123",
    "tenantId": "tenant1"
}
```

### Refresh Token

POST /api/auth/refresh
```json
{
    "refreshToken": "uuid-refresh-token"
}
```

## Security

- Access tokens expire after 15 minutes (configurable)
- Refresh tokens expire after 7 days (configurable)
- Tenant ID is enforced in all tokens
- Passwords are encrypted using BCrypt
- Refresh tokens are stored in the database