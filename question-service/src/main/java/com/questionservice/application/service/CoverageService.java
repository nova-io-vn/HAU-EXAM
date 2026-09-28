package com.questionservice.application.service;

import com.questionservice.application.port.out.*;
import com.questionservice.domain.exception.ForbiddenException;
import com.questionservice.domain.model.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CoverageService {
    private final CatalogRepository catalog; private final QuestionRepository questions;
    public CoverageService(CatalogRepository catalog,QuestionRepository questions){this.catalog=catalog;this.questions=questions;}

    public SubjectCoverage calculate(UUID subjectId,Actor actor){
        if(actor.role()!=Role.SUBJECT_ADMIN||actor.facultyId()==null)throw new ForbiddenException("COVERAGE_ACCESS_DENIED","SUBJECT_ADMIN faculty scope is required");
        var subject=catalog.findSubject(subjectId).orElseThrow(()->new IllegalArgumentException("Subject not found"));
        if(!subject.isAvailableTo(actor.facultyId()))throw new ForbiddenException("COVERAGE_ACCESS_DENIED","Subject is outside faculty scope");
        Map<UUID,EnumMap<Difficulty,Long>> actual=new HashMap<>();
        questions.approvedCoverage(subjectId).forEach(c->actual.computeIfAbsent(c.knowledgeItemId(),x->new EnumMap<>(Difficulty.class)).put(c.difficulty(),c.count()));
        var chapterRows=new ArrayList<ChapterCoverage>();var allItems=new ArrayList<ItemCoverage>();
        for(var chapter:catalog.findChapters(subjectId)){
            var topicRows=new ArrayList<TopicCoverage>();var chapterItems=new ArrayList<ItemCoverage>();
            for(var topic:catalog.findTopics(chapter.id())){
                var itemRows=catalog.findKnowledgeItems(topic.id()).stream().map(item->itemCoverage(item,actual.get(item.id()))).toList();
                chapterItems.addAll(itemRows);allItems.addAll(itemRows);
                topicRows.add(new TopicCoverage(topic.id(),topic.code(),topic.name(),summary(itemRows),itemRows));
            }
            chapterRows.add(new ChapterCoverage(chapter.id(),chapter.code(),chapter.name(),summary(chapterItems),topicRows));
        }
        return new SubjectCoverage(subject.id(),subject.code(),subject.name(),summary(allItems),chapterRows,
                "Coverage = sum(min(APPROVED questions, target) by EASY/MEDIUM/HARD) / sum(configured targets). Items without a target are reported as unconfigured and excluded from the percentage denominator.");
    }

    private ItemCoverage itemCoverage(KnowledgeItem item,EnumMap<Difficulty,Long> values){
        long easy=count(values,Difficulty.EASY),medium=count(values,Difficulty.MEDIUM),hard=count(values,Difficulty.HARD);
        int target=item.targetEasy()+item.targetMedium()+item.targetHard();
        long covered=Math.min(easy,item.targetEasy())+Math.min(medium,item.targetMedium())+Math.min(hard,item.targetHard());
        Double percentage=target==0?null:Math.round(covered*1000d/target)/10d;
        var missing=new ArrayList<String>();
        if(easy<item.targetEasy())missing.add("EASY thiếu "+(item.targetEasy()-easy));
        if(medium<item.targetMedium())missing.add("MEDIUM thiếu "+(item.targetMedium()-medium));
        if(hard<item.targetHard())missing.add("HARD thiếu "+(item.targetHard()-hard));
        return new ItemCoverage(item.id(),item.code(),item.name(),percentage,target,easy,medium,hard,item.targetEasy(),item.targetMedium(),item.targetHard(),missing);
    }
    private Summary summary(List<ItemCoverage> items){
        long target=items.stream().mapToLong(ItemCoverage::targetTotal).sum();
        long covered=items.stream().mapToLong(i->Math.min(i.easy(),i.targetEasy())+Math.min(i.medium(),i.targetMedium())+Math.min(i.hard(),i.targetHard())).sum();
        long approved=items.stream().mapToLong(i->i.easy()+i.medium()+i.hard()).sum();
        return new Summary(target==0?null:Math.round(covered*1000d/target)/10d,approved,target,items.stream().filter(i->i.targetTotal()==0).count());
    }
    private long count(EnumMap<Difficulty,Long> map,Difficulty d){return map==null?0:map.getOrDefault(d,0L);}

    public record SubjectCoverage(UUID id,String code,String name,Summary overall,List<ChapterCoverage> chapters,String formula){}
    public record ChapterCoverage(UUID id,String code,String name,Summary coverage,List<TopicCoverage> topics){}
    public record TopicCoverage(UUID id,String code,String name,Summary coverage,List<ItemCoverage> knowledgeItems){}
    public record ItemCoverage(UUID id,String code,String name,Double percentage,int targetTotal,long easy,long medium,long hard,int targetEasy,int targetMedium,int targetHard,List<String> missing){}
    public record Summary(Double percentage,long approvedQuestions,long targetQuestions,long unconfiguredItems){}
}
