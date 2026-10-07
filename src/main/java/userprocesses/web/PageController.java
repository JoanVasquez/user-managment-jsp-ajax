package userprocesses.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("pageTitle", "User Processes");
        return "index";
    }

    @GetMapping("/welcome")
    public String welcome(Model model, Authentication authentication) {
        model.addAttribute("pageTitle", "User Management");
        model.addAttribute("authenticated", authentication != null && authentication.isAuthenticated());
        return "welcome";
    }
}
