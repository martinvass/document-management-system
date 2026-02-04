package hu.martinvass.dms.corporation.controller;

import hu.martinvass.dms.data.CreateCorporationDTO;
import hu.martinvass.dms.corporation.service.CorporationService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@AllArgsConstructor
@RequestMapping("/corporation")
public class CorporationController {

    private final CorporationService corporationService;

    @PostMapping("/create")
    public String handleCreate(@ModelAttribute("createDto") CreateCorporationDTO dto,
                               Principal principal,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        try {
            corporationService.createCorporation(dto, principal.getName(), session);
            redirectAttributes.addFlashAttribute("message", "Corporation created");

            // TODO: redirect to company dashboard or something
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
        }
    }

    @GetMapping("/tags")
    public String handleTags() {
        return "/corporation/tags";
    }
}