package com.wms.web.controller;

import com.wms.web.model.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        SessionUser user = (SessionUser) session.getAttribute("user");
        model.addAttribute("user", user);
        return "web/index";
    }
}
