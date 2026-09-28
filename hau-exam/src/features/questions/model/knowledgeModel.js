export function chaptersForSubject(chapters = [], subjectId) {
  return chapters.filter((chapter) => chapter.subjectId === subjectId);
}

export function topicsForChapter(topics = [], chapterId) {
  return topics.filter((topic) => topic.chapterId === chapterId);
}

export function belongsToSelection(subject, chapter, topic) {
  if (!subject) return false;
  if (!chapter) return !topic;
  if (chapter.subjectId !== subject.id) return false;
  return !topic || topic.chapterId === chapter.id;
}
