package be.kdg.parkwise.config;

import be.kdg.parkwise.controller.AuthController;
import be.kdg.parkwise.domain.Enum.SubscriptionType;
import be.kdg.parkwise.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final SubscriptionService subscriptionService;

    public GlobalControllerAdvice(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Automatically adds subscription information to every page's model.
     * This allows the navigation to conditionally show/hide buttons based on plan.
     */
    @ModelAttribute
    public void addSubscriptionAttributes(Model model, HttpSession session) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser != null) {
            SubscriptionType planType = subscriptionService.getEffectivePlanCodeForUser(currentUser.getId());

            model.addAttribute("hasMapAccess", planType != null);
            model.addAttribute("hasAnalyticsAccess",
                    planType == SubscriptionType.PREMIUM || planType == SubscriptionType.ULTRA);
            model.addAttribute("hasPredictionAccess",
                    planType == SubscriptionType.ULTRA);
            model.addAttribute("userPlanType", planType != null ? planType.name() : "NONE");
        } else {
            model.addAttribute("hasMapAccess", false);
            model.addAttribute("hasAnalyticsAccess", false);
            model.addAttribute("hasPredictionAccess", false);
            model.addAttribute("userPlanType", "NONE");
        }
    }
}