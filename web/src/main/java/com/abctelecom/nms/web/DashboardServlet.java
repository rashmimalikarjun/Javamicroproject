package com.abctelecom.nms.web;

import com.abctelecom.nms.model.BaseStation;
import com.abctelecom.nms.service.BaseStationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Read-only monitoring view: base-station load table (same service layer as Swing). */
@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<BaseStation> stations = new BaseStationService().list();
            req.setAttribute("stations", stations);
            req.getRequestDispatcher("/WEB-INF/dashboard.jsp").forward(req, resp);
        } catch (Exception ex) {
            req.setAttribute("error", "Unable to load dashboard data: " + ex.getMessage());
            req.getRequestDispatcher("/WEB-INF/dashboard.jsp").forward(req, resp);
        }
    }
}
