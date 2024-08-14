package com.amr.book.email;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    @Async //i should make @EnableAsync in application
    public void sendEmail(
            String to,
            String username,
            EmailTemplate emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject
    ) throws MessagingException {
        String templateName;
        if(emailTemplate==null){
            templateName="confirm-email";
        }else{
            templateName=emailTemplate.name();
        }
        //to configure mail sender
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper=new MimeMessageHelper(
                mimeMessage,
                MimeMessageHelper.MULTIPART_MODE_MIXED,
                StandardCharsets.UTF_8.name()
        );
        //to pass some parameter to our email template
        Map<String,Object>properties=new HashMap<>();
        properties.put("username",username);
        properties.put("confirmationUrl",confirmationUrl);
        properties.put("activation_code",activationCode);

        //thymleaf
        Context context= new Context();
        context.setVariables(properties);
        helper.setFrom("contact@osamaamr397.com");
        helper.setTo(to);
        helper.setSubject(subject);
        String template = templateEngine.process(templateName,context);
        helper.setText(template,true);
        mailSender.send(mimeMessage);
    }
}
