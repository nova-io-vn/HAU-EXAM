import assert from "node:assert/strict";
import test from "node:test";
import { belongsToSelection, chaptersForSubject, topicsForChapter } from "./knowledgeModel.js";

test("chapters are rendered only under their selected subject", () => {
  const chapters = [{ id: "c-ai", subjectId: "ai" }, { id: "c-java", subjectId: "java" }];
  assert.deepEqual(chaptersForSubject(chapters, "ai"), [{ id: "c-ai", subjectId: "ai" }]);
});

test("topics are rendered only under their selected chapter", () => {
  const topics = [{ id: "t-ai", chapterId: "c-ai" }, { id: "t-java", chapterId: "c-java" }];
  assert.deepEqual(topicsForChapter(topics, "c-ai"), [{ id: "t-ai", chapterId: "c-ai" }]);
});

test("right panel rejects stale chapter and topic metadata", () => {
  assert.equal(belongsToSelection({ id: "ai" }, { id: "c-java", subjectId: "java" }, null), false);
  assert.equal(belongsToSelection({ id: "ai" }, { id: "c-ai", subjectId: "ai" }, { id: "t-java", chapterId: "c-java" }), false);
  assert.equal(belongsToSelection({ id: "ai" }, { id: "c-ai", subjectId: "ai" }, { id: "t-ai", chapterId: "c-ai" }), true);
});
