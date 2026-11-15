package be.kdg.parkwise.controller;

import be.kdg.parkwise.domain.SubscriptionPlan;
import be.kdg.parkwise.domain.UserSubscription;
import be.kdg.parkwise.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controller handling subscription browsing and lifecycle actions for the logged-in user.
 *
 * Exposes endpoints to list available plans, subscribe, cancel, and change plans. Uses the
 * current user stored in the HTTP session (see {@link AuthController.UserSessionView}).
 */
@Controller
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Displays available subscription plans and, if logged in, the user's current subscription.
     *
     * @param session HTTP session containing the current user (if any)
     * @param model   view model populated with plans and current subscription
     * @return the subscriptions view name
     */
    @GetMapping("/subscriptions")
    public String showSubscriptions(HttpSession session, Model model) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        List<SubscriptionPlan> plans = subscriptionService.getActivePlans();
        model.addAttribute("plans", plans);

        if (currentUser != null) {
            subscriptionService.getActiveSubscriptionForUser(currentUser.getId())
                    .ifPresent(sub -> model.addAttribute("currentSubscription", sub));
        }

        return "subscriptions";
    }

    /**
     * Subscribes the current user to the selected plan.
     *
     * @param planId             the ID of the plan to subscribe to
     * @param session            HTTP session used to resolve the current user
     * @param redirectAttributes flash attributes for user feedback
     * @return redirect to home on success; back to subscriptions on validation error
     */
    @PostMapping("/subscriptions/{planId}/subscribe")
    public String subscribe(@PathVariable Long planId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to subscribe");
            return "redirect:/home";
        }

        try {
            UserSubscription subscription = subscriptionService.subscribeUserToPlan(currentUser.getId(), planId);
            redirectAttributes.addFlashAttribute("success",
                    "Successfully subscribed to " + subscription.getLockedPlanName() + " plan! You now have access to the real-time map.");

            return "redirect:/home";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/subscriptions";
        }
    }

    /**
     * Cancels the current user's active subscription immediately.
     *
     * @param session            HTTP session used to resolve the current user
     * @param redirectAttributes flash attributes for success or error messages
     * @return redirect to the user dashboard after processing
     */
    @PostMapping("/subscriptions/cancel")
    public String cancelSubscription(HttpSession session, RedirectAttributes redirectAttributes) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to cancel your subscription");
            return "redirect:/home";
        }

        try {
            UserSubscription cancelledSub = subscriptionService.cancelSubscription(currentUser.getId());
            redirectAttributes.addFlashAttribute("success",
                    "Your " + cancelledSub.getLockedPlanName() + " subscription has been cancelled. You will retain access until the end of your billing period.");

            return "redirect:/user";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user";
        }
    }

    /**
     * Changes the current user's subscription to a different plan.
     *
     * @param planId             the ID of the new plan
     * @param session            HTTP session used to resolve the current user
     * @param redirectAttributes flash attributes for success or error messages
     * @return redirect to the user dashboard after processing
     */
    @PostMapping("/subscriptions/{planId}/change")
    public String changeSubscription(@PathVariable Long planId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in first");
            return "redirect:/home";
        }

        try {
            UserSubscription newSubscription = subscriptionService.changeSubscription(currentUser.getId(), planId);
            redirectAttributes.addFlashAttribute("success",
                    "Successfully changed to " + newSubscription.getLockedPlanName() + " plan! Your new features are now active.");

            return "redirect:/user";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/user";
        }
    }
}