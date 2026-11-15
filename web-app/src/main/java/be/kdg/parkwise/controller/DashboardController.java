package be.kdg.parkwise.controller;

import be.kdg.parkwise.service.ParkingService;
import be.kdg.parkwise.service.SubscriptionService;
import be.kdg.parkwise.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final SubscriptionService subscriptionService;

    public DashboardController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/search")
    public String search(Model model, HttpSession session) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        // Not logged in -> redirect to home with message
        if (currentUser == null) {
            return "redirect:/subscriptions";
        }

        // Check if user has map access
        if (!subscriptionService.hasMapAccess(currentUser.getId())) {
            return "redirect:/subscription/upgrade?feature=map";
        }

        // User has access - show the map
        return "index";
    }

    @GetMapping("/notifications")
    public String notifications(Model model, HttpSession session) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        // Not logged in -> redirect to home
        if (currentUser == null) {
            return "redirect:/home";
        }

        // Show notifications page
        return "notifications";
    }
}
//Premium/Ultra for later
// (Add this later only if you have the template)
// @GetMapping("/availability")
// public String availability() { return "availability"; }

