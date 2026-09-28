package com.questionservice.application.model;

import com.questionservice.domain.model.AssignmentStatus;
import com.questionservice.domain.model.QuestionAssignment;

public record QuestionAssignmentView(QuestionAssignment assignment, AssignmentStatus status, AssignmentProgress progress) { }
