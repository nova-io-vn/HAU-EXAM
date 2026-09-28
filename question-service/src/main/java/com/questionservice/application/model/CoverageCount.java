package com.questionservice.application.model;

import com.questionservice.domain.model.Difficulty;
import java.util.UUID;

public record CoverageCount(UUID knowledgeItemId, Difficulty difficulty, long count) { }
