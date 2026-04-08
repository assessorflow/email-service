# Email Service — Required Fixes

Gap analysis between reference docs (`pubsub.md`, `schema.md`, `overall.md`, `api_contract.md`) and current implementation.

**Source of truth:** `/Reference-Claude-Code-Instructions/References/`

---

## STEP 1 — MySQL → PostgreSQL

**Reference (schema.md):** Single PostgreSQL 18+ instance (Cloud SQL).

**Current code:** MySQL everywhere — `pom.xml` has `mysql-connector-j` + `flyway-mysql`, migration uses MySQL syntax (`BINARY(16)`, `InnoDB`, `utf8mb4_unicode_ci`), YAML configs point to `mysql://`.

**Fix:**
- Replace `mysql-connector-j` with `org.postgresql:postgresql` in `pom.xml`
- Replace `flyway-mysql` with `flyway-database-postgresql` in `pom.xml`
- Rewrite `V1__init_email_schema.sql` in PostgreSQL syntax: `UUID DEFAULT gen_random_uuid()`, `TIMESTAMPTZ`, no `InnoDB`/`utf8mb4`
- Update all `application-*.yml`: JDBC URLs from `mysql://` to `postgresql://`, driver class

**Files affected:**
- `pom.xml`
- `src/main/resources/db/migration/V1__init_email_schema.sql`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-docker.yml`
- `src/main/resources/application-prod.yml`

---

## STEP 2 — Entity: BINARY(16) → UUID, LocalDateTime → Instant

**Reference (schema.md):** `id` is `UUID` type (PostgreSQL native). All timestamps are `TIMESTAMPTZ`.

**Current code (`EmailLog.java`):** `@Column(columnDefinition = "BINARY(16)")` on id. Uses `LocalDateTime` for `sentAt` and `createdAt`.

**Fix:**
- Remove `@Column(columnDefinition = "BINARY(16)")` from `id` — JPA `GenerationType.UUID` works natively with PostgreSQL UUID
- Change `LocalDateTime sentAt` → `Instant sentAt`
- Change `LocalDateTime createdAt` → `Instant createdAt`

**Files affected:**
- `EmailLog.java`

---

## STEP 3 — Entity: `email_type` Enum Values Don't Match Schema

**Reference (schema.md):** `email_type` values are `assessor_review`, `participant_invitation`, `participant_report` (snake_case).

**Current code (`EmailLog.java`):** Enum values are `QUESTION_REVIEW`, `ASSESSMENT_LINK`, `PARTICIPANT_REPORT`.

**Mismatches:**
- `QUESTION_REVIEW` should be `ASSESSOR_REVIEW` (schema says `assessor_review`)
- `ASSESSMENT_LINK` should be `PARTICIPANT_INVITATION` (schema says `participant_invitation`)

**Fix:** Rename the enum values and add `@Column` value override to store as snake_case in DB:

```java
public enum EmailType {
    ASSESSOR_REVIEW,
    PARTICIPANT_INVITATION,
    PARTICIPANT_REPORT
}
```

Update all references in service classes (`EmailLog.EmailType.QUESTION_REVIEW` → `ASSESSOR_REVIEW`, `ASSESSMENT_LINK` → `PARTICIPANT_INVITATION`).

**Files affected:**
- `EmailLog.java`
- `AssessorReviewService.java`
- `AssessmentLinkService.java`

---

## STEP 4 — Pub/Sub Message Payloads Don't Match pubsub.md

**Reference (pubsub.md §5.25-5.28):** The request payloads use different field names than the current DTOs.

### Assessor Review Request (pubsub.md §5.25)

| Reference field | Current DTO field | Match? |
|----------------|-------------------|--------|
| `assessment_id` | (missing) | NO |
| `assessor_email` | `recipientEmail` | Rename needed |
| `review_link` | `reviewLink` | OK (camelCase in Java) |
| `question_count` | (missing) | NO |
| — | `workflowId` | Not in reference |
| — | `assessorName` | Not in reference |
| — | `assessmentName` | Not in reference |
| — | `subject` | Not in reference |
| — | `emailLogId` | Internal — not from publisher |

### Assessment Link Request (pubsub.md §5.26)

| Reference field | Current DTO field | Match? |
|----------------|-------------------|--------|
| `assessment_id` | (missing) | NO |
| `participant_email` | `recipientEmail` | Rename needed |
| `assessment_link` | `assessmentLink` | OK |
| `duration_minutes` | (missing) | NO |
| `deadline` | (missing) | NO |
| — | `workflowId` | Not in reference |
| — | `participantName` | Not in reference |
| — | `assessmentName` | Not in reference |

### Participant Report Request (pubsub.md §5.27)

| Reference field | Current DTO field | Match? |
|----------------|-------------------|--------|
| `assessment_id` | (missing) | NO |
| `participant_email` | `recipientEmail` | Rename needed |
| `report_id` | (missing) | NO |
| `report_link` | `reportLink` | OK |
| — | `workflowId` | Not in reference |
| — | `score` | Not in reference |

### Deliver Payload (pubsub.md §5.28)

**Reference says** the deliver stage receives pre-rendered HTML:
```json
{ "email_log_id": "uuid", "recipient_email": "...", "subject": "...", "template_id": "...", "rendered_html": "...", "rendered_text": "..." }
```

**Current code:** Re-passes the same request DTO to the deliver stage and renders the template there. The deliver stage should receive pre-rendered content, not raw template data.

**Fix:**
- Update request DTOs to match pubsub.md field names
- Add missing fields (`assessment_id`, `question_count`, `duration_minutes`, `deadline`, `report_id`)
- Create a separate `EmailDeliverEvent` DTO for the deliver stage with `email_log_id`, `recipient_email`, `subject`, `template_id`, `rendered_html`, `rendered_text`
- Update services: request stage renders template + publishes `EmailDeliverEvent`; deliver stage just sends the pre-rendered HTML

**Files affected:**
- `dto/assessor_review/AssessorReviewEvent.java` — rename/add fields
- `dto/assessment_link/AssessmentLinkEvent.java` — rename/add fields
- `dto/participant_report/ParticipantReportEvent.java` — rename/add fields
- New: `dto/EmailDeliverEvent.java`
- `AssessorReviewService.java` — render in request stage, publish deliver event
- `AssessmentLinkService.java` — same
- `ParticipantReportService.java` — same
- All 3 subscriber deliver handlers — deserialize `EmailDeliverEvent` instead

---

## STEP 5 — Email Sender: AWS SES → GCP-Native or SMTP

**Reference (overall.md):** The source of truth doesn't specify the email provider. The infra is fully GCP-based.

**Current code:** Uses **AWS SES v2** (`software.amazon.awssdk.services.sesv2`), which requires AWS credentials (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`).

