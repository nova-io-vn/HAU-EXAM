package com.examservice.application.service;

import com.examservice.application.port.out.ExportMetadataPort;
import com.examservice.application.port.out.QuestionCatalogPort;
import com.examservice.domain.exception.DomainException;
import com.examservice.domain.model.Exam;
import java.io.*;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;

@Service
public class ExamPdfService {
    private static final float MARGIN=52,LINE=17;
    private final QuestionCatalogPort questions; private final ExportMetadataPort metadata;
    public ExamPdfService(ExamGenerationService ignored,QuestionCatalogPort questions,ExportMetadataPort metadata){this.questions=questions;this.metadata=metadata;}

    public ExportedPdf export(Exam exam,int version,String token){
        var selected=exam.versions().stream().filter(v->v.versionNumber()==version).findFirst().orElseThrow(()->new IllegalArgumentException("Exam version not found"));
        var exporter=metadata.exporter(token);var subject=metadata.subject(exam.subjectId(),token);String faculty=metadata.facultyName(exam.facultyId());
        List<Line> lines=new ArrayList<>();
        lines.add(new Line("TRƯỜNG ĐẠI HỌC KIẾN TRÚC HÀ NỘI",true));
        lines.add(new Line("KHOA: "+faculty,true));lines.add(new Line("MÔN: "+subject.name(),true));lines.add(new Line("",false));
        lines.add(new Line("ĐỀ THI TRẮC NGHIỆM",true));lines.add(new Line("Mã đề: "+exam.examCode()+" · Phiên bản: "+version,false));
        lines.add(new Line("Thời lượng: "+exam.durationMinutes()+" phút",false));lines.add(new Line("",false));
        int number=1;
        for(var ref:selected.questions()){
            var q=questions.question(ref.questionId(),token);lines.add(new Line("Câu "+number+++": "+plain(q.content()),true));
            for(var option:q.options())lines.add(new Line("    "+option.label()+". "+plain(option.content()),false));
            lines.add(new Line("",false));
        }
        lines.add(new Line("",false));lines.add(new Line("Người xuất đề",true));lines.add(new Line(exporter.fullName(),true));
        lines.add(new Line("",false));lines.add(new Line("____________________",false));lines.add(new Line("Chữ ký",false));
        byte[] body=render(lines);
        String filename=safe(subject.code())+"_"+safe(exam.examCode())+"_v"+version+".pdf";
        return new ExportedPdf(body,filename);
    }

    private byte[] render(List<Line> source){
        try(var document=new PDDocument();var output=new ByteArrayOutputStream()){
            Font font=font(document);PDPage page=null;PDPageContentStream stream=null;float y=0;int pageNo=0;
            for(Line sourceLine:source){
                for(String value:wrap(font.safe(sourceLine.text()),95)){
                    if(stream==null||y<MARGIN+35){if(stream!=null){footer(stream,font,pageNo);stream.close();}page=new PDPage(PDRectangle.A4);document.addPage(page);stream=new PDPageContentStream(document,page);y=PDRectangle.A4.getHeight()-MARGIN;pageNo++;}
                    stream.beginText();stream.setFont(sourceLine.bold()?font.bold():font.regular(),sourceLine.bold()?12:11);stream.newLineAtOffset(MARGIN,y);stream.showText(value);stream.endText();y-=LINE;
                }
            }
            if(stream!=null){footer(stream,font,pageNo);stream.close();}
            document.getDocumentInformation().setTitle("Đề thi "+source.get(4).text());document.getDocumentInformation().setProducer("HAU Exam");document.save(output);return output.toByteArray();
        }catch(IOException e){throw new DomainException("Unable to generate exam PDF");}
    }
    private void footer(PDPageContentStream stream,Font font,int page)throws IOException{stream.beginText();stream.setFont(font.regular(),9);stream.newLineAtOffset(PDRectangle.A4.getWidth()-100,24);stream.showText("Trang "+page);stream.endText();}
    private Font font(PDDocument document)throws IOException{
        List<Path> paths=new ArrayList<>();String windows=System.getenv("WINDIR");if(windows!=null){paths.add(Path.of(windows,"Fonts","arial.ttf"));paths.add(Path.of(windows,"Fonts","arialbd.ttf"));}
        paths.add(Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"));
        Path regular=paths.stream().filter(Files::isRegularFile).findFirst().orElse(null);
        if(regular!=null){Path bold=regular.getFileName().toString().equalsIgnoreCase("arial.ttf")?regular.resolveSibling("arialbd.ttf"):regular.resolveSibling("DejaVuSans-Bold.ttf");PDType0Font r=PDType0Font.load(document,Files.newInputStream(regular));PDType0Font b=Files.isRegularFile(bold)?PDType0Font.load(document,Files.newInputStream(bold)):r;return new Font(r,b,true);}
        return new Font(new PDType1Font(Standard14Fonts.FontName.HELVETICA),new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),false);
    }
    private static String plain(String value){return (value==null?"":value).replaceAll("<[^>]+>"," ").replace("&nbsp;"," ").replace("&amp;","&").replaceAll("\\s+"," ").trim();}
    private static String safe(String value){String s=Normalizer.normalize(value==null?"exam":value,Normalizer.Form.NFD).replaceAll("\\p{M}","").replaceAll("[^A-Za-z0-9_-]+","-");return s.isBlank()?"exam":s;}
    private static List<String> wrap(String value,int max){if(value.isBlank())return List.of("");List<String> out=new ArrayList<>();String remaining=value;while(remaining.length()>max){int at=remaining.lastIndexOf(' ',max);if(at<1)at=max;out.add(remaining.substring(0,at));remaining=remaining.substring(at).trim();}out.add(remaining);return out;}
    private record Line(String text,boolean bold){}
    private record Font(PDFont regular,PDFont bold,boolean unicode){String safe(String value){return unicode?value:Normalizer.normalize(value,Normalizer.Form.NFD).replaceAll("\\p{M}","").replaceAll("[^\\x20-\\x7E]","?");}}
    public record ExportedPdf(byte[] bytes,String filename){}
}
