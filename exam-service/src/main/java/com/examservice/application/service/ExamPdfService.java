package com.examservice.application.service;

import com.examservice.application.port.out.QuestionCatalogPort;
import com.examservice.domain.model.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class ExamPdfService {
    private final ExamGenerationService exams;
    private final QuestionCatalogPort questions;
    public ExamPdfService(ExamGenerationService exams, QuestionCatalogPort questions) { this.exams=exams; this.questions=questions; }
    public byte[] export(Exam exam, int version, String token) {
        var selected=exam.versions().stream().filter(v->v.versionNumber()==version).findFirst().orElseThrow(()->new IllegalArgumentException("Exam version not found"));
        List<String> lines=new ArrayList<>(); lines.add("HAU QM - " + exam.name()); lines.add("Subject: " + exam.subjectId()); lines.add("Version: " + version); lines.add("");
        int number=1;
        for(var ref:selected.questions()) { var q=questions.question(ref.questionId(),token); lines.add(number++ + ". " + q.content()); for(var o:q.options()) lines.add("   " + o.label()+". "+o.content()); lines.add(""); }
        return pdf(lines);
    }
    private static byte[] pdf(List<String> source) {
        List<List<String>> pages=new ArrayList<>(); for(int i=0;i<source.size();i+=44) pages.add(source.subList(i,Math.min(i+44,source.size())));
        StringBuilder out=new StringBuilder("%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n"); List<Integer> offsets=new ArrayList<>(); offsets.add(0);
        int pageCount=pages.size(), font=3+pageCount*2, catalog=font+1, pagesObj=font+2;
        add(out,offsets,1,"<< /Type /Catalog /Pages "+pagesObj+" 0 R >>");
        StringBuilder kids=new StringBuilder(); for(int p=0;p<pageCount;p++) kids.append(3+p*2).append(" 0 R ");
        add(out,offsets,pagesObj,"<< /Type /Pages /Kids ["+kids+"] /Count "+pageCount+" >>");
        for(int p=0;p<pageCount;p++){int page=3+p*2,content=page+1;StringBuilder stream=new StringBuilder("BT /F1 11 Tf 50 770 Td\n");int row=0;for(String line:pages.get(p)){for(String part:wrap(ascii(line),105)){if(row++>0)stream.append("0 -16 Td\n");stream.append("(").append(escape(part)).append(") Tj\n");}}stream.append("ET");add(out,offsets,page,"<< /Type /Page /Parent "+pagesObj+" 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 "+font+" 0 R >> >> /Contents "+content+" 0 R >>");add(out,offsets,content,"<< /Length "+stream.length()+" >>\nstream\n"+stream+"\nendstream");}
        add(out,offsets,font,"<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"); add(out,offsets,catalog,"<< /Producer (HAU QM) >>");
        int xref=out.length();out.append("xref\n0 ").append(offsets.size()).append("\n0000000000 65535 f \n");for(int i=1;i<offsets.size();i++)out.append(String.format("%010d 00000 n \n",offsets.get(i)));out.append("trailer\n<< /Size ").append(offsets.size()).append(" /Root 1 0 R >>\nstartxref\n").append(xref).append("\n%%EOF");return out.toString().getBytes(StandardCharsets.ISO_8859_1);
    }
    private static void add(StringBuilder out,List<Integer> offsets,int n,String body){while(offsets.size()<=n)offsets.add(0);offsets.set(n,out.length());out.append(n).append(" 0 obj\n").append(body).append("\nendobj\n");}
    private static String escape(String s){return s.replace("\\","\\\\").replace("(","\\(").replace(")","\\)");}
    private static String ascii(String s){return Normalizer.normalize(s==null?"":s,Normalizer.Form.NFD).replaceAll("\\p{M}","").replaceAll("[^\\x20-\\x7E]","?");}
    private static List<String> wrap(String s,int max){List<String> r=new ArrayList<>();while(s.length()>max){int at=s.lastIndexOf(' ',max);if(at<1)at=max;r.add(s.substring(0,at));s=s.substring(at).trim();}r.add(s);return r;}
}
