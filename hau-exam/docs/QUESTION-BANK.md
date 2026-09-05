# Question Bank UI

- `/questions`: question list within the backend actor scope.
- `/questions/mine`: USER-owned questions, queried with `createdBy`.
- `/questions/new`: manual question creation.
- `/questions/:id`: details, review history and action confirmation.
- `/questions/:id/edit`: owner editing for DRAFT / NEED_REVISION.
- Preview is available in a drawer from the list and editor.

All requests use the shared API Gateway client. Filters use `facultyId`,
`subjectId`, `chapterId`, `topicId`, `difficulty`, `status`, `source`,
`keyword`, `createdBy`, `page` and `size`.

The editor maps answer selection to `options[].correct` and `sortOrder`.
It preserves existing question and option storage keys. Source and status
are read-only response metadata and are never included in save payloads.
Catalog names are resolved from catalog APIs; IDs remain visible when a
catalog cannot be resolved.

Backend constraints:

- `facultyId`, `subjectId` and `chapterId` are required.
- Update currently changes content/options/type/difficulty/images only;
  subject, chapter and topic controls are disabled while editing.
- There is no explanation field or image upload endpoint in the current
  question contract. The UI explains the explanation limitation and
  accepts optional image URLs.
- NEED_REVISION must first be updated through PUT (backend returns DRAFT),
  then submitted through POST `/submit`. “Save and resubmit” performs these
  requests sequentially. No client-side status transition is performed.
- Approved questions can be archived by the owner or a SUBJECT_ADMIN in
  the same faculty. The backend remains authoritative for every action.
- USER search is always owner-scoped by the backend, including `/questions`.

Verification:

```sh
node --test src/features/questions/model/questionModel.test.js
npm run lint
npm run build
```
