package com.abctelecom.nms.web;

import com.abctelecom.nms.service.AuthService;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "LogoutServlet", urlPatterns = "/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object u = session.getAttribute("user");
            session.invalidate();
            try { new AuthService().logout(); } catch (Exception ignored) { /* end local session anyway */ }
        }
        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
