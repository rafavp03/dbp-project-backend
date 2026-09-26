package dbp.projectbackend.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private static final Locale ES_PE = Locale.of("es", "PE");

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final String remitente;

    public EmailService(JavaMailSender mailSender,
                        ITemplateEngine templateEngine,
                        @Value("${app.mail.from:}") String remitente) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.remitente = remitente;
    }

    public void send(String destinatario, String asunto, String plantilla, Map<String, Object> variables) {
        if (!StringUtils.hasText(remitente)) {
            log.info("Correo no configurado: se omite '{}' para {}", asunto, destinatario);
            return;
        }
        try {
            Context contexto = new Context(ES_PE, variables);
            String html = templateEngine.process(plantilla, contexto);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(mensaje);

            log.info("Correo '{}' enviado a {}", asunto, destinatario);
        } catch (MessagingException | MailException ex) {
            log.error("No se pudo enviar el correo '{}' a {}", asunto, destinatario, ex);
        }
    }
}
