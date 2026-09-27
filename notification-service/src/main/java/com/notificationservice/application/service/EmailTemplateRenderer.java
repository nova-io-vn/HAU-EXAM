package com.notificationservice.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Renders one email-compatible HAU QM layout for every notification template. */
@Component
public class EmailTemplateRenderer {
    private final String systemName;
    private final String shortName;
    private final String websiteUrl;
    private final String logoUrl;

    public EmailTemplateRenderer() { this("HAU QM", "HAU QM", "https://exam.nova.io.vn/", ""); }

    @Autowired
    public EmailTemplateRenderer(@Value("${notification.system-name:HAU QM}") String systemName,
                                 @Value("${notification.short-name:HAU QM}") String shortName,
                                 @Value("${notification.website-url:https://exam.nova.io.vn/}") String websiteUrl,
                                 @Value("${notification.logo-url:}") String logoUrl) {
        this.systemName = fallback(systemName, "HAU QM");
        this.shortName = fallback(shortName, "HAU QM");
        this.websiteUrl = fallback(websiteUrl, "https://exam.nova.io.vn/");
        this.logoUrl = logoUrl == null ? "" : logoUrl.trim();
    }

    public String render(String recipientName, String title, String body, String ctaText, String ctaUrl) {
        String logo = logoUrl.isBlank() ? "" : "<img src=\"" + esc(logoUrl) + "\" alt=\"" + esc(shortName) + "\" width=\"42\" style=\"display:block;border:0;margin-bottom:10px\">";
        String cta = ctaUrl == null || ctaUrl.isBlank() ? "" : "<p style=\"margin:26px 0\"><a href=\"" + esc(ctaUrl) + "\" style=\"display:inline-block;background:#0756a6;color:#ffffff;padding:13px 22px;border-radius:6px;text-decoration:none;font-weight:700\">" + esc(fallback(ctaText, "Truy cập HAU QM")) + "</a></p>";
        return "<!doctype html><html><body style=\"margin:0;background:#f4f7fb;font-family:Arial,Helvetica,sans-serif;color:#18324b\"><table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\"><tr><td align=\"center\" style=\"padding:28px 12px\"><table role=\"presentation\" width=\"600\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:600px;background:#ffffff;border-radius:12px;overflow:hidden\"><tr><td style=\"padding:22px 28px;background:#0756a6;color:#ffffff;font-size:21px;font-weight:700\">" + logo + esc(shortName) + " <span style=\"font-size:13px;font-weight:400\">· Hệ thống Quản lý Khảo thí</span></td></tr><tr><td style=\"padding:30px 28px;line-height:1.6\"><h1 style=\"font-size:24px;line-height:1.3;margin:0 0 20px\">" + esc(title) + "</h1><p>Xin chào " + esc(fallback(recipientName, "bạn")) + ",</p><p>" + esc(body).replace("\n", "<br>") + "</p>" + cta + "</td></tr><tr><td style=\"padding:18px 28px;border-top:1px solid #e5ebf2;color:#6a7d90;font-size:12px\">Bạn nhận được email này từ " + esc(systemName) + ".<br><a href=\"" + esc(websiteUrl) + "\" style=\"color:#0756a6\">Truy cập hệ thống</a><br>© HAU QM</td></tr></table></td></tr></table></body></html>";
    }

    private static String fallback(String value, String fallback) { return value == null || value.isBlank() ? fallback : value.trim(); }
    private static String esc(String value) { return value == null ? "" : value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;"); }
}
