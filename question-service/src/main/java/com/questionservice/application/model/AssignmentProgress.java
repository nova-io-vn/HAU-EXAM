package com.questionservice.application.model;

public record AssignmentProgress(long total, long submitted, long approved,
                                 long easyApproved, long mediumApproved, long hardApproved) { }
