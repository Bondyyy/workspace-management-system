package com.wms.web.interceptor;

import com.wms.web.model.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/staff") && !user.isStaff()) {
            response.sendRedirect("/portal");
            return false;
        }

        return true;
    }
}