**Decision needed:** This works but introduces a cross-cloud dependency. Options:
- **(a)** Keep AWS SES — it works, you already have it configured
- **(b)** Switch to SendGrid, Mailgun, or another provider via SMTP — simpler, no AWS SDK dependency
- **(c)** Switch to `spring-boot-starter-mail` with SMTP config — provider-agnostic

**Recommendation:** Keep AWS SES for now (option a). It works and the templates are already integrated. No code change needed unless the team decides to go GCP-only.

**No files affected** — decision only.

---

## STEP 6 — Jackson snake_case + ISO 8601 Timestamps

**Reference:** Pub/Sub payloads use `snake_case` field names (`assessment_id`, `participant_email`). All timestamps should be ISO 8601 UTC.

**Current code:** Jackson defaults to `camelCase`. No explicit date config.

**Fix:** Add to `application.yml`:
```yaml
spring:
  jackson:
    property-naming-strategy: SNAKE_CASE
    serialization:
      write-dates-as-timestamps: false
    time-zone: UTC
```

**Note:** Since DTOs are deserialized from Pub/Sub messages (published by Orchestrator), the field names in DTOs must match what the Orchestrator publishes. If the Orchestrator publishes `snake_case` JSON (which it should per pubsub.md), then this config ensures correct deserialization. If DTO field names are `camelCase` in Java, Jackson with `SNAKE_CASE` strategy will auto-map `assessment_id` → `assessmentId`.

