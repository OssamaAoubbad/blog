package net.aoubbad.blog.controller;

import jakarta.validation.Valid;
import net.aoubbad.blog.entity.User;
import net.aoubbad.blog.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {

        if (result.hasErrors()) {
            return "auth/register";
        }

        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("errorUsername", "Ce nom d'utilisateur est deja pris.");
            return "auth/register";
        }

        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("errorEmail", "Cette adresse email est deja utilisee.");
            return "auth/register";
        }

        try {
            userService.register(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Compte cree. Verifiez votre email pour activer votre compte.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    (e.getMessage() != null && !e.getMessage().isBlank())
                            ? e.getMessage()
                            : "Une erreur est survenue lors de l'inscription.");
            return "auth/register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token) {
        return userService.verifyEmail(token)
                ? "auth/verify-success"
                : "auth/verify-error";
    }
}
