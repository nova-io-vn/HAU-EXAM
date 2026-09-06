package com.examservice.domain.model;import java.util.UUID;public record ExamQuestionReference(UUID id,UUID questionId,int position,UUID matrixRuleId){}