**Files affected:**
- `src/main/resources/application.yml`

---

## STEP 7 — Missing Idempotency Check

**Reference (pubsub.md):** Pub/Sub delivers at-least-once. The email service could receive duplicate messages.

**Current code:** No duplicate detection. If the same `request` event is delivered twice, two `email_log` rows are created and two emails are sent.

**Fix:** Add idempotency using `assessment_id + recipient_email + email_type` as a composite unique constraint. Before creating a new `email_log` entry, check if one already exists with status `SENT` or `QUEUED`:

```java
if (emailLogRepository.existsByWorkflowIdAndRecipientEmailAndEmailType(
        event.getWorkflowId(), event.getRecipientEmail(), emailType)) {
    log.warn("Duplicate email request ignored: workflow={}, recipient={}", ...);
    return;
}
```

Also add a database-level unique constraint via migration:
```sql
CREATE UNIQUE INDEX uq_email_log_idempotency
ON email_log(workflow_id, recipient_email, email_type);
```

**Files affected:**
- `EmailLogRepository.java` — add `existsByWorkflowIdAndRecipientEmailAndEmailType` method
- `AssessorReviewService.java`, `AssessmentLinkService.java`, `ParticipantReportService.java` — add idempotency check
- New migration: `V2__add_email_idempotency_index.sql`

---

## STEP 8 — Dockerfile: Same Issue as Identity Service

**Current code:** Root `Dockerfile` uses multi-stage build with `artifact-registry-sa.json` hardcoded, and COPY paths assume parent directory context.

**Fix:** Delete root `Dockerfile`. Keep or create a `Dockerfile.ci` in `.github/workflows/` that expects a pre-built JAR (same pattern as identity-access-service).

**Files affected:**
- `Dockerfile` — delete
- `.github/workflows/Dockerfile.ci` — create if missing

---

## STEP 9 — Missing `assessment_id` Column in `email_log`

**Reference (pubsub.md §5.25-5.27):** All request payloads include `assessment_id`. This should be stored in the email log for traceability.

**Current code:** `email_log` only has `workflow_id`, not `assessment_id`.

**Fix:** Add `assessment_id UUID` column to `email_log` table. This enables querying email history by assessment.

**Files affected:**
- New migration: `V3__add_assessment_id_to_email_log.sql` (or fold into V1 rewrite since we're rewriting for PostgreSQL)
- `EmailLog.java` — add `assessmentId` field
- All 3 service classes — populate `assessmentId` from event

---

## STEP 10 — Update Tests

After all fixes:
- Update `EmailServiceApplicationTests.java` — may need test profile with H2 `MODE=PostgreSQL`
- Add unit tests for idempotency logic
- Add unit tests for template rendering
- Create `application-test.yml` (currently missing)

**Files affected:**
- New: `src/test/resources/application-test.yml`
- `EmailServiceApplicationTests.java`
- New test classes for service layer

---

## Summary — Execution Status

| Step | Fix | Status |
|------|-----|--------|
| 1 | MySQL → PostgreSQL (+ folded Steps 7 SQL + 9 SQL into V1 migration) | ✅ Done |
| 2 | Entity: UUID native + Instant timestamps | ✅ Done |
| 3 | email_type enum values: `ASSESSOR_REVIEW`, `PARTICIPANT_INVITATION` | ✅ Done |
| 4 | Pub/Sub DTOs match pubsub.md + separate `EmailDeliverEvent` + 2-stage render-then-send | ✅ Done |
| 5 | Email sender: keep AWS SES (decision only) | ✅ N/A |
| 6 | Jackson snake_case + ISO 8601 UTC | ✅ Done |
| 7 | Idempotency check: `existsByWorkflowIdAndRecipientEmailAndEmailType` + unique index | ✅ Done |
| 8 | Deleted root Dockerfile | ✅ Done |
| 9 | `assessment_id` column + entity field + populated in all 3 services | ✅ Done |
| 10 | Tests: `application-test.yml` + 3 service tests (success + duplicate) | ✅ Done |