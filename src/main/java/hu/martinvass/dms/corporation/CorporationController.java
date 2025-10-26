package hu.martinvass.dms.corporation;

import hu.martinvass.dms.corporation.data.CreateCorporationDTO;
import hu.martinvass.dms.corporation.service.CorporationService;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@AllArgsConstructor
public class CorporationController {

    private final CorporationService corporationService;

    @PostMapping("corporation/create")
    public String handleCreate(@ModelAttribute("createDto") CreateCorporationDTO dto,
                               Principal principal,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        try {
            corporationService.createCorporation(dto, principal.getName(), session);
            redirectAttributes.addFlashAttribute("message", "Corporation created.");

            // TODO: redirect to company dashboard or something
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/home";
        }
    }
}