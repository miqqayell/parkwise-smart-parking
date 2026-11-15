package be.kdg.parkwise.controller;

import be.kdg.parkwise.domain.User;
import be.kdg.parkwise.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller responsible for authentication and session management.
 *
 * Handles login, signup and logout, and stores a lightweight user view in the HTTP session
 * under the attribute name {@code currentUser}.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes a login attempt and stores a minimal user view in the session on success.
     *
     * @param email              user's email
     * @param password           user's raw password
     * @param session            HTTP session to store the current user
     * @param redirectAttributes flash attributes for success or error feedback
     * @return redirect to the user dashboard on success; home page on failure
     */
    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        try {
            if (!userService.authenticateUser(email, password)) {
                redirectAttributes.addFlashAttribute("loginError", "Invalid email or password");
                return "redirect:/home";
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("loginError", "User not found");
                return "redirect:/home";
            }

            User user = userOpt.get();
            UserSessionView sessionView = new UserSessionView();
            sessionView.setId(user.getId());
            sessionView.setFirstName(user.getFirstName());
            sessionView.setLastName(user.getLastName());
            sessionView.setEmail(user.getEmail());
            sessionView.setAvatarUrl("https://ui-avatars.com/api/?name=" +
                    user.getFirstName().charAt(0) + "+" + user.getLastName().charAt(0) +
                    "&background=0D8ABC&color=fff");

            session.setAttribute("currentUser", sessionView);
            session.setAttribute("userRole", user.getRole()); // Store user role in session

            // Update last login timestamp
            userService.updateLastLogin(email);

            return "redirect:/user";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("loginError", "Login failed: " + e.getMessage());
            return "redirect:/home";
        }
    }

    /**
     * Registers a new user and logs them in by creating a session entry.
     *
     * @param firstName          user's first name
     * @param lastName           user's last name
     * @param email              user's email
     * @param password           user's raw password
     * @param session            HTTP session to store the current user
     * @param redirectAttributes flash attributes for success or error feedback
     * @return redirect to the user dashboard on success; home page on failure
     */
    @PostMapping("/signup")
    public String doSignup(@RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {

        try {
            User user = userService.registerUser(email, password, firstName.trim(), lastName.trim());

            UserSessionView sessionView = new UserSessionView();
            sessionView.setId(user.getId());
            sessionView.setFirstName(user.getFirstName());
            sessionView.setLastName(user.getLastName());
            sessionView.setEmail(user.getEmail());
            sessionView.setAvatarUrl("https://ui-avatars.com/api/?name=" +
                    firstName.charAt(0) + (lastName.isEmpty() ? "" : "+" + lastName.charAt(0)) +
                    "&background=0D8ABC&color=fff");

            session.setAttribute("currentUser", sessionView);
            session.setAttribute("userRole", user.getRole()); // Store user role in session

            // Update last login timestamp for new user
            userService.updateLastLogin(email);

            return "redirect:/user";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("signupError", e.getMessage());
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("signupError", "Registration failed: " + e.getMessage());
            return "redirect:/home";
        }
    }

    /**
     * Logs out the current user by invalidating the HTTP session.
     *
     * @param session current HTTP session
     * @return redirect to the public home page
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/home";
    }

    /**
     * Lightweight user view stored in the HTTP session to avoid exposing the full entity.
     * Contains only non-sensitive display and identification fields.
     */
    // This is the object we keep in session
    public static class UserSessionView {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String avatarUrl;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    }
}