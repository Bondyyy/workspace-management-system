package com.wms.web.controller;

import com.wms.web.form.LoginForm;
import com.wms.web.form.OtpForm;
import com.wms.web.form.RegisterForm;
import com.wms.web.model.PendingRegistration;
import com.wms.web.model.SessionUser;
import com.wms.web.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "web/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                        BindingResult bindingResult,
                        HttpSession session,
                        Model model) {
        if (bindingResult.hasErrors()) {
            return "web/login";
        }

        try {
            SessionUser user = authService.login(loginForm);
            session.setAttribute("user", user);
            return user.isStaff() ? "redirect:/staff/bookings" : "redirect:/portal";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "web/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "web/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm registerForm,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "web/register";
        }

        try {
            PendingRegistration pendingRegistration = authService.requestRegistrationOtp(registerForm);
            session.setAttribute("pendingRegistration", pendingRegistration);
            redirectAttributes.addFlashAttribute("success", "Mã OTP đã được gửi tới email của bạn.");
            return "redirect:/register/verify";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "web/register";
        }
    }

    @GetMapping("/register/verify")
    public String verifyPage(HttpSession session, Model model) {
        PendingRegistration pendingRegistration =
                (PendingRegistration) session.getAttribute("pendingRegistration");
        if (pendingRegistration == null) {
            return "redirect:/register";
        }
        if (!model.containsAttribute("otpForm")) {
            model.addAttribute("otpForm", new OtpForm());
        }
        model.addAttribute("email", pendingRegistration.getEmail());
        return "web/verify-otp";
    }

    @PostMapping("/register/verify")
    public String verify(@Valid @ModelAttribute("otpForm") OtpForm otpForm,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        PendingRegistration pendingRegistration =
                (PendingRegistration) session.getAttribute("pendingRegistration");
        if (pendingRegistration == null) {
            return "redirect:/register";
        }

        model.addAttribute("email", pendingRegistration.getEmail());
        if (bindingResult.hasErrors()) {
            return "web/verify-otp";
        }

        try {
            authService.completeRegistration(pendingRegistration, otpForm.getOtp());
            session.removeAttribute("pendingRegistration");
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công. Bạn có thể đăng nhập ngay.");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            model.addAttribute("error", "Không tạo được tài khoản: " + ex.getMessage());
            return "web/verify-otp";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
