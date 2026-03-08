package hu.martinvass.dms.shared;

import hu.martinvass.dms.config.AppConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppConfig appConfig;

    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = appConfig.getVerificationUrl(token);

        String subject = "Email Verification - DMS";
        String body = buildVerificationEmailBody(verificationUrl);

        sendHtmlEmail(to, subject, body);
    }

    public void sendInvitationEmail(String to, String token, String company, String expires) {
        String joinUrl = appConfig.getInvitationAcceptUrl(token);

        String subject = "Invitation to join " + company;
        String body = buildInvitationEmailBody(joinUrl, company, expires);

        sendHtmlEmail(to, subject, body);
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            message.setFrom("dms.app.test@gmail.com");

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + to, e);
        }
    }

    private String buildVerificationEmailBody(String verificationUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2>Welcome to DMS!</h2>
                    <p>Please verify your email by clicking the link below:</p>
                    <a href="%s" style="display: inline-block; padding: 12px 24px; background: #5F6F52; color: white; text-decoration: none; border-radius: 8px; margin: 20px 0;">
                        Verify Email
                    </a>
                    <p style="color: #666; font-size: 14px;">
                        Or copy this link: <a href="%s">%s</a>
                    </p>
                </div>
            </body>
            </html>
            """.formatted(verificationUrl, verificationUrl, verificationUrl);
    }

    private String buildInvitationEmailBody(String joinUrl, String company, String expires) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto;">
                    <h2>You've been invited!</h2>
                    <p>You've been invited to join <strong>%s</strong> on DMS.</p>
                    <a href="%s" style="display: inline-block; padding: 12px 24px; background: #5F6F52; color: white; text-decoration: none; border-radius: 8px; margin: 20px 0;">
                        Accept Invitation
                    </a>
                    <p style="color: #666; font-size: 14px;">
                        Or copy this link: <a href="%s">%s</a>
                    </p>
                    <p style="color: #999; font-size: 12px; margin-top: 40px;">
                        This invitation expires on %s.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(company, joinUrl, joinUrl, joinUrl, expires);
    }
}