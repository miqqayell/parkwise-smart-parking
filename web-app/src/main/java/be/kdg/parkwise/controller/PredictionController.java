package be.kdg.parkwise.controller;

import be.kdg.parkwise.dto.AiModelResponseDto;
import be.kdg.parkwise.service.PredictionService;
import be.kdg.parkwise.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;

@Controller
public class PredictionController {

    private final PredictionService predictionService;
    private final SubscriptionService subscriptionService;

    public PredictionController(PredictionService predictionService, SubscriptionService subscriptionService) {
        this.predictionService = predictionService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/prediction")
    public String showPrediction(HttpSession session) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/home";
        }

        if (!subscriptionService.hasPredictionAccess(currentUser.getId())) {
            return "redirect:/subscription/upgrade?feature=prediction";
        }

        return "prediction";
    }

    @GetMapping("/api/prediction/generate")
    @ResponseBody
    public AiModelResponseDto getPredictionData(
            @RequestParam("time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime time) {

        return predictionService.fetchPredictions(time);
    }
}