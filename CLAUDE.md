# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

`multi-tenant-jwt-auth-starter` (`io.linc:multi-tenant-jwt-auth-starter`) is a **Spring Boot auto-configuration starter library** — not a runnable service. It is included as a Maven dependency by other io-linc microservices to provide shared infrastructure with zero application code changes required.

Current version: `1.1.1-SNAPSHOT` | Java 17 | Spring Boot 3.1.5

## Build Commands

```bash
mvn clean package          # compile and package JAR + sources JAR
mvn clean install          # install to local Maven repository (use before testing in a downstream service)
mvn test                   # run all tests
mvn test -Dtest=ClassName  # run a single test class
mvn deploy                 # deploy to GitHub Packages (needs settings.xml server id github + write:packages token)
```

## Architecture Overview

### Auto-Configuration Bootstrap

The library activates via `src/main/resources/META-INF/spring.factories` (Spring Boot 2.x style). Two classes are registered as auto-configurations:

- `io.linc.common.auth.config.SecurityConfig` — Spring Security filter chain (JWT, stateless sessions, public URL rules)
- `io.linc.common.common.CommonLibraryAutoConfiguration` — everything else (AOP aspects, HTTP interceptors, DB config, JPA auditing, logging)

### Package Structure

Two top-level packages under `io.linc.common`:

**`auth/`** — Authentication and identity domain
- `security/` — `JwtTokenProvider` (validates/parses JWT), `JwtAuthenticationFilter` (per-request filter), `MDCLoggingFilter`
- `config/` — `SecurityConfig`, `WebConfig` (CORS), `MultiTenantAuthProperties`
- `entity/` — JPA entities: `User`, `Tenant`, `Role`, `Permission`, `RefreshToken`, `UserOtp`, `SsoSession`, etc.
- `repository/` — Spring Data JPA repos for auth entities
- `service/` — `EmailService`, `OtpService`, `UserService`, `SsoService`, `TenantFilterService`
- `controller/` — `OtpController`, `SignUpController`, `SsoController`
- `util/` — `AuthenticationUtils` (static helper API for downstream services)

**`common/`** — Cross-cutting infrastructure
- `aop/` — `@Audited` and `@LogPerformance` annotations + their aspects; `ExceptionLoggingAspect` (auto-applied to `@Service`/`@Repository`/`@Controller`)
- `db/` — `DatabaseConfiguration` (multi-datasource Tomcat JDBC pool), `DatabaseProperties`
- `entity/` — `AuditableEntity` (abstract base class — extend this for automatic `createdAt/By`, `updatedAt/By`)
- `http/exception/` — `GlobalExceptionHandler` (`@ControllerAdvice`) + `ErrorResponse` (standardized error JSON)
- `http/interceptor/` — `CorrelationIdInterceptor`, `RequestMetricsInterceptor`, `SecurityTokenInterceptor`
- `logging/` — `LoggingConfiguration`, `LoggingProperties`

### Key Design Decisions

**JWT is validate-only**: This library never issues tokens. Token generation is the responsibility of the central IAM service (`io-linc-iam-account`). `JwtTokenProvider` only validates signatures (HS256) and parses claims.

**Session cache**: `JwtTokenProvider` maintains an in-memory `ConcurrentHashMap` keyed by username. It caches `sessionId`, `permissions`, `role`, `tenantId`, `userId` to avoid re-parsing the JWT on every request. The filter validates the token's `sessionId` matches the cached value.

**Multi-tenancy via Hibernate filter**: `TenantFilterService` applies a named Hibernate filter (`tenantFilter`) to enforce row-level tenant isolation. Downstream entity classes must declare `@FilterDef` and `@Filter` annotations themselves.

**Public URLs**: `/api/auth/**` and `/actuator/**` bypass JWT authentication. All other endpoints require a valid Bearer token.

### How Downstream Services Use This Library

1. Add the Maven dependency and GitHub Packages repository (see `docs/COMMON_LIBRARY_OVERVIEW.md` §14). Authenticate with `docs/maven-settings.xml.example` (local PAT) or org secret `IO_LINC_COMMON_PACKAGE_TOKEN` in CI.
2. Configure required properties (see `src/main/resources/application.properties.example`).
3. Extend `AuditableEntity` for any JPA entity that needs audit columns.
4. Call `AuthenticationUtils.getTenantId()`, `getUsername()`, or `getTokenDetails()` to access auth context — no injection needed.
5. Annotate service methods with `@LogPerformance` or `@Audited(action = "...")` to activate AOP logging.

### Configuration

All properties are prefixed with `common.*` or `auth.*`. Master switches:
- `common.enabled: true` — disable the entire library
- `common.db.enabled: true` — disable multi-datasource setup
- `common.exception.handler-enabled: true` — disable global exception handler
- `common.aop.performance.enabled / audit.enabled / exception.enabled` — per-aspect toggles

See `src/main/resources/application.yml` for defaults and `docs/COMMON_LIBRARY_OVERVIEW.md` for the full configuration reference with sequence diagrams.

## Database Tables

All entities live in `src/main/java/io/linc/common/auth/entity/`. All tables except `tenant_master` extend `AuditableEntity` and automatically carry `created_at`, `created_by`, `updated_at`, `updated_by` columns.

**Core Auth**

| Table | Entity | Purpose |
|-------|--------|---------|
| `users` | `User` | User accounts; credentials, MFA flags, email/phone verification |
| `tenants` | `Tenant` | Runtime tenant records |
| `tenant_master` | `TenantMaster` | Tenant registration data (name, code, contact info, plan type); manages its own audit columns via `@PrePersist`/`@PreUpdate` |
| `roles` | `Role` | Role definitions scoped per tenant |
| `permissions` | `Permission` | Permission codes with optional description |
| `applications` | `Application` | Applications registered per tenant |
| `domains` | `Domain` | Domain definitions per tenant |

**Associations**

| Table | Entity | Purpose |
|-------|--------|---------|
| `user_roles` | `UserRole` | Many-to-many: users ↔ roles |
| `role_domain_permissions` | `RoleDomainPermission` | Three-way mapping: role + domain + permission |

**Sessions & Tokens**

| Table | Entity | Purpose |
|-------|--------|---------|
| `refresh_tokens` | `RefreshToken` | JWT refresh tokens with expiry |
| `refresh_tokens_roles` | (`@ElementCollection` on `RefreshToken`) | Roles embedded in a refresh token |
| `refresh_tokens_apps` | (`@ElementCollection` on `RefreshToken`) | Apps embedded in a refresh token |
| `user_otps` | `UserOtp` | OTP codes for EMAIL and SMS flows |
| `sso_sessions` | `SsoSession` | SSO session tracking with client ID and last access |
