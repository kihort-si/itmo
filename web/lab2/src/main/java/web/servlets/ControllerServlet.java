package web.servlets;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet("/controller")
public class ControllerServlet extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(ControllerServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processingRequest(req, resp);
    }

    private void processingRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var x = Double.parseDouble(req.getParameter("x"));
            var y = Double.parseDouble(req.getParameter("y"));
            var r = Integer.parseInt(req.getParameter("r"));
            logger.info("Values: x={}, y={}, r={}", x, y, r);

            req.setAttribute("x", x);
            req.setAttribute("y", y);
            req.setAttribute("r", r);

            req.getRequestDispatcher("/check").forward(req, resp);

        } catch (NumberFormatException e) {
            logger.error("Invalid format: {}", e.getMessage());
            req.setAttribute("error", "Invalid input format");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }
    }
}
