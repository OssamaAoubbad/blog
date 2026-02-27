package net.aoubbad.blog.controller;

import net.aoubbad.blog.entity.enums.PostStatus;
import net.aoubbad.blog.entity.enums.RoleName;
import net.aoubbad.blog.service.CommentService;
import net.aoubbad.blog.service.PostService;
import net.aoubbad.blog.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    public AdminController(UserService userService,
                           PostService postService,
                           CommentService commentService) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.countActiveUsers());
        model.addAttribute("totalPublished", postService.countByStatus(PostStatus.PUBLISHED));
        model.addAttribute("totalDrafts", postService.countByStatus(PostStatus.DRAFT));
        model.addAttribute("pendingComments", commentService.countPending());
        model.addAttribute("recentComments", commentService.findRecentComments(5));
        return "admin/dashboard";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public String listUsers(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("users", keyword != null && !keyword.isBlank()
                ? userService.searchUsers(keyword)
                : userService.findAll());
        model.addAttribute("keyword", keyword);
        return "admin/users/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/enable")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.enableUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur active.");
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.disableUser(id);
        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur desactive.");
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/role/assign")
    public String assignRole(@PathVariable Long id,
                             @RequestParam RoleName roleName,
                             RedirectAttributes redirectAttributes) {
        userService.assignRole(id, roleName);
        redirectAttributes.addFlashAttribute("successMessage", "Role " + roleName + " assigne.");
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/role/revoke")
    public String revokeRole(@PathVariable Long id,
                             @RequestParam RoleName roleName,
                             RedirectAttributes redirectAttributes) {
        userService.revokeRole(id, roleName);
        redirectAttributes.addFlashAttribute("successMessage", "Role " + roleName + " revoque.");
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        userService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Utilisateur supprime.");
        return "redirect:/admin/users";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping("/posts")
    public String listPosts(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "15") int size,
                            @RequestParam(required = false) String status,
                            Model model) {

        Page<?> posts = (status != null && !status.isBlank())
                ? postService.findByStatus(PostStatus.valueOf(status), page, size)
                : postService.findAll(page, size);

        model.addAttribute("posts", posts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", posts.getTotalPages());
        model.addAttribute("statuses", PostStatus.values());
        model.addAttribute("selectedStatus", status);
        return "admin/posts/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        postService.deleteByAdmin(id);
        redirectAttributes.addFlashAttribute("successMessage", "Article supprime.");
        return "redirect:/admin/posts";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping("/comments")
    public String pendingComments(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  Model model) {

        Page<?> pending = commentService.findPendingPaginated(page, size);
        model.addAttribute("comments", pending.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pending.getTotalPages());
        return "admin/comments/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/comments/{id}/approve")
    public String approveComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        commentService.approve(id);
        redirectAttributes.addFlashAttribute("successMessage", "Commentaire approuve.");
        return "redirect:/admin/comments";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/comments/{id}/reject")
    public String rejectComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        commentService.reject(id);
        redirectAttributes.addFlashAttribute("successMessage", "Commentaire rejete.");
        return "redirect:/admin/comments";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        commentService.deleteByAdmin(id);
        redirectAttributes.addFlashAttribute("successMessage", "Commentaire supprime.");
        return "redirect:/admin/comments";
    }
}