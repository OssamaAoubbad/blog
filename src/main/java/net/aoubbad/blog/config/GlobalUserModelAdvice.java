package net.aoubbad.blog.config;

import net.aoubbad.blog.entity.User;
import net.aoubbad.blog.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserModelAdvice {

    private final UserService userService;

    public GlobalUserModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUserProfile")
    public User currentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        try {
            return userService.findByUsername(authentication.getName());
        } catch (Exception e) {
            return null;
        }
    }
}
