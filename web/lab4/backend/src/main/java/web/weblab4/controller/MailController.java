package web.weblab4.controller;

import jakarta.ejb.Stateless;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

@Stateless
public class MailController {
    private final String username;
    private final String password;
    private final Properties props;


    public MailController() {
        this.username = System.getenv("MAIL_USERNAME");
        this.password = System.getenv("MAIL_PASSWORD");

        if (this.username == null || this.password == null) {
            throw new IllegalStateException("MAIL_USERNAME or MAIL_PASSWORD environment variables are not set.");
        }

        props = new Properties();
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
    }

    public void sendMail(String to, String subject, String body) {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send email");
            System.out.println(e);
        }
    }

    public void sendRegistrationEmail(String email, String username) {
        String subject = "Добро пожаловать!";
        String body = String.format("Привет %s,\n\nСпасибо за регистрацию. Я рад видеть тебя\n\nС наилучшими пожеланиями,\nНикита", username);

        sendMail(email, subject, body);
    }

    public void sendPasswordResetEmail(String email, String resetToken) {
        String subject = "Сброс пароля";
        String body = String.format("Чтобы сбросить пароль, нажмите на ссылку ниже:\n\nhttp://localhost:4200/#/change-password?token=%s\n\nЕсли вы не запрашивали это письмо, игнорируйте его.", resetToken);

        sendMail(email, subject, body);
    }
}
