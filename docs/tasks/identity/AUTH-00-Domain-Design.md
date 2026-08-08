# AUTH-00 - Identity Domain Design

## 1. Purpose

This document defines the domain model for the Identity module of WorkForge. It
is the domain-design baseline for the authentication work that follows,
including user registration (`AUTH-01`).

The design follows the existing modular-monolith, Clean Architecture, and
Domain-Driven Design decisions. The Identity module owns global user accounts
and authentication context; it does not own tenant membership or
organization-specific permissions.

## 2. Bounded Context

### 2.1 Identity owns

- The `User` aggregate root.
- User account lifecycle and login eligibility.
- Global account identity: email address, full name, and user identifier.
- The stored password hash.
- System-level roles.
- Domain validation and identity-specific domain errors.
- The `UserRepository` domain contract.

### 2.2 Identity does not own

- Organizations or tenants.
- Organization membership or organization-scoped roles.
- Workspace, project, or team membership.
- Audit-log persistence.
- Notification delivery.
- Password hashing algorithms, JWT issuance, or HTTP concerns.

Other modules may reference a `UserId`, but must not modify the `User`
aggregate directly.

## 3. Aggregate Model

`User` is the sole aggregate root in this bounded context.

| Attribute | Type | Domain meaning |
| --- | --- | --- |
| `id` | `UserId` | Stable identity of the account |
| `email` | `Email` | Normalized, validated login identifier |
| `password` | `Password` | Already-hashed credential |
| `fullName` | `String` | Display name, trimmed and limited to 200 characters |
| `status` | `UserStatus` | Account lifecycle state |
| `role` | `RoleType` | Global system role |
| `emailVerified` | `boolean` | Whether email ownership was verified |
| `emailVerifiedAt` | `Instant` | Verification time, when verified |
| `lastLoginAt` | `Instant` | Most recent successful login |
| `passwordChangedAt` | `Instant` | Most recent password change |
| `createdAt` | `Instant` | Account creation time |
| `updatedAt` | `Instant` | Last aggregate mutation time |

The aggregate is identified by `UserId`; equality is identity-based rather
than based on mutable account attributes.

## 4. Value Objects

### 4.1 UserId

`UserId` wraps a non-null UUID. It can be generated for a new account or
constructed from a UUID/string when restoring an existing account. Invalid,
blank, or unparsable values raise `InvalidUserIdException`.

### 4.2 Email

`Email` is immutable and normalizes input by trimming whitespace and converting
it to lowercase using the root locale. It rejects null, blank, overlong
(greater than 254 characters), and syntactically invalid values through
`InvalidEmailException`.

Email equality is value equality after normalization. Uniqueness is a
repository/database invariant in addition to the aggregate value-object
invariant.

### 4.3 Password

`Password` contains only the stored hash. It must never expose or accept a
plain-text password as a domain credential. It rejects null, blank,
whitespace-padded, shorter-than-8, or longer-than-255 values through
`InvalidPasswordException`.

Hashing is performed outside the domain through the application outbound
`PasswordEncoder` port. The domain compares password values only when applying
the change-password rule; raw credential verification remains an application
and infrastructure concern.

## 5. Enumerations

### 5.1 UserStatus

The account states are:

- `PENDING_VERIFICATION` - newly registered or awaiting verification after an
  email change.
- `ACTIVE` - verified and eligible for authenticated operations.
- `SUSPENDED` - administratively disabled.
- `LOCKED` - temporarily or administratively locked.
- `DELETED` - terminal account state.

### 5.2 RoleType

The global roles are:

- `SYSTEM_ADMIN` - platform-level administration.
- `TENANT_ADMIN` - system role associated with tenant administration.
- `USER` - standard account role.

Public registration always assigns `RoleType.USER`; a client-provided role is
not part of the registration command.

## 6. Lifecycle and Invariants

### 6.1 Registration

`User.register(...)` creates a new account with:

- `PENDING_VERIFICATION` status.
- `emailVerified == false`.
- `emailVerifiedAt == null`.
- `createdAt == updatedAt == now`.
- The role supplied by the trusted application service.

The application service must normalize the email, check repository uniqueness,
encode the plain password through `PasswordEncoder`, and then construct the
aggregate. The database must also enforce a unique email constraint to prevent
concurrent registration races.

### 6.2 Verification and activation

- `verifyEmail(now)` rejects deleted or locked users and repeated verification.
- Verification sets the verification flag and timestamp.
- Verification moves a pending account to `ACTIVE`.
- `activate(now)` requires a verified email and rejects deleted or already active
  users.

### 6.3 Suspension, locking, and deletion

- `suspend(now)` requires a non-deleted user and rejects an already suspended
  user.
- `lock(now)` requires a non-deleted user and rejects an already locked user.
- `unlock(now)` restores `ACTIVE` for verified users, otherwise
  `PENDING_VERIFICATION`.
- `delete(now)` is terminal and rejects an already deleted user.
- Mutating operations update `updatedAt` using the operation timestamp.

### 6.4 Login

`canLogin()` returns true only when the account is both `ACTIVE` and
email-verified. `login(now)` enforces the same active/verified requirements and
records `lastLoginAt`.

JWT creation, token validation, session renewal, rate limiting, and failed
attempt policy are outside this aggregate.

### 6.5 Password changes

