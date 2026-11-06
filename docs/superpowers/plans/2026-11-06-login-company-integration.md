# Linketinder Login and Company Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Integrate company registration and basic candidate/company login across the existing Groovy API and TypeScript frontend.

**Architecture:** Keep shared credentials in `User`; infer the current profile from `Candidate` or `Company`. Add a password-verifying login endpoint, a safe case-insensitive email unique index, and frontend account/menu state held only in memory.

**Tech Stack:** Groovy, JDK HttpServer, JDBC, PostgreSQL/pgcrypto, Spock, TypeScript, Vite, Vitest.

**Spec:** `docs/superpowers/specs/2026-11-06-auth-and-company-registration-design.md`

## Global Constraints

- No web framework.
- Never return or log passwords or password hashes.
- Do not add Recruiter yet; keep profile resolution straightforward to extend.
- No session/token or server-side authorization is included in this scope.
- Preserve already-pending candidate-delete changes in both repositories.
- Only apply the email index after confirming zero normalized duplicates.

## Review Focus

- Duplicate emails differing only by case/outer whitespace must be rejected across both profile types.
- Invalid credentials must not disclose which credential failed.
- A user without a recognized profile must not receive a successful login.
- Company registration must not silently fall back to localStorage.
- Reload/logout must not retain credentials or claim a protected server session.

---

### Task 1: Email uniqueness and duplicate registration responses

**Files:**
- Modify: `sql/Linketinder.sql`
- Create: `sql/migrations/V1__unique_user_email_normalized.sql`
- Modify: `app/src/main/groovy/org/linketinder/dao/user/CandidateDAO.groovy`
- Modify: `app/src/main/groovy/org/linketinder/dao/user/CompanyDAO.groovy`
- Create/modify: registration exception, handlers, and Spock tests.
- Test: integration test proving a case/whitespace email collision is rejected.

- [x] Add failing tests for duplicate-conflict HTTP behavior and database uniqueness.
- [x] Run them and confirm the expected failures.
- [x] Add normalized unique index and map unique-registration conflicts to HTTP 409.
- [x] Apply migration only after the zero-collision preflight.
- [x] Run relevant backend tests.

### Task 2: Backend login endpoint

**Files:**
- Create: `AuthDAO`, `AuthController`, `AuthHandler` and focused tests.
- Modify: `Router`, `ApiApp`, `RouterTest`, `CorsFilterTest`, `requests.http`, README.

- [x] Test valid login, generic 401, malformed/missing JSON fields, and CORS POST registration.
- [x] Verify those tests fail before implementation.
- [x] Implement normalized-email lookup and `crypt` verification; return only ID/name/profile type.
- [x] Run handler, router, and database tests.

### Task 3: Frontend company registration and login UI

**Files:**
- Create: login request/response types, `LoginService`, `LoginController`, login page/form.
- Modify: `CompanyService`, `CompanyController`, `EntityInputs`, `UserForm`, `Header`, `renderAll`, README.
- Test: Vitest coverage for company POST, login response/error mapping, and controller behavior.

- [x] Add failing service/controller tests first and verify RED.
- [x] Wire company form to API and include a six-character-minimum password.
- [x] Add login submission, account name/type display, role-specific menu, and in-memory logout.
- [x] Run all frontend tests and production build.

### Task 4: Cross-repository verification

- [x] Create `docs/integracao-frontend-backend.md` with both applications, request/data flows, examples, and current limitations; link it from both READMEs.
- [x] Run backend `./gradlew check` and frontend `npm run test:run` plus `npm run build`.
- [x] Review `git diff --check`, API request examples, and README claims.
- [x] Report whether the email index migration was applied and distinguish login UI from server-side authorization.
