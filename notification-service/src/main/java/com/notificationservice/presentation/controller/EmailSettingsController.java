package com.notificationservice.presentation.controller;
import com.notificationservice.application.port.out.EmailSender;
import com.notificationservice.presentation.request.EmailTestRequest;
import com.notificationservice.presentation.response.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/admin/email-settings") @PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class EmailSettingsController {
    private final EmailSender email; private final String host; private final int port; private final String username; private final String from; private final boolean auth; private final boolean startTls;
    public EmailSettingsController(EmailSender email,@Value("${spring.mail.host}")String host,@Value("${spring.mail.port}")int port,@Value("${spring.mail.username:}")String username,@Value("${notification.mail-from}")String from,@Value("${spring.mail.properties.mail.smtp.auth:false}")boolean auth,@Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")boolean startTls){this.email=email;this.host=host;this.port=port;this.username=username;this.from=from;this.auth=auth;this.startTls=startTls;}
    @GetMapping public ApiResponse<EmailSettingsResponse> get(){return ApiResponse.success(new EmailSettingsResponse(host,port,username,from,auth,startTls,host!=null&&!host.isBlank()));}
    @PostMapping("/test") public ApiResponse<Void> test(@Valid @RequestBody EmailTestRequest request){email.send(request.recipient(),"[HAU QM] Email kiểm tra","Email kiểm tra HAU QM đã được gửi thành công.");return ApiResponse.success(null);}
}
