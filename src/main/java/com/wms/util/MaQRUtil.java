package com.wms.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public final class MaQRUtil {

    private static final int DEFAULT_SIZE = 260;
    private static final DateTimeFormatter TOKEN_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private MaQRUtil() {
    }

    public static String taoMaQRPhien(String maPhien, String maDatCho) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        return "SPRING-MNGT|PHIEN=" + safe(maPhien)
                + "|DATCHO=" + safe(maDatCho)
                + "|TS=" + LocalDateTime.now().format(TOKEN_TIME)
                + "|TOKEN=" + token;
    }

    public static String taoMaQRDatCho(String maDatCho) {
        return taoMaQRPhien("", maDatCho);
    }

    public static byte[] taoAnhPng(String noiDung) {
        return taoAnhPng(noiDung, DEFAULT_SIZE);
    }

    public static byte[] taoAnhPng(String noiDung, int kichThuoc) {
        if (noiDung == null || noiDung.isBlank()) {
            return new byte[0];
        }
        int size = Math.max(120, kichThuoc);
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        try {
            BitMatrix matrix = new QRCodeWriter().encode(noiDung, BarcodeFormat.QR_CODE, size, size, hints);
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(matrix, "PNG", out);
                return out.toByteArray();
            }
        } catch (WriterException | IOException ex) {
            System.err.println("[MaQRUtil] Loi tao anh QR: " + ex.getMessage());
            return new byte[0];
        }
    }

    public static String taoDataUriPng(String noiDung, int kichThuoc) {
        byte[] bytes = taoAnhPng(noiDung, kichThuoc);
        if (bytes.length == 0) {
            return "";
        }
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
