# Candidate Delete Implementation Plan

> **For agentic workers:** Follow this plan task-by-task with TDD.

**Goal:** Let the frontend delete a persisted candidate through the backend API.

**Architecture:** Reuse the existing CandidateDAO and CandidatoController removal methods. Add DELETE routing and candidate-only CORS permission, then expose a frontend service/controller operation and connect the management page to it.

**Tech Stack:** Groovy, JDK HttpServer, Spock, TypeScript, Vitest, Vite.

**Spec:** `docs/superpowers/specs/2026-11-06-candidate-delete-design.md`

## Global Constraints

- Do not implement candidate editing, company/job deletion, authentication, or schema changes.
- Keep deletion by ID at `DELETE /api/candidatos/{id}`.
- Send no response body for a successful `204`.
- Keep the candidate visible in the UI unless the API confirms deletion.

## Review Focus

- Invalid and missing IDs must not delete a record; cover in CandidateHandler tests.
- Deleting the collection URL without an ID must remain disallowed; cover in CandidateHandler tests.
- A failed HTTP response or network error must not remove the card; cover service behavior and review the UI branch.
- Cross-origin preflight must allow DELETE for candidates, but not for companies/vacancies; cover the configured filter behavior.
- A successful 204 response must contain no response entity; cover exact status/body length in the handler test.

---

### Task 1: Backend DELETE endpoint and CORS

**Files:**
- Modify `app/src/main/groovy/org/linketinder/http/handler/CandidateHandler.groovy`
- Modify `app/src/main/groovy/org/linketinder/http/Router.groovy`
- Test `app/src/test/groovy/org/linketinder/http/handler/CandidateHandlerTest.groovy`
- Test `app/src/test/groovy/org/linketinder/http/CorsFilterTest.groovy`
- Test `app/src/test/groovy/org/linketinder/http/RouterTest.groovy`
- Modify `requests.http`

**Interfaces:**
- Consumes `CandidatoController.buscarCandidatoPorId(int)` and `removerCandidato(int)`.
- Supports `DELETE /api/candidatos/{id}` only; collection DELETE remains 405.
- Returns 204 with no entity on success, 404 when lookup returns null, 500 when removal returns false after a successful lookup, and 400 for malformed IDs.
- Candidate CORS allows `GET, POST, DELETE`; company and vacancy methods remain `GET, POST`.

- [x] Add tests first for success, missing candidate, failed removal, invalid ID, and collection DELETE.
- [x] Add CORS/Router tests for candidate DELETE preflight and ensure company/vacancy do not gain DELETE.
- [x] Run the focused tests and observe the expected failures.
- [x] Implement the route handling and candidate-only CORS method list.
- [x] Add request examples and run the complete backend test suite.

### Task 2: Frontend candidate deletion

**Files:**
- Modify `src/services/CandidateService.ts`
- Modify `src/controllers/CandidateController.ts`
- Modify `src/renderAll.ts`
- Modify `src/view/pages/CandidateFeedPage.ts`
- Test `tests/services/CandidateService.test.ts`

**Interfaces:**
- `CandidateService.excluirCandidato(id: string): Promise<boolean>` sends DELETE to `${url}/${id}` and returns whether the HTTP response is successful.
- `CandidateController.excluirCandidato(id: string): Promise<boolean>` delegates to the service and reports network errors as failure.
- `CandidateFeedPage` receives the deletion callback from `renderAll`; only remote management uses it.

- [x] Add service tests for 204, HTTP error, and network failure; run and observe expected failures.
- [x] Implement service/controller delegation.
- [x] Connect the remote management delete action: ask for confirmation, await API success, then reload the list; on failure, preserve the list and show an error.
- [x] Run frontend unit tests and production build.
