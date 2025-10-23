package hu.martinvass.dms.auth;

import hu.martinvass.dms.user.AppUser;
import hu.martinvass.dms.user.exception.UserAlreadyExistsException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller class responsible for handling authentication-related operations.
 */
@Controller
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Handles the login page request.
     *
     * @param model The model to add attributes for rendering the view.
     * @return The view name for the login page.
     */
    @GetMapping("auth/login")
    private String login(Model model) {
        model.addAttribute("title", "DMS - Login");
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
        model.addAttribute("title", "DMS - Sign Up");
        model.addAttribute("user", new AppUser());

        return "auth/sign-up";
    }

    /**
     * Handles the registration form submission.
     *
     * @param user The AppUser object containing registration information.
     * @return A redirection URL based on the registration result.
     */
    @PostMapping("auth/sign-up")
    private String handleSignUp(@ModelAttribute AppUser user, Model model) {
        try {
            authService.registerUser(user);
        } catch (UserAlreadyExistsException e) {
            model.addAttribute("error", e.getMessage());
            return "/auth/sign-up";
        }

        model.addAttribute("message", "Sikeres regisztráció! Kérlek, erősítsd meg az e-mail címedet.");
        return "redirect:/auth/login";
    }

    @GetMapping("auth/verify")
    public String verifyAccount(@RequestParam("token") String token, Model model) {
        var result = authService.verifyUser(token);

        System.out.println("token: " + token);
        System.out.println("result: " + result.name());
        switch (result) {
            case SUCCESS -> {
                model.addAttribute("message", "Fiók sikeresen aktiválva! Most már bejelentkezhetsz.");
                return "auth/login";
            }
            case EXPIRED -> {
                model.addAttribute("error", "Az aktiváló link lejárt. Kérj újat.");
                return "auth/verification-failed";
            }
            case INVALID -> {
                model.addAttribute("error", "Érvénytelen aktiváló link.");
                return "auth/verification-failed";
            }
        }

        model.addAttribute("error", "Ismeretlen hiba történt.");
        return "auth/verification-failed";
    }
}