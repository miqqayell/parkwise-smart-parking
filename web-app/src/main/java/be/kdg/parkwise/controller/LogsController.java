package be.kdg.parkwise.controller;

import be.kdg.parkwise.domain.SystemLog;
import be.kdg.parkwise.service.LoggingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class LogsController {

    private final LoggingService loggingService;

    public LogsController(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @GetMapping("/developer/logs")
    public String logsPage(HttpSession session, Model model,
                           @RequestParam(required = false) String level,
                           @RequestParam(required = false) String category) {

        // Check authentication
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/home";
        }

        // Check if user has developer or system_admin role
        String userRole = (String) session.getAttribute("userRole");
        if (!"developer".equals(userRole) && !"system_admin".equals(userRole)) {
            return "redirect:/home";
        }

        // Get logs based on filter
        List<SystemLog> logs;
        if (level != null && !level.isEmpty()) {
            logs = loggingService.getLogsByLevel(level);
        } else if (category != null && !category.isEmpty()) {
            logs = loggingService.getLogsByCategory(category);
        } else {
            logs = loggingService.getRecentLogs(100);
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("logs", logs);
        model.addAttribute("statistics", loggingService.getLogStatistics());
        model.addAttribute("currentLevel", level);
        model.addAttribute("currentCategory", category);

        return "developer/logs";
    }

    @GetMapping("/api/logs/recent")
    @ResponseBody
    public List<SystemLog> getRecentLogs(@RequestParam(defaultValue = "50") int limit) {
        return loggingService.getRecentLogs(limit);
    }

    @GetMapping("/api/logs/statistics")
    @ResponseBody
    public Map<String, Long> getStatistics() {
        return loggingService.getLogStatistics();
    }
}
