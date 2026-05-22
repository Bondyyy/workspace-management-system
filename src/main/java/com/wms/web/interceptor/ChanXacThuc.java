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
        String path = request.getRequestURI();

        if (user == null) {
            if (path.startsWith("/staff/api")) {
                ghiLoiJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Phiên đăng nhập nhân viên đã hết hạn.");
                return false;
            }
            response.sendRedirect("/dangNhap");
            return false;
        }

        if (path.startsWith("/staff") && !user.laNhanVien()) {
            if (path.startsWith("/staff/api")) {
                ghiLoiJson(response, HttpServletResponse.SC_FORBIDDEN, "Tài khoản không có quyền nhận chỗ.");
                return false;
            }
            response.sendRedirect("/portal");
            return false;
        }

        return true;
    }

    private void ghiLoiJson(HttpServletResponse response, int status, String thongBao) throws java.io.IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"thanhCong\":false,\"thongBao\":\"" + thoatJson(thongBao) + "\"}");
    }

    private String thoatJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
