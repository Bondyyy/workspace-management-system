package com.wms.web.controller;

import com.wms.web.model.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return user.isStaff() ? "redirect:/staff/bookings" : "redirect:/portal";
    }
}
