package hu.martinvass.dms.auth;

import hu.martinvass.dms.user.AppUser;
import hu.martinvass.dms.user.exception.UserAlreadyExistsException;
import hu.martinvass.dms.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller class responsible for handling authentication-related operations.
 */
@Controller
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Handler method for the home page.
     *
     * @return The name of the view template for the home page.
     */
    @GetMapping("/")
    private String index() {
        return "index";
    }

    @GetMapping("/access-denied")
    private String accessDenied() {
        return "access-denied";
    }

    /**
     * Handles the login page request.
     *
     * @return The view name for the login page.
     */
    @GetMapping("auth/login")
    private String login() {
        // We prevent logged-in users from accessing the login page
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/";
        }

        return "auth/login";
    }

    /**
     * Handles the registration page request.
     *
     * @param model The model to add attributes for rendering the view.
     * @return The view name for the registration page.
     */
    @GetMapping("auth/sign-up")
    private String signUp(Model model) {
        // We prevent logged-in users from accessing the sign-up page
        if (SecurityUtils.isAuthenticated()) {
            return "redirect:/";
        }

        model.addAttribute("user", new AppUser());
        return "auth/sign-up";
    }

    /**
     * Handles the registration form submission.
     *
     * @param user The AppUser object containing registration information.
     * @param redirectAttributes Interface to carry attributes through redirection
     * @param model The model to add attributes for rendering the view.
     * @return A redirection URL based on the registration result.
     */
    @PostMapping("auth/sign-up")
    private String handleSignUp(@ModelAttribute AppUser user, RedirectAttributes redirectAttributes, Model model) {
        try {
            authService.registerUser(user);
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "auth/sign-up";
        }

        redirectAttributes.addFlashAttribute("message", "Registration successful! Please confirm your email address.");
        return "redirect:/auth/login";
    }

    @GetMapping("auth/verify")
    public String verifyAccount(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        var result = authService.verifyUser(token);

        switch (result) {
            case SUCCESS -> {
                redirectAttributes.addFlashAttribute("message", "Account successfully activated! You can now log in.");
                return "redirect:/auth/login";
            }
            case EXPIRED -> {
                redirectAttributes.addFlashAttribute("error", "The activation link has expired. Request a new one.");
                return "redirect:/auth/verification-failed";
            }
            case INVALID -> {
                redirectAttributes.addFlashAttribute("error", "Invalid activation token.");
                return "redirect:/auth/verification-failed";
            }
        }

        redirectAttributes.addFlashAttribute("error", "An unknown error has occurred.");
        return "redirect:/auth/verification-failed";
    }

    @GetMapping("auth/verification-failed")
    public String handleVerificationFailed(Model model) {
        return "auth/verification-failed";
    }
}