package net.aoubbad.blog.controller;

import net.aoubbad.blog.entity.User;
import net.aoubbad.blog.service.CustomUserDetails;
import net.aoubbad.blog.service.FileStorageService;
import net.aoubbad.blog.service.PostService;
import net.aoubbad.blog.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final FileStorageService fileStorageService;

    public UserController(UserService userService,
                          PostService postService,
                          FileStorageService fileStorageService) {
        this.userService = userService;
        this.postService = postService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/{username}")
    public String publicProfile(@PathVariable String username, Model model) {
        User user = userService.findByUsername(username);
        model.addAttribute("user", user);
        model.addAttribute("recentPosts", postService.findRecentByAuthor(user.getId(), 5));
        model.addAttribute("postCount", postService.countByAuthor(user.getId()));
        model.addAttribute("totalViews", postService.sumViewsByAuthor(user.getId()));
        return "users/profile";
    }

    @GetMapping("/me")
    public String myProfile(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        try {
            User user = resolveCurrentUser(currentUser);
            model.addAttribute("user", user);
            model.addAttribute("recentPosts", postService.findRecentByAuthor(user.getId(), 5));
            model.addAttribute("postCount", postService.countByAuthor(user.getId()));
            model.addAttribute("totalViews", postService.sumViewsByAuthor(user.getId()));
            return "users/profile";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/me/edit")
    public String editProfileForm(@AuthenticationPrincipal UserDetails currentUser, Model model) {
        try {
            User user = resolveCurrentUser(currentUser);
            model.addAttribute("user", user);
            return "users/edit";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
    }

    @PostMapping("/me/edit")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                @RequestParam(value = "profileImageFile", required = false) MultipartFile profileImageFile,
                                @AuthenticationPrincipal UserDetails currentUser,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            String uploadedImagePath = fileStorageService.storeProfileImage(profileImageFile);
            if (uploadedImagePath != null) {
                updatedUser.setProfilePicture(uploadedImagePath);
            }

            User user = resolveCurrentUser(currentUser);
            userService.updateProfile(user.getId(), updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", "Profil mis a jour !");
            return "redirect:/users/me";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", updatedUser);
            return "users/edit";
        } catch (Exception e) {
            model.addAttribute("errorMessage",
                    (e.getMessage() != null && !e.getMessage().isBlank())
                            ? e.getMessage()
                            : "Erreur lors de la mise a jour du profil.");
            model.addAttribute("user", updatedUser);
            return "users/edit";
        }
    }

    @GetMapping("/me/password")
    public String changePasswordForm() {
        return "users/change-password";
    }

    @PostMapping("/me/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 @AuthenticationPrincipal UserDetails currentUser,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Le nouveau mot de passe et la confirmation ne correspondent pas.");
            return "users/change-password";
        }

        try {
            User user = resolveCurrentUser(currentUser);
            userService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Mot de passe modifie avec succes !");
            return "redirect:/users/me";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "users/change-password";
        }
    }

    private User resolveCurrentUser(UserDetails currentUser) {
        if (currentUser instanceof CustomUserDetails customUser) {
            return userService.findById(customUser.getId());
        }
        return userService.findByUsername(currentUser.getUsername());
    }
}
