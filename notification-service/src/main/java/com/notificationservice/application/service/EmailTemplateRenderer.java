package com.notificationservice.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** One responsive, email-safe HAU QM template shared by every system email. */
@Component
public class EmailTemplateRenderer {
    private final String systemName, shortName, websiteUrl, logoUrl;
    public EmailTemplateRenderer() { this("HAU QM", "HAU QM", "https://exam.nova.io.vn", ""); }
    public EmailTemplateRenderer(@Value("${notification.system-name:HAU QM}") String systemName,
                                 @Value("${notification.short-name:HAU QM}") String shortName,
                                 @Value("${notification.website-url:https://exam.nova.io.vn}") String websiteUrl,
                                 @Value("${notification.logo-url:}") String logoUrl) {
        this.systemName=fallback(systemName,"HAU QM"); this.shortName=fallback(shortName,"HAU QM"); this.websiteUrl=fallback(websiteUrl,"https://exam.nova.io.vn"); this.logoUrl=logoUrl==null?"":logoUrl.trim();
    }
    public String render(String recipientName,String title,String body,String ctaText,String ctaUrl) {
        String logo=logoUrl.isBlank()?"<div style=\"font-size:28px;font-weight:800;letter-spacing:.04em\">HAU QM</div>":"<img src=\""+esc(logoUrl)+"\" alt=\"HAU QM\" width=\"48\" style=\"display:block;border:0;margin-bottom:10px\">";
        String cta=ctaUrl==null||ctaUrl.isBlank()?"":"<p style=\"margin:26px 0\"><a href=\""+esc(ctaUrl)+"\" style=\"display:inline-block;background:#0756a6;color:#fff;padding:13px 22px;border-radius:6px;text-decoration:none;font-weight:700\">"+esc(fallback(ctaText,"Mở HAU QM"))+"</a></p>";
        return "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head><body style=\"margin:0;background:#f4f7fb;font-family:Arial,Helvetica,sans-serif;color:#18324b\"><table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tr><td align=\"center\" style=\"padding:28px 12px\"><table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:600px;width:100%;background:#fff;border-radius:12px;overflow:hidden\"><tr><td style=\"padding:22px 28px;background:#0756a6;color:#fff\">"+logo+"<div style=\"font-size:13px;margin-top:8px\">Hệ thống hỗ trợ quản lý ngân hàng câu hỏi và đề thi</div></td></tr><tr><td style=\"padding:30px 28px;line-height:1.6\"><h1 style=\"font-size:24px;line-height:1.3;margin:0 0 20px\">"+esc(title)+"</h1><p>Xin chào "+esc(fallback(recipientName,"bạn"))+",</p><p>"+esc(body).replace("\n","<br>")+"</p>"+cta+"</td></tr><tr><td style=\"padding:18px 28px;border-top:1px solid #e5ebf2;color:#6a7d90;font-size:12px\">"+esc(systemName)+"<br>Trường Đại học Kiến trúc Hà Nội<br><a href=\""+esc(websiteUrl)+"\" style=\"color:#0756a6\">"+esc(websiteUrl)+"</a><br><br>Đây là email được gửi tự động từ hệ thống HAU QM. Vui lòng không gửi thông tin mật qua email.</td></tr></table></td></tr></table></body></html>";
    }
    private static String fallback(String value,String fallback){return value==null||value.isBlank()?fallback:value.trim();}
    private static String esc(String value){return value==null?"":value.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
}
