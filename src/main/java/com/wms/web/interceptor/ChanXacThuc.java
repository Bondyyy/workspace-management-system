package com.wms.web.interceptor;

import com.wms.web.model.NguoiDungPhien;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ChanXacThuc implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        NguoiDungPhien user = session == null ? null : (NguoiDungPhien) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("/dangNhap");
            return false;
        }

        String path = request.getRequestURI();
        if (path.startsWith("/staff") && !user.laNhanVien()) {
            response.sendRedirect("/portal");
            return false;
        }

        return true;
    }
}
