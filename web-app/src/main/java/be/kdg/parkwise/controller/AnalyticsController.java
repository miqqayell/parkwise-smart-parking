package be.kdg.parkwise.controller;

import be.kdg.parkwise.service.AnalyticsService;
import be.kdg.parkwise.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class AnalyticsController {

    private final SubscriptionService subscriptionService;
    private final AnalyticsService analyticsService;

    public AnalyticsController(SubscriptionService subscriptionService,
                               AnalyticsService analyticsService) {
        this.subscriptionService = subscriptionService;
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics")
    public String showAnalytics(HttpSession session, Model model) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/home";
        }

        if (!subscriptionService.hasAnalyticsAccess(currentUser.getId())) {
            return "redirect:/subscription/upgrade?feature=analytics";
        }

        // Fetch analytics data
        Map<String, Object> occupancyData = analyticsService.getOccupancyOverTime();
        Map<String, Object> heatmapData = analyticsService.getSpotHeatmap();
        Map<String, Object> scatterData = analyticsService.getDistanceScatter();
        Map<String, Object> peakHours = analyticsService.getPeakHours();
        Map<String, Object> stats = analyticsService.getStats();

        // Fetch AI predictions
        Map<String, Object> aiPredictions = analyticsService.getAIPredictions();
        Map<String, Object> aiForecast = analyticsService.getAIForecast();
        Map<String, Object> aiBusynessChart = analyticsService.getAIBusynessChart();

        // Add to model
        model.addAttribute("occupancyChart", occupancyData.get("image"));
        model.addAttribute("heatmapChart", heatmapData.get("image"));
        model.addAttribute("scatterChart", scatterData.get("image"));
        model.addAttribute("peakHours", peakHours.get("peak_hours"));
        model.addAttribute("stats", stats);

        // Add AI data
        model.addAttribute("aiPredictions", aiPredictions.get("predictions"));
        model.addAttribute("aiForecast", aiForecast.get("predictions"));
        model.addAttribute("aiBusynessChart", aiBusynessChart.get("image"));
        model.addAttribute("weather", aiPredictions.get("weather"));

        // Error handling
        if (occupancyData.containsKey("error")) {
            model.addAttribute("error", occupancyData.get("error"));
        }

        return "analytics";
    }
}