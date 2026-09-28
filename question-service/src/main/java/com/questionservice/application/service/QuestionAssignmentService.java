package com.questionservice.application.service;

import com.questionservice.application.model.*;
import com.questionservice.application.port.out.*;
import com.questionservice.domain.exception.*;
import com.questionservice.domain.model.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuestionAssignmentService {
    private final QuestionAssignmentRepository assignments;
    private final QuestionRepository questions;
    private final CatalogRepository catalog;
    private final UserDirectoryPort users;
    private final Clock clock;

    public QuestionAssignmentService(QuestionAssignmentRepository assignments, QuestionRepository questions,
                                     CatalogRepository catalog, UserDirectoryPort users, Clock clock) {
        this.assignments=assignments;this.questions=questions;this.catalog=catalog;this.users=users;this.clock=clock;
    }

    public QuestionAssignmentView create(Actor actor, Command command) {
        requireSubjectAdmin(actor);
        validateScope(actor, command);
        Instant now=Instant.now(clock);
        var assignment=new QuestionAssignment(UUID.randomUUID(),actor.facultyId(),command.subjectId(),command.chapterId(),
                command.topicId(),command.knowledgeItemId(),command.lecturerId(),actor.userId(),command.requiredQuestionCount(),
                command.requiredEasy(),command.requiredMedium(),command.requiredHard(),command.deadline(),trim(command.note()),now,now);
        return view(assignments.save(assignment));
    }

    public QuestionAssignmentView update(UUID id, Actor actor, Command command) {
        requireSubjectAdmin(actor);
        var old=find(id);
        if(!Objects.equals(old.facultyId(),actor.facultyId())) throw denied();
        var progress=questions.assignmentProgress(id);
        if(progress.total()>0) throw new IllegalArgumentException("Assignment with linked questions can no longer be edited");
        validateScope(actor,command);
        return view(assignments.save(new QuestionAssignment(id,actor.facultyId(),command.subjectId(),command.chapterId(),
                command.topicId(),command.knowledgeItemId(),command.lecturerId(),old.assignedBy(),command.requiredQuestionCount(),
                command.requiredEasy(),command.requiredMedium(),command.requiredHard(),command.deadline(),trim(command.note()),old.createdAt(),Instant.now(clock))));
    }

    @Transactional(readOnly=true)
    public List<QuestionAssignmentView> list(Actor actor) {
        List<QuestionAssignment> values;
        if(actor.role()==Role.USER) values=assignments.findByLecturer(actor.userId());
        else if(actor.role()==Role.SUBJECT_ADMIN){ if(actor.facultyId()==null) throw denied(); values=assignments.findByFaculty(actor.facultyId()); }
        else throw denied();
        return values.stream().map(this::view).toList();
    }

    @Transactional(readOnly=true)
    public QuestionAssignmentView get(UUID id, Actor actor) {
        var value=find(id);
        if(actor.role()==Role.USER && Objects.equals(value.lecturerId(),actor.userId())) return view(value);
        if(actor.role()==Role.SUBJECT_ADMIN && Objects.equals(value.facultyId(),actor.facultyId())) return view(value);
        throw denied();
    }

    private void validateScope(Actor actor,Command c){
        var subject=catalog.findSubject(c.subjectId()).orElseThrow(()->new NotFoundException("SUBJECT_NOT_FOUND","Subject not found"));
        if(!Objects.equals(subject.managingFacultyId(),actor.facultyId())) throw denied();
        var lecturer=users.findLecturer(c.lecturerId()).orElseThrow(()->new NotFoundException("LECTURER_NOT_FOUND","Lecturer not found"));
        if(!"USER".equals(lecturer.role())||!"ACTIVE".equals(lecturer.status())||!subject.isAvailableTo(lecturer.facultyId())) throw denied();
        if(c.chapterId()!=null){var chapter=catalog.findChapter(c.chapterId()).orElseThrow(()->new NotFoundException("CHAPTER_NOT_FOUND","Chapter not found"));if(!Objects.equals(chapter.subjectId(),c.subjectId()))throw new IllegalArgumentException("Chapter does not belong to subject");}
        if(c.topicId()!=null){var topic=catalog.findTopic(c.topicId()).orElseThrow(()->new NotFoundException("TOPIC_NOT_FOUND","Topic not found"));if(c.chapterId()==null||!Objects.equals(topic.chapterId(),c.chapterId()))throw new IllegalArgumentException("Topic does not belong to chapter");}
        if(c.knowledgeItemId()!=null){var item=catalog.findKnowledgeItem(c.knowledgeItemId()).orElseThrow(()->new NotFoundException("KNOWLEDGE_ITEM_NOT_FOUND","Knowledge item not found"));if(c.topicId()==null||!Objects.equals(item.topicId(),c.topicId()))throw new IllegalArgumentException("Knowledge item does not belong to topic");}
    }

    private QuestionAssignmentView view(QuestionAssignment a){
        var p=questions.assignmentProgress(a.id());
        AssignmentStatus status;
        boolean difficultyMet=p.easyApproved()>=a.requiredEasy()&&p.mediumApproved()>=a.requiredMedium()&&p.hardApproved()>=a.requiredHard();
        if(p.approved()>=a.requiredQuestionCount()&&difficultyMet)status=AssignmentStatus.COMPLETED;
        else if(LocalDate.now(clock).isAfter(a.deadline()))status=AssignmentStatus.OVERDUE;
        else if(p.submitted()>=a.requiredQuestionCount())status=AssignmentStatus.SUBMITTED;
        else if(p.total()>0)status=AssignmentStatus.IN_PROGRESS;
        else status=AssignmentStatus.ASSIGNED;
        return new QuestionAssignmentView(a,status,p);
    }
    private QuestionAssignment find(UUID id){return assignments.findById(id).orElseThrow(()->new NotFoundException("ASSIGNMENT_NOT_FOUND","Question assignment not found"));}
    private void requireSubjectAdmin(Actor actor){if(actor==null||actor.role()!=Role.SUBJECT_ADMIN||actor.userId()==null||actor.facultyId()==null)throw denied();}
    private ForbiddenException denied(){return new ForbiddenException("ASSIGNMENT_ACCESS_DENIED","Question assignment is outside authenticated scope");}
    private String trim(String value){return value==null||value.isBlank()?null:value.trim();}

    public record Command(UUID subjectId,UUID chapterId,UUID topicId,UUID knowledgeItemId,UUID lecturerId,
                          int requiredQuestionCount,int requiredEasy,int requiredMedium,int requiredHard,
                          LocalDate deadline,String note){}
}
