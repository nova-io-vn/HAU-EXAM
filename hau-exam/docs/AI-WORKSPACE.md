# AI Workspace

Routes: `/ai/documents`, `/ai/generate`, `/ai/jobs`, `/ai/jobs/:id`,
`/ai/analysis`, `/chat`. Existing role visibility remains: document upload/chat
are USER screens; generation, analysis and jobs support USER/SUBJECT_ADMIN.
All HTTP calls use the shared Gateway client.

Upload validates MIME, size and UTF-8 before sending.
`VITE_AI_DOCUMENT_MAX_SIZE_BYTES` defaults to 10485760; align with server limits
when deploying. Server multipart limits remain authoritative. No fake progress.
Document status means persisted upload, not an invented extraction state.

Generation supports count 1–100, optional difficulty/topic UUID. Analysis uses a
freeform request because backend defines no enum. Commands return 202 jobs and
guard duplicate clicks. Detail polls sequentially every 3 seconds for active
jobs; list refreshes every 5 seconds while it contains active jobs. Timers stop
on unmount, terminal status or error. Manual reload never creates another job.

Result preview includes question/options/correct answer/explanation/difficulty/
topic and never marks questions APPROVED. Analysis displays returned JSON. Chat
shows answer/references when present, renders plain text, and performs no vector
DB logic. Conversation is local to the mounted page; persistent results remain
in Jobs. Previous turns are not implicitly sent as context.

Added backend read contracts are documented in ai-service/docs/API.md.

Checks: `npm run lint`, `npm run build`, and
`node --test src/features/ai/model/*.test.js src/features/questions/model/*.test.js`.
Four AiWorkspaceServiceTest cases verify owner checks, completed-only result reads,
and bounded owner-scoped pagination. New Java classes compile and these tests pass
against cached dependencies. Normal Maven remains blocked by the existing parent
POM mismatch; full Spring integration/live provider/browser checks remain pending.
