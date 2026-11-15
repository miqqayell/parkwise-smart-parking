package be.kdg.parkwise.config;

import be.kdg.parkwise.controller.AuthController;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UserSessionAdvice {

    @ModelAttribute("currentUser")
    public AuthController.UserSessionView addUserToModel(HttpSession session) {
        return (AuthController.UserSessionView) session.getAttribute("currentUser");
    }
}
