package hu.martinvass.dms.user.controller;

import hu.martinvass.dms.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/home")
    public String home(Model model, Principal principal) {
        var user = authService.findByUsername(principal.getName());

        model.addAttribute("user", user);
        return "home";
    }
}