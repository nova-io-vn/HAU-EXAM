package com.hau.exam.domain.model;import java.util.UUID;public record ExamQuestionReference(UUID id,UUID questionId,int position,UUID matrixRuleId){}
