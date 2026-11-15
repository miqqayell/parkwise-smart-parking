package be.kdg.parkwise.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UpgradeController {

    @GetMapping("/subscription/upgrade")
    public String showUpgrade(@RequestParam(required = false) String feature, Model model) {
        model.addAttribute("feature", feature);
        return "subscriptions/upgrade";
    }
}