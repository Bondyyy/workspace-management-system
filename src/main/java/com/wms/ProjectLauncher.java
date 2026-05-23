package com.wms;

import org.springframework.boot.SpringApplication;
import javax.swing.SwingUtilities;

public class ProjectLauncher {

    public static void main(String[] args) {
        System.out.println("[Launcher] Starting WMS Multi-Platform System...");
        System.out.println("[Launcher] ProjectLauncher dang chay che do full Desktop + Web.");

        Thread webThread = new Thread(() -> {
            try {
                System.out.println("[Launcher] Starting Web Server...");
                SpringApplication.run(WebApplication.class, args);
            } catch (Exception e) {
                System.err.println("[Launcher] Failed to start Web Server: " + e.getMessage());
                e.printStackTrace();
            }
        });
        webThread.setName("WMS-Web-Server");
        webThread.start();

        System.out.println("[Launcher] Starting Desktop UI...");
        MainApp.main(args);
    }
}
