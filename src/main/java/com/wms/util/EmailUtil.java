package com.wms.util;

import jakarta.mail.Authenticator;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.activation.DataHandler;

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
        if (SENDER_EMAIL == null || SENDER_EMAIL.isBlank()
                || SENDER_APP_PASSWORD == null || SENDER_APP_PASSWORD.isBlank()) {
            System.err.println("[EmailUtil] Lỗi: email gửi hoặc app password chưa được cấu hình!");
            return false;
        }

        Session session = createSmtpSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Mã OTP đăng ký hệ thống WMS");

            String content = "<h3>WMS xin chào</h3><p>Mã OTP của bạn là: <b>" + otp + "</b></p>";
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

    public static boolean guiEmailXacNhanDatChoDaThanhToan(
            String toEmail,
            String hoTen,
            String maPhien,
            String maDatCho,
            String tenKhongGian,
            String tenChiNhanh,
            String thoiGian,
            String thanhTien,
            byte[] anhQRPng) {
        return guiEmailXacNhanDatChoDaThanhToan(toEmail, hoTen, maPhien, maDatCho,
                tenKhongGian, tenChiNhanh, thoiGian, thanhTien, null, anhQRPng);
    }

    public static boolean guiEmailXacNhanDatChoDaThanhToan(
            String toEmail,
            String hoTen,
            String maPhien,
            String maDatCho,
            String tenKhongGian,
            String tenChiNhanh,
            String thoiGian,
            String thanhTien,
            String maQR,
            byte[] anhQRPng) {
        if (toEmail == null || toEmail.isBlank()) {
            System.err.println("[EmailUtil] Không có email khách hàng để gửi xác nhận đặt chỗ.");
            return false;
        }
        if (SENDER_EMAIL == null || SENDER_EMAIL.isBlank()
                || SENDER_APP_PASSWORD == null || SENDER_APP_PASSWORD.isBlank()) {
            System.err.println("[EmailUtil] Lỗi: email gửi hoặc app password chưa được cấu hình!");
            return false;
        }

        Session session = createSmtpSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            String code = safe(maPhien).isBlank() ? safe(maDatCho) : safe(maPhien);
            message.setSubject("Xác nhận thanh toán đặt chỗ " + code + " - Spring MNGT");

            Multipart multipart = new MimeMultipart("related");

            BodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(taoNoiDungEmailXacNhan(hoTen, maPhien, maDatCho, tenKhongGian, tenChiNhanh, thoiGian, thanhTien, maQR),
                    "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            if (anhQRPng != null && anhQRPng.length > 0) {
                MimeBodyPart qrPart = new MimeBodyPart();
                qrPart.setDataHandler(new DataHandler(new ByteArrayDataSource(anhQRPng, "image/png")));
                qrPart.setHeader("Content-ID", "<layQrPhien>");
                qrPart.setDisposition(MimeBodyPart.INLINE);
                qrPart.setFileName("qr-" + code + ".png");
                multipart.addBodyPart(qrPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("[EmailUtil] Đã gửi email xác nhận đặt chỗ tới: " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.err.println("[EmailUtil] Lỗi gửi email xác nhận đặt chỗ: " + e.getMessage());
            return false;
        }
    }

    public static boolean guiEmailThanhToanDatChoThatBai(
            String toEmail,
            String hoTen,
            String maDatCho,
            String tenKhongGian,
            String tenChiNhanh,
            String thoiGian,
            String thanhTien,
            String lyDo) {
        if (toEmail == null || toEmail.isBlank()) {
            System.err.println("[EmailUtil] Không có email khách hàng để gửi thông báo thanh toán thất bại.");
            return false;
        }
        if (SENDER_EMAIL == null || SENDER_EMAIL.isBlank()
                || SENDER_APP_PASSWORD == null || SENDER_APP_PASSWORD.isBlank()) {
            System.err.println("[EmailUtil] Lỗi: email gửi hoặc app password chưa được cấu hình!");
            return false;
        }

        Session session = createSmtpSession();
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Thanh toán đặt chỗ chưa thành công " + safe(maDatCho) + " - Spring MNGT");
            String content = """
                    <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#1f1722;">
                        <h2 style="color:#eb5e8d;">Spring MNGT chưa xác nhận được thanh toán</h2>
                        <p>Xin chào <b>%s</b>,</p>
                        <p>Yêu cầu đặt chỗ của bạn chưa được thanh toán thành công.</p>
                        <div style="background:#fff5f8;border:1px solid #f3c9d9;padding:14px 18px;margin:18px 0;">
                            <p><b>Mã đặt chỗ:</b> %s</p>
                            <p><b>Không gian:</b> %s</p>
                            <p><b>Chi nhánh:</b> %s</p>
                            <p><b>Thời gian:</b> %s</p>
                            <p><b>Số tiền cần thanh toán:</b> %s</p>
                            <p><b>Lý do:</b> %s</p>
                        </div>
                        <p>Không gian đã được mở lại để khách khác có thể đặt. Bạn có thể tạo yêu cầu mới trên portal.</p>
                    </div>
                    """.formatted(
                    html(safe(hoTen, "Quý khách")),
                    html(safe(maDatCho)),
                    html(safe(tenKhongGian)),
                    html(safe(tenChiNhanh)),
                    html(safe(thoiGian)),
                    html(safe(thanhTien, "0 VNĐ")),
                    html(safe(lyDo, "Không nhận được thanh toán trong thời gian giữ chỗ."))
            );
            message.setContent(content, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("[EmailUtil] Đã gửi email thanh toán thất bại tới: " + toEmail);
            return true;
        } catch (MessagingException e) {
            System.err.println("[EmailUtil] Lỗi gửi email thanh toán thất bại: " + e.getMessage());
            return false;
        }
    }

    private static String taoNoiDungEmailXacNhan(String hoTen, String maPhien, String maDatCho,
                                                 String tenKhongGian, String tenChiNhanh,
                                                 String thoiGian, String thanhTien, String maQR) {
        String qrHtml = "<p style=\"margin:18px 0 8px;color:#555;\">Mã QR nhận chỗ của bạn:</p>"
                + "<img src=\"cid:layQrPhien\" alt=\"QR nhận chỗ\" style=\"width:220px;height:220px;border:1px solid #f3c9d9;padding:10px;\"/>";
        String qrText = safe(maQR).isBlank()
                ? ""
                : "<p style=\"word-break:break-all;\"><b>Mã QR dự phòng:</b> " + html(safe(maQR)) + "</p>";
        String maChinh = safe(maPhien).isBlank() ? safe(maDatCho) : safe(maPhien);
        String nhanMaChinh = safe(maPhien).isBlank() ? "Mã đặt chỗ" : "Mã phiên";
        String dongMaDatCho = safe(maPhien).isBlank()
                ? ""
                : "<p><b>Mã đặt chỗ:</b> " + html(safe(maDatCho)) + "</p>";
        return """
                <div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;color:#1f1722;">
                    <h2 style="color:#eb5e8d;">Spring MNGT xác nhận thanh toán thành công</h2>
                    <p>Xin chào <b>%s</b>,</p>
                    <p>Cảm ơn bạn đã đặt chỗ tại Spring MNGT. Phiên làm việc của bạn đã được nhân viên xác nhận thanh toán.</p>
                    <div style="background:#fff5f8;border:1px solid #f3c9d9;padding:14px 18px;margin:18px 0;">
                        <p><b>%s:</b> %s</p>
                        %s
                        <p><b>Không gian:</b> %s</p>
                        <p><b>Chi nhánh:</b> %s</p>
                        <p><b>Thời gian:</b> %s</p>
                        <p><b>Tổng cộng:</b> %s</p>
                        %s
                    </div>
                    %s
                    <p style="margin-top:18px;">Khi đến quầy, bạn có thể mở email này hoặc lịch sử đặt chỗ trên web để nhân viên quét mã nhanh hơn.</p>
                    <p>Hẹn gặp bạn tại Spring MNGT.</p>
                </div>
                """.formatted(
                html(safe(hoTen, "Quý khách")),
                html(nhanMaChinh),
                html(maChinh),
                dongMaDatCho,
                html(safe(tenKhongGian)),
                html(safe(tenChiNhanh)),
                html(safe(thoiGian)),
                html(safe(thanhTien, "0 VNĐ")),
                qrText,
                qrHtml
        );
    }

    private static String safe(String value) {
        return safe(value, "");
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String html(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static Session createSmtpSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.connectiontimeout", "10000");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_APP_PASSWORD);
            }
        });
    }
}
