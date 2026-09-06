package com.questionservice.domain.model;
import static org.junit.jupiter.api.Assertions.*; import com.questionservice.domain.exception.InvalidTransitionException; import java.time.Instant; import java.util.*; import org.junit.jupiter.api.Test;
class QuestionTest {private static final Instant NOW=Instant.parse("2026-01-01T00:00:00Z");private Question question(QuestionSource source){return Question.create(UUID.randomUUID(),"CNTT",UUID.randomUUID(),UUID.randomUUID(),null,"Content",null,null,QuestionType.SINGLE_CHOICE,Difficulty.EASY,source,source==QuestionSource.AI?"job:item":null,UUID.randomUUID(),List.of(new QuestionOption(UUID.randomUUID(),"A","Yes",null,null,true,0),new QuestionOption(UUID.randomUUID(),"B","No",null,null,false,1)),NOW);}
 @Test void aiQuestionAlwaysStartsDraft(){assertEquals(QuestionStatus.DRAFT,question(QuestionSource.AI).status());}
 @Test void cannotApproveBeforeSubmit(){assertThrows(InvalidTransitionException.class,()->question(QuestionSource.MANUAL).approve(UUID.randomUUID(),null,NOW));}
 @Test void approvedQuestionCannotBeEdited(){var q=question(QuestionSource.MANUAL);q.submit(NOW);q.approve(UUID.randomUUID(),null,NOW);assertThrows(InvalidTransitionException.class,()->q.edit("new",null,null,QuestionType.SINGLE_CHOICE,Difficulty.HARD,q.options(),NOW));}
}
