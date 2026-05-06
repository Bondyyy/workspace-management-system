package com.wms.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

public class EmailUtil {
    private static String SENDER_EMAIL;
    private static String SENDER_APP_PASSWORD;

    static {
        try (InputStream input = EmailUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                SENDER_EMAIL = prop.getProperty("mail.sender.email");
                SENDER_APP_PASSWORD = prop.getProperty("mail.sender.password");
                System.out.println("[EmailUtil] Đã nạp cấu hình: " + SENDER_EMAIL);
            } else {
                System.err.println("[EmailUtil] Không tìm thấy file config.properties trong resources!");
            }
        } catch (Exception ex) {
            System.err.println("[EmailUtil] Lỗi nạp cấu hình email: " + ex.getMessage());
        }
    }

    public static String generateRandomOTP() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    public static boolean sendOTP(String toEmail, String otp) {
        System.out.println("[EmailUtil] Đang gửi OTP tới: " + toEmail);
        if (SENDER_EMAIL == null || SENDER_APP_PASSWORD == null) {
            System.err.println("[EmailUtil] Lỗi: SENDER_EMAIL hoặc SENDER_APP_PASSWORD chưa được cấu hình!");
            return false;
        }

        Session session = createSmtpSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã OTP Đăng Ký Hệ Thống WMS");
            
            String content = "<h3>WMS Xin Chào</h3><p>Mã OTP của bạn là: <b>" + otp + "</b></p>";
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("[EmailUtil] Gửi email thành công!");
            return true;
        } catch (MessagingException e) {
            System.err.println("[EmailUtil] Lỗi gửi mail: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private static Session createSmtpSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.timeout", "10000"); // 10s timeout
        props.put("mail.smtp.connectiontimeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });
    }
}