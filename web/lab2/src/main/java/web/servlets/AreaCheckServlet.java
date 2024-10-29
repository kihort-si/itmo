package web.servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import web.Dot;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/check")
public class AreaCheckServlet extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(AreaCheckServlet.class);

    @Override
    protected void doPost (HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        processingRequest(req, resp);
    }

    private void processingRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Double x = Double.parseDouble(req.getParameter("x"));
            Double y = Double.parseDouble(req.getParameter("y"));
            Integer r = Integer.parseInt(req.getParameter("r"));

            long startTime = System.nanoTime();

            boolean check = check(x, y, r);
            long executionTime = System.nanoTime() - startTime;

            logger.info("Values: x={}, y={}, r={}; Check result: {}", x, y, r, check);

            LocalDateTime currentTime = LocalDateTime.now();
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            String formattedTime = currentTime.format(timeFormatter);
            Dot dot = new Dot(x, y, r, formattedTime, executionTime, check);

            HttpSession session = req.getSession();
            List<Dot> dots = (List<Dot>) session.getAttribute("dots");
            if (dots == null) {
                dots = new ArrayList<>();
            }
            dots.add(dot);
            session.setAttribute("dots", dots);

            req.setAttribute("dot", dot);
            req.getRequestDispatcher("/result.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            logger.error("Invalid input format: {}", e.getMessage());
            req.setAttribute("error", "Invalid input format");
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
            req.getRequestDispatcher("/index.jsp").forward(req, resp);
        }
    }

    private static boolean check(Double x, Double y, Integer r) {
        if (x <= 0 && y >= 0) return (x >= -r && y <= (double) r / 2);
        if (x >= 0 && y <= 0) return ((x * x + y * y) <= (r * r));
        if (x > 0 && y > 0) return (x <= (double) r / 2) && (y <= (double) r / 2) && (2 * y + 2 * x <= r);
        return false;
    }
}
