package com.wms.util;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

public final class QRCodeScannerUtil {
    private QRCodeScannerUtil() {
    }

    public static String quetHoacNhapThuCong(Component parent) {
        AtomicReference<String> ketQua = new AtomicReference<>();
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Quét mã QR nhận chỗ", true);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(BorderFactory.createEmptyBorder(18, 18, 16, 18));
        root.setBackground(Color.WHITE);

        JLabel title = new JLabel("Quét mã QR nhận chỗ");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.decode("#231E30"));
        JLabel subtitle = new JLabel("Đưa mã QR của hội viên vào vùng camera để xác nhận.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(Color.decode("#6F5E6B"));
        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.CENTER);

        JLabel preview = new JLabel("Đang chờ quyền camera...", SwingConstants.CENTER);
        preview.setPreferredSize(new Dimension(560, 360));
        preview.setOpaque(true);
        preview.setBackground(Color.decode("#FFF5F8"));
        preview.setForeground(Color.decode("#C64275"));
        preview.setFont(new Font("Segoe UI", Font.BOLD, 16));
        preview.setBorder(BorderFactory.createLineBorder(Color.decode("#F6B8CE"), 2, true));

        JLabel status = new JLabel("Đang quét mã QR...");
        status.setFont(new Font("Segoe UI", Font.BOLD, 13));
        status.setForeground(Color.decode("#C64275"));

        JButton manual = new JButton("Nhập mã thủ công");
        JButton stop = new JButton("Dừng");
        JButton close = new JButton("Đóng");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(status);
        actions.add(manual);
        actions.add(stop);
        actions.add(close);

        root.add(header, BorderLayout.NORTH);
        root.add(preview, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        dialog.setContentPane(root);

        SwingWorker<Void, BufferedImage> worker = new SwingWorker<>() {
            private Webcam webcam;

            @Override
            protected Void doInBackground() {
                try {
                    webcam = Webcam.getDefault();
                    if (webcam == null) {
                        SwingUtilities.invokeLater(() -> status.setText("Không tìm thấy camera. Vui lòng nhập mã thủ công."));
                        return null;
                    }
                    webcam.setViewSize(WebcamResolution.VGA.getSize());
                    webcam.open();
                    SwingUtilities.invokeLater(() -> status.setText("Đang quét mã QR..."));
                    MultiFormatReader reader = new MultiFormatReader();
                    while (!isCancelled()) {
                        BufferedImage image = webcam.getImage();
                        if (image == null) {
                            continue;
                        }
                        SwingUtilities.invokeLater(() -> preview.setIcon(new ImageIcon(scale(image, 560, 360))));
                        try {
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
                            Result result = reader.decode(bitmap);
                            if (result != null && result.getText() != null && !result.getText().isBlank()) {
                                ketQua.set(result.getText().trim());
                                SwingUtilities.invokeLater(() -> {
                                    status.setText("Đã nhận mã QR, đang xác thực...");
                                    dialog.dispose();
                                });
                                break;
                            }
                        } catch (NotFoundException ignored) {
                            // Frame chưa có QR, tiếp tục quét.
                        } finally {
                            reader.reset();
                        }
                    }
                } catch (RuntimeException ex) {
                    SwingUtilities.invokeLater(() -> status.setText("Không mở được camera. Vui lòng nhập mã thủ công."));
                } finally {
                    if (webcam != null && webcam.isOpen()) {
                        webcam.close();
                    }
                }
                return null;
            }
        };

        manual.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(dialog, "Dán hoặc nhập nội dung QR:", "Nhập mã thủ công", JOptionPane.PLAIN_MESSAGE);
            if (input != null && !input.isBlank()) {
                ketQua.set(input.trim());
                dialog.dispose();
            }
        });
        stop.addActionListener(e -> {
            worker.cancel(true);
            status.setText("Đã dừng quét. Có thể nhập mã thủ công hoặc đóng.");
        });
        close.addActionListener(e -> dialog.dispose());
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                worker.cancel(true);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        worker.execute();
        dialog.setVisible(true);
        worker.cancel(true);
        return ketQua.get();
    }

    private static Image scale(BufferedImage image, int maxWidth, int maxHeight) {
        double ratio = Math.min(maxWidth / (double) image.getWidth(), maxHeight / (double) image.getHeight());
        int width = Math.max(1, (int) Math.round(image.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(image.getHeight() * ratio));
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(image.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
        g2.dispose();
        return scaled;
    }
}
