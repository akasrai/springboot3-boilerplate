package com.springboot3.boilerplate.user;

import com.springboot3.boilerplate.config.MailConfig;
import com.springboot3.boilerplate.miscellaneous.mail.MailService;
import com.springboot3.boilerplate.user.enduser.Enduser;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserMailService {

    private static final Logger logger = LoggerFactory.getLogger(UserMailService.class);

    private final Configuration template;

    private final MailConfig mailConfig;

    @Autowired
    private MailService mailService;

    @Autowired
    UserMailService(MailConfig mailConfig, Configuration template) {
        this.mailConfig = mailConfig;
        this.template = template;
    }

    @Async
    public void sendMail(Enduser sender, UserEmailEvent userEmailEvent) {
        try {
            String subject = userEmailEvent.getSubject();
            String templateName = userEmailEvent.getTemplate();
            Map<String, String> templateVariable = getTemplateVariable(sender);

            if (!templateName.isEmpty()) {
                Template t = template.getTemplate(templateName);
                String body = FreeMarkerTemplateUtils.processTemplateIntoString(t, templateVariable);

                mailService.sendMail(sender.getEmail(), subject, body);
            } else {
                logger.error("No email template found to send an email for {}", userEmailEvent.name());
            }

        } catch (Exception ex) {
            logger.error("Error while sending email for {}", userEmailEvent.name(), ex);
        }
    }

    @Async
    public void sendMail(Enduser sender, UserEmailEvent userEmailEvent, String verificationCode) {
        try {
            String subject = userEmailEvent.getSubject();
            String templateName = userEmailEvent.getTemplate();
            Map<String, String> templateVariable = getTemplateVariable(sender);
            templateVariable.put("VERIFICATION_CODE", verificationCode);

            if (!templateName.isEmpty()) {
                Template t = template.getTemplate(templateName);
                String body = FreeMarkerTemplateUtils.processTemplateIntoString(t, templateVariable);

                mailService.sendMail(sender.getEmail(), subject, body);
            } else {
                logger.error("No email template found to send an email for {}", userEmailEvent.name());
            }
        } catch (Exception ex) {
            logger.error("Error while sending verification mail to fundraiser id: {}", sender.getEmail(), ex);
        }
    }

    private Map<String, String> getTemplateVariable(Enduser sender) {
        Map<String, String> map = new HashMap<>();
        map.put("USER_NAME", sender.getFullName());
        map.put("BASE_URL", mailConfig.getBaseUrl());

        return map;
    }
}