- `changePassword(...)` requires an active, verified user.
- The current password must match the stored value.
- The new password must differ from the current value.
- `resetPassword(...)` rejects deleted users and also requires a different value.
- Successful changes set `passwordChangedAt` and `updatedAt`.

The application layer is responsible for comparing a plain password with the
stored hash through the password encoder before passing domain values to a
change operation.

### 6.6 Profile and email changes

- `updateProfile(...)` requires an active user, validates the trimmed name, and
  rejects an unchanged value.
- `changeEmail(...)` rejects deleted users and unchanged values.
- Changing email clears verification, clears its timestamp, and returns the
  account to `PENDING_VERIFICATION`.

## 7. Domain Error Model

Identity-specific validation and lookup errors use `DomainException` and
`IdentityErrorCode`:

| Code | Meaning |
| --- | --- |
| `IDENTITY_001` | Invalid user identifier |
| `IDENTITY_002` | Invalid email |
| `IDENTITY_003` | Invalid password/hash |
| `IDENTITY_004` | User already exists |
| `IDENTITY_005` | User not found |

Domain exceptions contain domain codes and messages only. HTTP status mapping
belongs to the application/interface boundary and must not leak into the
domain.

Illegal lifecycle transitions use the aggregate's domain preconditions. They
must be translated by the outer error boundary consistently with the existing
global exception handling policy.

## 8. Repository Contract

The domain depends on `UserRepository`, not on Spring Data or JPA. Its
operations are:

- `save(User)`
- `findById(UserId)`
- `findByEmail(Email)`
- `existsByEmail(Email)`
- `delete(User)`

Infrastructure implements this contract with persistence entities and a mapper.
The domain aggregate remains free of persistence annotations. Repository
implementations must reconstitute persisted state without applying
registration-only defaults.

## 9. Registration Boundary

The application registration command contains only:

- `email`
- `plainPassword`
- `fullName`

The registration flow is:

1. Presentation validates the request shape.
2. The application service creates an `Email` value object.
3. The repository checks for an existing email.
4. The outbound password encoder hashes the plain password.
5. The service creates a `User` with a generated `UserId` and `USER` role.
6. The repository saves the aggregate.
7. The result exposes the user identity and public account state, never the
   password or hash.

Email verification dispatch is an integration concern and may be published as
an event after successful persistence. Token storage and failed-attempt
lockout policy remain infrastructure/application decisions.

## 10. Boundary and Dependency Rules

```text
Presentation -> Application -> Domain
Infrastructure -> Application ports and Domain repository contract
Domain -> Java standard library and Identity domain types only
```

In particular:

- Domain classes must not import Spring, JPA, Spring Security, JWT libraries,
  HTTP types, or database types.
- Application services orchestrate repositories, encoders, transactions, and
  use-case results.
- Infrastructure owns BCrypt, JWT, persistence mapping, and framework
  configuration.
- Organization and member modules exchange stable identifiers and explicit
  contracts rather than aggregate internals.

## 11. Persistence Mapping

The logical `users` table is expected to contain:

| Column | Constraint |
| --- | --- |
| `id` | UUID primary key |
| `email` | Unique, not null, maximum 254 characters |
| `password_hash` | Not null, maximum 255 characters |
| `full_name` | Not null, maximum 200 characters |
| `status` | Not null enum representation |
| `role` | Not null enum representation |
| `email_verified` | Not null, default false |
| `email_verified_at` | Nullable timestamp |
| `last_login_at` | Nullable timestamp |
| `password_changed_at` | Nullable timestamp |
| `created_at` | Not null timestamp |
| `updated_at` | Not null timestamp |

The persistence model and mapper are infrastructure artifacts. They must
preserve all aggregate state, including lifecycle timestamps, and must not
expose `password_hash` in API responses.

## 12. Related Documents

- `docs/architecture/modules/identity/Identity-Architecture-Design.md`
- `docs/architecture/quality-attributes/Functional-Requirements.md`
- `docs/architecture/quality-attributes/Non-Functional-Requirements.md`
- `docs/ADR/ADR-0002-Architecture-Style.md`
- `docs/ADR/ADR-0003-Domain-Driven-Design.md`
- `docs/project/Scope.md`

Implementation references:

- `backend/src/main/java/io/github/tphung1724/workforge/identity/domain/aggregate/User.java`
- `backend/src/main/java/io/github/tphung1724/workforge/identity/domain/valueobject/UserId.java`
- `backend/src/main/java/io/github/tphung1724/workforge/identity/domain/valueobject/Email.java`
- `backend/src/main/java/io/github/tphung1724/workforge/identity/domain/valueobject/Password.java`
- `backend/src/main/java/io/github/tphung1724/workforge/identity/domain/enums/UserStatus.java`
- `backend/src/main/java/io/github/tphung1724/workforge/identity/domain/enums/RoleType.java`
- `backend/src/main/java/io/github/tphung1724/workforge/identity/domain/repository/UserRepository.java`

## 13. Acceptance Criteria

- The aggregate root and its invariants are explicitly documented.
- Email, password, and identifier validation rules are unambiguous.
- Lifecycle transitions and login eligibility are defined.
- Public registration cannot elevate its role.
- Domain, application, infrastructure, and organization boundaries are clear.
- Persistence and error handling rules preserve domain purity.
- No source-code changes are required by this design artifact.
